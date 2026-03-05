package com.campus.cms.repository;

import com.campus.cms.entity.Student;
import com.campus.cms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUser(User user);
    Optional<Student> findByStudentNo(String studentNo);
    boolean existsByStudentNo(String studentNo);
}
