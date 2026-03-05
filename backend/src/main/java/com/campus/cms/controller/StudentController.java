package com.campus.cms.controller;

import com.campus.cms.dto.StudentRequest;
import com.campus.cms.entity.Student;
import com.campus.cms.entity.User;
import com.campus.cms.repository.StudentRepository;
import com.campus.cms.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentController(StudentRepository studentRepository, UserRepository userRepository,
                              PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return studentRepository.findAll().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return studentRepository.findById(id)
                .map(s -> ResponseEntity.ok(toMap(s)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody StudentRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalStateException("用户名已存在");
        }
        if (studentRepository.existsByStudentNo(req.getStudentNo())) {
            throw new IllegalStateException("学号已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(User.Role.student);
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        userRepository.save(user);

        Student student = new Student();
        student.setUser(user);
        student.setStudentNo(req.getStudentNo());
        student.setGrade(req.getGrade());
        student.setClassName(req.getClassName());
        studentRepository.save(student);

        return ResponseEntity.ok(toMap(student));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody StudentRequest req) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        User user = student.getUser();
        if (req.getName() != null) user.setName(req.getName());
        if (req.getEmail() != null) user.setEmail(req.getEmail());
        if (req.getGrade() != null) student.setGrade(req.getGrade());
        if (req.getClassName() != null) student.setClassName(req.getClassName());
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        userRepository.save(user);
        studentRepository.save(student);
        return ResponseEntity.ok(toMap(student));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        studentRepository.delete(student);
        userRepository.delete(student.getUser());
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    private Map<String, Object> toMap(Student s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("username", s.getUser().getUsername());
        m.put("name", s.getUser().getName());
        m.put("student_no", s.getStudentNo());
        m.put("grade", s.getGrade());
        m.put("class_name", s.getClassName());
        m.put("email", s.getUser().getEmail());
        return m;
    }
}
