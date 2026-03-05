package com.campus.cms.repository;

import com.campus.cms.entity.ClassEntity;
import com.campus.cms.entity.Enrollment;
import com.campus.cms.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudent(Student student);
    List<Enrollment> findByClassEntity(ClassEntity classEntity);
    Optional<Enrollment> findByClassEntityAndStudent(ClassEntity classEntity, Student student);
    boolean existsByClassEntityAndStudent(ClassEntity classEntity, Student student);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.classEntity = :classEntity")
    long countByClassEntity(ClassEntity classEntity);

    @Query("SELECT SUM(c.course.credits) FROM Enrollment e JOIN e.classEntity c WHERE e.student = :student AND c.semester = :semester")
    Integer sumCreditsByStudentAndSemester(Student student, String semester);
}
