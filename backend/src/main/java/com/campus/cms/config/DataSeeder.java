package com.campus.cms.config;

import com.campus.cms.entity.*;
import com.campus.cms.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final ClassRepository classRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public DataSeeder(UserRepository userRepository, TeacherRepository teacherRepository,
                      StudentRepository studentRepository, CourseRepository courseRepository,
                      ClassRepository classRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.classRepository = classRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run(String... args) throws Exception {
        String seedDir = System.getProperty("seed.dir", "/workspace/data/021/seed");
        File seedDirFile = new File(seedDir);
        if (!seedDirFile.exists()) {
            log.info("Seed directory not found, skipping seed data import.");
            return;
        }

        seedUsers(new File(seedDir, "users.json"));
        seedTeachers(new File(seedDir, "teachers.json"));
        seedStudents(new File(seedDir, "students.json"));
        seedCourses(new File(seedDir, "courses.json"));
        seedClasses(new File(seedDir, "classes.json"));

        log.info("Seed data import completed.");
    }

    private void seedUsers(File file) throws Exception {
        if (!file.exists()) return;
        List<Map<String, Object>> users = objectMapper.readValue(file, new TypeReference<>() {});
        for (Map<String, Object> u : users) {
            String username = (String) u.get("username");
            if (!userRepository.existsByUsername(username)) {
                User user = new User();
                user.setUsername(username);
                user.setPasswordHash(passwordEncoder.encode((String) u.get("password")));
                user.setRole(User.Role.valueOf((String) u.get("role")));
                user.setName((String) u.get("name"));
                user.setEmail((String) u.get("email"));
                userRepository.save(user);
                log.info("Created user: {}", username);
            }
        }
    }

    private void seedTeachers(File file) throws Exception {
        if (!file.exists()) return;
        List<Map<String, Object>> teachers = objectMapper.readValue(file, new TypeReference<>() {});
        for (Map<String, Object> t : teachers) {
            String staffNo = (String) t.get("staff_no");
            if (!teacherRepository.existsByStaffNo(staffNo)) {
                String username = (String) t.get("username");
                User user = userRepository.findByUsername(username).orElse(null);
                if (user == null) continue;
                Teacher teacher = new Teacher();
                teacher.setUser(user);
                teacher.setStaffNo(staffNo);
                teacher.setDepartment((String) t.get("department"));
                teacherRepository.save(teacher);
                log.info("Created teacher: {}", staffNo);
            }
        }
    }

    private void seedStudents(File file) throws Exception {
        if (!file.exists()) return;
        List<Map<String, Object>> students = objectMapper.readValue(file, new TypeReference<>() {});
        for (Map<String, Object> s : students) {
            String studentNo = (String) s.get("student_no");
            if (!studentRepository.existsByStudentNo(studentNo)) {
                String username = (String) s.get("username");
                User user = userRepository.findByUsername(username).orElse(null);
                if (user == null) continue;
                Student student = new Student();
                student.setUser(user);
                student.setStudentNo(studentNo);
                student.setGrade((String) s.get("grade"));
                student.setClassName((String) s.get("class_name"));
                studentRepository.save(student);
                log.info("Created student: {}", studentNo);
            }
        }
    }

    private void seedCourses(File file) throws Exception {
        if (!file.exists()) return;
        List<Map<String, Object>> courses = objectMapper.readValue(file, new TypeReference<>() {});
        for (Map<String, Object> c : courses) {
            String code = (String) c.get("code");
            if (!courseRepository.existsByCode(code)) {
                Course course = new Course();
                course.setCode(code);
                course.setName((String) c.get("name"));
                course.setCredits((Integer) c.get("credits"));
                course.setDescription((String) c.get("description"));
                courseRepository.save(course);
                log.info("Created course: {}", code);
            }
        }
    }

    private void seedClasses(File file) throws Exception {
        if (!file.exists()) return;
        List<Map<String, Object>> classes = objectMapper.readValue(file, new TypeReference<>() {});
        for (Map<String, Object> cl : classes) {
            String courseCode = (String) cl.get("course_code");
            String teacherStaffNo = (String) cl.get("teacher_staff_no");
            String semester = (String) cl.get("semester");

            Course course = courseRepository.findByCode(courseCode).orElse(null);
            Teacher teacher = teacherRepository.findByStaffNo(teacherStaffNo).orElse(null);
            if (course == null || teacher == null) continue;

            boolean exists = classRepository.findByTeacher(teacher).stream()
                    .anyMatch(c -> c.getCourse().getCode().equals(courseCode) && c.getSemester().equals(semester));
            if (!exists) {
                ClassEntity classEntity = new ClassEntity();
                classEntity.setCourse(course);
                classEntity.setTeacher(teacher);
                classEntity.setSemester(semester);
                classEntity.setSchedule((String) cl.get("schedule"));
                Object cap = cl.get("capacity");
                if (cap instanceof Integer) classEntity.setCapacity((Integer) cap);
                classRepository.save(classEntity);
                log.info("Created class: {} - {}", courseCode, semester);
            }
        }
    }
}
