package com.competition.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.competition.entity.CompetitionRegistration;
import com.competition.mapper.CompetitionRegistrationMapper;
import org.springframework.stereotype.Service;

@Service
public class CompetitionRegistrationService extends ServiceImpl<CompetitionRegistrationMapper, CompetitionRegistration> {
}