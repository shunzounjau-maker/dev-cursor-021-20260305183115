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
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentRepository enrollmentRepository;
    private final ClassRepository classRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;

    public EnrollmentController(EnrollmentRepository enrollmentRepository, ClassRepository classRepository,
                                  StudentRepository studentRepository, UserRepository userRepository,
                                  GradeRepository gradeRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.classRepository = classRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.gradeRepository = gradeRepository;
    }

    @GetMapping
    public ResponseEntity<?> list(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Student student = studentRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        List<Map<String, Object>> list = enrollmentRepository.findByStudent(student).stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public synchronized ResponseEntity<?> enroll(@RequestBody Map<String, Long> body, Authentication auth) {
        Long classId = body.get("class_id");
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Student student = studentRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("开课不存在"));

        if (enrollmentRepository.existsByClassEntityAndStudent(classEntity, student)) {
            throw new IllegalStateException("已选该课程");
        }

        long enrolled = enrollmentRepository.countByClassEntity(classEntity);
        if (classEntity.getCapacity() != null && enrolled >= classEntity.getCapacity()) {
            throw new IllegalStateException("课程已满");
        }

        Integer currentCredits = enrollmentRepository.sumCreditsByStudentAndSemester(student, classEntity.getSemester());
        int credits = currentCredits != null ? currentCredits : 0;
        if (credits + classEntity.getCourse().getCredits() > 30) {
            throw new IllegalStateException("超过学分上限（30学分/学期）");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setClassEntity(classEntity);
        enrollment.setStudent(student);
        enrollmentRepository.save(enrollment);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", enrollment.getId());
        result.put("class_id", classEntity.getId());
        result.put("message", "选课成功");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> drop(@PathVariable Long id, Authentication auth) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("选课记录不存在"));
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Student student = studentRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));

        if (!enrollment.getStudent().getId().equals(student.getId())) {
            throw new SecurityException("无权操作");
        }

        if (gradeRepository.findByEnrollment(enrollment).isPresent()) {
            throw new IllegalStateException("成绩已录入，无法退课");
        }

        enrollmentRepository.delete(enrollment);
        return ResponseEntity.ok(Map.of("message", "退课成功"));
    }

    private Map<String, Object> toMap(Enrollment e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("class_id", e.getClassEntity().getId());
        m.put("course_name", e.getClassEntity().getCourse().getName());
        m.put("course_code", e.getClassEntity().getCourse().getCode());
        m.put("credits", e.getClassEntity().getCourse().getCredits());
        m.put("teacher_name", e.getClassEntity().getTeacher().getUser().getName());
        m.put("semester", e.getClassEntity().getSemester());
        m.put("schedule", e.getClassEntity().getSchedule());
        m.put("enrolled_at", e.getEnrolledAt());
        return m;
    }
}
