package com.campus.cms.dto;

import lombok.Data;

@Data
public class TeacherRequest {
    private String username;
    private String password;
    private String name;
    private String staffNo;
    private String department;
    private String email;
}
