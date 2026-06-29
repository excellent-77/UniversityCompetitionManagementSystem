package com.competition.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("guidance_relation")
public class GuidanceRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long teacherId;
    private Long teamId;
    private Integer status;  // 0-待确认, 1-已接受, 2-已拒绝, 3-已结束
    private String applyReason;
    private String teacherComment;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}