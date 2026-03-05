package com.campus.cms.controller;

import com.campus.cms.dto.GradeRequest;
import com.campus.cms.entity.*;
import com.campus.cms.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/grades")
public class GradeController {

    private final GradeRepository gradeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;

    public GradeController(GradeRepository gradeRepository, EnrollmentRepository enrollmentRepository,
                             StudentRepository studentRepository, TeacherRepository teacherRepository,
                             UserRepository userRepository) {
        this.gradeRepository = gradeRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> list(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if (user.getRole() == User.Role.student) {
            Student student = studentRepository.findByUser(user)
                    .orElseThrow(() -> new IllegalArgumentException("学生不存在"));
            List<Map<String, Object>> grades = gradeRepository.findByStudent(student).stream()
                    .map(this::toMap)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(grades);
        } else if (user.getRole() == User.Role.teacher) {
            Teacher teacher = teacherRepository.findByUser(user)
                    .orElseThrow(() -> new IllegalArgumentException("教师不存在"));
            // Return all grades for teacher's classes
            List<Map<String, Object>> grades = gradeRepository.findAll().stream()
                    .filter(g -> g.getEnrollment().getClassEntity().getTeacher().getId().equals(teacher.getId()))
                    .map(this::toMap)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(grades);
        }
        return ResponseEntity.ok(gradeRepository.findAll().stream().map(this::toMap).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody GradeRequest req, Authentication auth) {
        Enrollment enrollment = enrollmentRepository.findById(req.getEnrollmentId())
                .orElseThrow(() -> new IllegalArgumentException("选课记录不存在"));

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Teacher teacher = teacherRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("教师不存在"));

        if (!enrollment.getClassEntity().getTeacher().getId().equals(teacher.getId())) {
            throw new SecurityException("无权为此课程录入成绩");
        }

        if (req.getScore() < 0 || req.getScore() > 100) {
            throw new IllegalArgumentException("分数必须在0-100之间");
        }

        if (gradeRepository.findByEnrollment(enrollment).isPresent()) {
            throw new IllegalStateException("成绩已存在，请使用PUT修改");
        }

        Grade grade = new Grade();
        grade.setEnrollment(enrollment);
        grade.setScore(req.getScore());
        gradeRepository.save(grade);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", grade.getId());
        result.put("enrollment_id", enrollment.getId());
        result.put("score", grade.getScore());
        result.put("message", "成绩录入成功");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody GradeRequest req, Authentication auth) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("成绩不存在"));

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Teacher teacher = teacherRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("教师不存在"));

        if (!grade.getEnrollment().getClassEntity().getTeacher().getId().equals(teacher.getId())) {
            throw new SecurityException("无权修改此成绩");
        }

        if (req.getScore() < 0 || req.getScore() > 100) {
            throw new IllegalArgumentException("分数必须在0-100之间");
        }

        grade.setScore(req.getScore());
        grade.setUpdatedAt(LocalDateTime.now());
        gradeRepository.save(grade);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", grade.getId());
        result.put("enrollment_id", grade.getEnrollment().getId());
        result.put("score", grade.getScore());
        result.put("message", "成绩修改成功");
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> toMap(Grade g) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", g.getId());
        m.put("enrollment_id", g.getEnrollment().getId());
        m.put("course_name", g.getEnrollment().getClassEntity().getCourse().getName());
        m.put("course_code", g.getEnrollment().getClassEntity().getCourse().getCode());
        m.put("student_name", g.getEnrollment().getStudent().getUser().getName());
        m.put("score", g.getScore());
        m.put("semester", g.getEnrollment().getClassEntity().getSemester());
        m.put("updated_at", g.getUpdatedAt());
        return m;
    }
}
