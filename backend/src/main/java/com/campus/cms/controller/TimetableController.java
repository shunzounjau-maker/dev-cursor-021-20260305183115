package com.campus.cms.controller;

import com.campus.cms.entity.*;
import com.campus.cms.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/timetable")
public class TimetableController {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public TimetableController(EnrollmentRepository enrollmentRepository,
                                 StudentRepository studentRepository,
                                 UserRepository userRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getTimetable(Authentication auth,
                                           @RequestParam(required = false) String semester) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Student student = studentRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));

        List<Map<String, Object>> timetable = enrollmentRepository.findByStudent(student).stream()
                .filter(e -> semester == null || e.getClassEntity().getSemester().equals(semester))
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("enrollment_id", e.getId());
                    m.put("class_id", e.getClassEntity().getId());
                    m.put("course_name", e.getClassEntity().getCourse().getName());
                    m.put("course_code", e.getClassEntity().getCourse().getCode());
                    m.put("credits", e.getClassEntity().getCourse().getCredits());
                    m.put("teacher_name", e.getClassEntity().getTeacher().getUser().getName());
                    m.put("schedule", e.getClassEntity().getSchedule());
                    m.put("semester", e.getClassEntity().getSemester());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(timetable);
    }
}
