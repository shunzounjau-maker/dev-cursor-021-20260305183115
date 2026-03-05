package com.campus.cms.repository;

import com.campus.cms.entity.Teacher;
import com.campus.cms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByUser(User user);
    Optional<Teacher> findByStaffNo(String staffNo);
    boolean existsByStaffNo(String staffNo);
}
