package com.competition.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("teacher_detail")
public class TeacherDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String teacherNo;
    private String department;
    private String title;
    private String researchDirection;
    private Integer maxTeams;
    private Integer currentTeams;
    private Integer isAvailable;
}