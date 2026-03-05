package com.campus.cms.controller;

import com.campus.cms.dto.TeacherRequest;
import com.campus.cms.entity.Teacher;
import com.campus.cms.entity.User;
import com.campus.cms.repository.TeacherRepository;
import com.campus.cms.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/teachers")
public class TeacherController {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TeacherController(TeacherRepository teacherRepository, UserRepository userRepository,
                              PasswordEncoder passwordEncoder) {
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return teacherRepository.findAll().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return teacherRepository.findById(id)
                .map(t -> ResponseEntity.ok(toMap(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TeacherRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalStateException("用户名已存在");
        }
        if (teacherRepository.existsByStaffNo(req.getStaffNo())) {
            throw new IllegalStateException("工号已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(User.Role.teacher);
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        userRepository.save(user);

        Teacher teacher = new Teacher();
        teacher.setUser(user);
        teacher.setStaffNo(req.getStaffNo());
        teacher.setDepartment(req.getDepartment());
        teacherRepository.save(teacher);

        return ResponseEntity.ok(toMap(teacher));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody TeacherRequest req) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("教师不存在"));
        User user = teacher.getUser();
        if (req.getName() != null) user.setName(req.getName());
        if (req.getEmail() != null) user.setEmail(req.getEmail());
        if (req.getDepartment() != null) teacher.setDepartment(req.getDepartment());
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        userRepository.save(user);
        teacherRepository.save(teacher);
        return ResponseEntity.ok(toMap(teacher));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("教师不存在"));
        teacherRepository.delete(teacher);
        userRepository.delete(teacher.getUser());
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    private Map<String, Object> toMap(Teacher t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("username", t.getUser().getUsername());
        m.put("name", t.getUser().getName());
        m.put("staff_no", t.getStaffNo());
        m.put("department", t.getDepartment());
        m.put("email", t.getUser().getEmail());
        return m;
    }
}
