package com.campus.cms.controller;

import com.campus.cms.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final ClassRepository classRepository;
    private final EnrollmentRepository enrollmentRepository;

    public DashboardController(UserRepository userRepository, TeacherRepository teacherRepository,
                                 StudentRepository studentRepository, CourseRepository courseRepository,
                                 ClassRepository classRepository, EnrollmentRepository enrollmentRepository) {
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.classRepository = classRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @GetMapping
    public ResponseEntity<?> getDashboard() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total_students", studentRepository.count());
        stats.put("total_teachers", teacherRepository.count());
        stats.put("total_courses", courseRepository.count());
        stats.put("total_classes", classRepository.count());
        stats.put("total_enrollments", enrollmentRepository.count());
        return ResponseEntity.ok(stats);
    }
}
