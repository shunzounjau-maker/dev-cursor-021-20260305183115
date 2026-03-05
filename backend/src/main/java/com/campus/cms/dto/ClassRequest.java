package com.campus.cms.dto;

import lombok.Data;

@Data
public class ClassRequest {
    private Long courseId;
    private Long teacherId;
    private String semester;
    private String schedule;
    private Integer capacity;
}
