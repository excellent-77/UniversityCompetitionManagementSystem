package com.competition.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("student_detail")
public class StudentDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String studentNo;
    private String major;
    private String grade;
    private String className;
    private String skills;
    private String interests;
    private String honors;
}