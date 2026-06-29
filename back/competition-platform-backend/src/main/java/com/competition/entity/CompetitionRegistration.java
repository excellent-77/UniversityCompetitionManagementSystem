package com.competition.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("competition_registration")
public class CompetitionRegistration {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long competitionId;
    private Long teamId;
    private Integer status;
    private LocalDateTime submitTime;
    private LocalDateTime auditTime;
    private String auditComment;
}