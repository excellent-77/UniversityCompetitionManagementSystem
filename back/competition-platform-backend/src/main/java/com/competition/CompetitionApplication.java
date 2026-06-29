package com.competition;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.competition.mapper")
public class CompetitionApplication {
    public static void main(String[] args) {
        SpringApplication.run(CompetitionApplication.class, args);
        System.out.println("========================================");
        System.out.println("高校竞赛管理平台启动成功！");
        System.out.println("访问地址: http://localhost:8080/api");
        System.out.println("========================================");
    }
}