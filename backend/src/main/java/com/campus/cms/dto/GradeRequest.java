package com.campus.cms.dto;

import lombok.Data;

@Data
public class GradeRequest {
    private Long enrollmentId;
    private Integer score;
}
