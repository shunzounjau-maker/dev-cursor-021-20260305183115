package com.campus.cms.controller;

import com.campus.cms.dto.ClassRequest;
import com.campus.cms.entity.ClassEntity;
import com.campus.cms.entity.Teacher;
import com.campus.cms.entity.User;
import com.campus.cms.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/classes")
public class ClassController {

    private final ClassRepository classRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    public ClassController(ClassRepository classRepository, CourseRepository courseRepository,
                            TeacherRepository teacherRepository, UserRepository userRepository,
                            EnrollmentRepository enrollmentRepository) {
        this.classRepository = classRepository;
        this.courseRepository = courseRepository;
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @GetMapping
    public List<Map<String, Object>> list(Authentication auth) {
        List<ClassEntity> classes;
        // Teachers see only their own classes
        if (auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_teacher"))) {
            User user = userRepository.findByUsername(auth.getName()).orElse(null);
            if (user != null) {
                Teacher teacher = teacherRepository.findByUser(user).orElse(null);
                if (teacher != null) {
                    classes = classRepository.findByTeacher(teacher);
                } else {
                    classes = classRepository.findAll();
                }
            } else {
                classes = classRepository.findAll();
            }
        } else {
            classes = classRepository.findAll();
        }
        return classes.stream().map(this::toMap).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return classRepository.findById(id)
                .map(c -> ResponseEntity.ok(toMap(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/students")
    public ResponseEntity<?> getStudents(@PathVariable Long id) {
        ClassEntity classEntity = classRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("开课不存在"));
        List<Map<String, Object>> students = enrollmentRepository.findByClassEntity(classEntity).stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("enrollment_id", e.getId());
                    m.put("student_id", e.getStudent().getId());
                    m.put("student_no", e.getStudent().getStudentNo());
                    m.put("name", e.getStudent().getUser().getName());
                    m.put("class_name", e.getStudent().getClassName());
                    m.put("enrolled_at", e.getEnrolledAt());
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(students);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ClassRequest req) {
        var course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("课程不存在"));
        var teacher = teacherRepository.findById(req.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException("教师不存在"));

        ClassEntity classEntity = new ClassEntity();
        classEntity.setCourse(course);
        classEntity.setTeacher(teacher);
        classEntity.setSemester(req.getSemester());
        classEntity.setSchedule(req.getSchedule());
        classEntity.setCapacity(req.getCapacity());
        classRepository.save(classEntity);

        return ResponseEntity.ok(toMap(classEntity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ClassRequest req) {
        ClassEntity classEntity = classRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("开课不存在"));
        if (req.getCourseId() != null) {
            classEntity.setCourse(courseRepository.findById(req.getCourseId())
                    .orElseThrow(() -> new IllegalArgumentException("课程不存在")));
        }
        if (req.getTeacherId() != null) {
            classEntity.setTeacher(teacherRepository.findById(req.getTeacherId())
                    .orElseThrow(() -> new IllegalArgumentException("教师不存在")));
        }
        if (req.getSemester() != null) classEntity.setSemester(req.getSemester());
        if (req.getSchedule() != null) classEntity.setSchedule(req.getSchedule());
        if (req.getCapacity() != null) classEntity.setCapacity(req.getCapacity());
        classRepository.save(classEntity);
        return ResponseEntity.ok(toMap(classEntity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        classRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("开课不存在"));
        classRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    private Map<String, Object> toMap(ClassEntity c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("course_id", c.getCourse().getId());
        m.put("course_name", c.getCourse().getName());
        m.put("course_code", c.getCourse().getCode());
        m.put("credits", c.getCourse().getCredits());
        m.put("teacher_id", c.getTeacher().getId());
        m.put("teacher_name", c.getTeacher().getUser().getName());
        m.put("semester", c.getSemester());
        m.put("schedule", c.getSchedule());
        m.put("capacity", c.getCapacity());
        return m;
    }
}
