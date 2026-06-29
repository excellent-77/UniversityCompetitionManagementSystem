package com.competition.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("team_recruitment")
public class TeamRecruitment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long teamId;
    private String needSkills;
    private Integer needCount;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
}