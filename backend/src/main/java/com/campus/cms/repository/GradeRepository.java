package com.campus.cms.repository;

import com.campus.cms.entity.Enrollment;
import com.campus.cms.entity.Grade;
import com.campus.cms.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    Optional<Grade> findByEnrollment(Enrollment enrollment);

    @Query("SELECT g FROM Grade g WHERE g.enrollment.student = :student")
    List<Grade> findByStudent(Student student);
}
