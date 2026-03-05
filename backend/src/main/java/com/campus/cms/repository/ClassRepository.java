package com.campus.cms.repository;

import com.campus.cms.entity.ClassEntity;
import com.campus.cms.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClassRepository extends JpaRepository<ClassEntity, Long> {
    List<ClassEntity> findByTeacher(Teacher teacher);
    List<ClassEntity> findBySemester(String semester);
}
