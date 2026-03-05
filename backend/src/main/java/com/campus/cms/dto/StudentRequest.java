package com.campus.cms.dto;

import lombok.Data;

@Data
public class StudentRequest {
    private String username;
    private String password;
    private String name;
    private String studentNo;
    private String grade;
    private String className;
    private String email;
}
