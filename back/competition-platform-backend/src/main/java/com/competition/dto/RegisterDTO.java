package com.competition.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    // 通用字段
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;

    // 学生特有字段
    private String studentNo;
    private String major;
    private String grade;
    private String className;
    private String skills;
    private String interests;
    private String honors;

    // 教师特有字段
    private String teacherNo;
    private String department;
    private String title;
    private String researchDirection;
}