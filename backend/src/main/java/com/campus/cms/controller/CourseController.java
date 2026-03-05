package com.campus.cms.controller;

import com.campus.cms.entity.Course;
import com.campus.cms.repository.CourseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseRepository courseRepository;

    public CourseController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return courseRepository.findAll().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return courseRepository.findById(id)
                .map(c -> ResponseEntity.ok(toMap(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Course req) {
        if (courseRepository.existsByCode(req.getCode())) {
            throw new IllegalStateException("课程代码已存在");
        }
        Course course = new Course();
        course.setCode(req.getCode());
        course.setName(req.getName());
        course.setCredits(req.getCredits());
        course.setDescription(req.getDescription());
        courseRepository.save(course);
        return ResponseEntity.ok(toMap(course));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Course req) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在"));
        if (req.getName() != null) course.setName(req.getName());
        if (req.getCredits() != null) course.setCredits(req.getCredits());
        if (req.getDescription() != null) course.setDescription(req.getDescription());
        courseRepository.save(course);
        return ResponseEntity.ok(toMap(course));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在"));
        courseRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    private Map<String, Object> toMap(Course c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("code", c.getCode());
        m.put("name", c.getName());
        m.put("credits", c.getCredits());
        m.put("description", c.getDescription());
        return m;
    }
}
