/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 80029
Source Host           : localhost:3306
Source Database       : competition_platform

Target Server Type    : MYSQL
Target Server Version : 80029
File Encoding         : 65001

Date: 2026-04-15 18:47:42
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for announcement
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `content` longtext COLLATE utf8mb4_unicode_ci COMMENT '公告内容',
  `is_top` tinyint DEFAULT '0' COMMENT '是否置顶: 0-否, 1-是',
  `status` tinyint DEFAULT '1' COMMENT '状态: 0-下架, 1-发布中',
  `publish_user_id` bigint DEFAULT NULL COMMENT '发布人ID',
  `publish_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_is_top` (`is_top`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统公告表';

-- ----------------------------
-- Records of announcement
-- ----------------------------
INSERT INTO `announcement` VALUES ('1', '2026年春季竞赛报名通知', '<p>各位同学：2026年春季学期各项竞赛已经开始报名，请同学们积极关注并踊跃参加！</p><p>报名截止日期：2026年4月30日</p>', '1', '1', '1', '2026-03-24 19:18:18', '2026-03-24 19:18:18');
INSERT INTO `announcement` VALUES ('2', '关于竞赛平台使用说明', '<p>为方便师生使用竞赛管理平台，现发布使用说明文档。</p><p>如有问题请联系管理员。</p>', '0', '1', '1', '2026-03-24 19:18:18', '2026-03-24 19:18:18');

-- ----------------------------
-- Table structure for competition
-- ----------------------------
DROP TABLE IF EXISTS `competition`;
CREATE TABLE `competition` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '竞赛ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '竞赛名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '竞赛简介',
  `content` longtext COLLATE utf8mb4_unicode_ci COMMENT '详细内容(富文本)',
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '竞赛类别',
  `required_skills` text COLLATE utf8mb4_unicode_ci COMMENT '所需技能标签(JSON数组)',
  `start_time` datetime DEFAULT NULL COMMENT '报名开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '报名结束时间',
  `competition_time` datetime DEFAULT NULL COMMENT '竞赛开始时间',
  `location` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '竞赛地点',
  `max_team_members` int DEFAULT '5' COMMENT '最大队员数',
  `min_team_members` int DEFAULT '1' COMMENT '最小队员数',
  `status` tinyint DEFAULT '0' COMMENT '状态: 0-未开始, 1-报名中, 2-进行中, 3-已结束',
  `publish_user_id` bigint DEFAULT NULL COMMENT '发布人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_category` (`category`),
  KEY `publish_user_id` (`publish_user_id`),
  CONSTRAINT `competition_ibfk_1` FOREIGN KEY (`publish_user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞赛表';

-- ----------------------------
-- Records of competition
-- ----------------------------
INSERT INTO `competition` VALUES ('1', '2026年全国大学生软件设计大赛', '全国大学生软件设计大赛是一项面向全国高校学生的软件设计竞赛，旨在培养学生的创新能力和实践能力。', '<h3>竞赛详情</h3><p>全国大学生软件设计大赛是一项面向全国高校学生的软件设计竞赛，旨在培养学生的创新能力和实践能力。</p><h3>参赛要求</h3><p>1. 每队3-5人</p><p>2. 提交完整项目作品</p><p>3. 需要现场答辩</p>', '程序设计', '[\"Java\",\"Spring Boot\",\"Vue\",\"MySQL\",\"微服务\"]', '2026-03-01 00:00:00', '2026-04-15 23:59:59', '2026-05-20 09:00:00', '待定', '5', '3', '1', '2', '2026-03-24 19:18:17', '2026-04-05 21:50:54');
INSERT INTO `competition` VALUES ('2', '2026年全国大学生数学建模竞赛', '全国大学生数学建模竞赛是教育部认可的全国性学科竞赛，培养学生运用数学方法解决实际问题的能力。', '<h3>竞赛详情</h3><p>全国大学生数学建模竞赛是教育部认可的全国性学科竞赛，培养学生运用数学方法解决实际问题的能力。</p><h3>竞赛形式</h3><p>3人一组，在72小时内完成建模论文。</p>', '数学建模', '[\"Python\",\"MATLAB\",\"数学建模\",\"数据分析\",\"统计学\"]', '2026-04-01 00:00:00', '2026-05-10 23:59:59', '2026-05-20 18:00:00', '线上', '3', '3', '1', '2', '2026-03-24 19:18:17', '2026-03-24 19:18:17');
INSERT INTO `competition` VALUES ('3', '第15届“挑战杯”大学生创业计划大赛', '“挑战杯”大学生创业计划大赛是培养大学生创新创业能力的重要赛事。', '<h3>大赛简介</h3><p>“挑战杯”大学生创业计划大赛是培养大学生创新创业能力的重要赛事。</p><h3>参赛要求</h3><p>提交商业计划书，进行路演答辩。</p>', '创新创业', '[\"商业计划\",\"市场营销\",\"财务管理\",\"产品设计\"]', '2026-03-15 00:00:00', '2026-04-30 23:59:59', '2026-06-10 09:00:00', '大学生活动中心', '8', '3', '1', '3', '2026-03-24 19:18:17', '2026-03-24 19:18:17');
INSERT INTO `competition` VALUES ('4', '2026年ACM-ICPC国际大学生程序设计竞赛', 'ACM-ICPC是全球最具影响力的大学生程序设计竞赛。', '<h3>竞赛介绍</h3><p>ACM-ICPC是全球最具影响力的大学生程序设计竞赛，考验算法设计与编程能力。</p><h3>竞赛规则</h3><p>3人一队，5小时解决10-13道编程题。</p>', '程序设计', '[\"C++\",\"算法\",\"数据结构\",\"图论\",\"动态规划\"]', '2026-05-01 00:00:00', '2026-06-15 23:59:59', '2026-07-15 12:00:00', '待定', '3', '3', '0', '2', '2026-03-24 19:18:17', '2026-04-05 21:51:13');
INSERT INTO `competition` VALUES ('5', '2026年人工智能创新应用大赛', '人工智能创新应用大赛聚焦AI技术在垂直领域的创新应用。', '<h3>大赛主题</h3><p>人工智能赋能千行百业</p><h3>作品要求</h3><p>提交AI应用原型及技术报告。</p>', '人工智能', '[\"Python\",\"TensorFlow\",\"PyTorch\",\"机器学习\",\"深度学习\"]', '2026-04-01 00:00:00', '2026-05-20 23:59:59', '2026-06-20 09:00:00', '待定', '5', '2', '1', '3', '2026-03-24 19:18:17', '2026-04-05 21:51:21');
INSERT INTO `competition` VALUES ('6', '2026年全国大学生英语竞赛', '全国大学生英语竞赛是全国性英语综合能力竞赛。', '<h3>竞赛介绍</h3><p>全国大学生英语竞赛是全国性英语综合能力竞赛，分为初赛和决赛。</p>', '英语竞赛', '[\"英语\",\"翻译\",\"写作\",\"口语\"]', '2026-03-01 00:00:00', '2026-04-10 23:59:59', '2026-04-25 14:00:00', '待定', '1', '1', '2', '2', '2026-03-24 19:18:17', '2026-04-05 21:51:27');

-- ----------------------------
-- Table structure for competition_registration
-- ----------------------------
DROP TABLE IF EXISTS `competition_registration`;
CREATE TABLE `competition_registration` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `competition_id` bigint NOT NULL COMMENT '竞赛ID',
  `team_id` bigint NOT NULL COMMENT '团队ID',
  `status` tinyint DEFAULT '0' COMMENT '状态: 0-待审核, 1-审核通过, 2-审核拒绝, 3-已取消',
  `submit_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_comment` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核意见',
  PRIMARY KEY (`id`),
  KEY `idx_competition_id` (`competition_id`),
  KEY `idx_team_id` (`team_id`),
  CONSTRAINT `competition_registration_ibfk_1` FOREIGN KEY (`competition_id`) REFERENCES `competition` (`id`),
  CONSTRAINT `competition_registration_ibfk_2` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞赛报名表';

-- ----------------------------
-- Records of competition_registration
-- ----------------------------
INSERT INTO `competition_registration` VALUES ('1', '1', '1', '0', '2026-04-05 17:08:25', null, null);

-- ----------------------------
-- Table structure for guidance_relation
-- ----------------------------
DROP TABLE IF EXISTS `guidance_relation`;
CREATE TABLE `guidance_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `teacher_id` bigint NOT NULL COMMENT '教师ID',
  `team_id` bigint NOT NULL COMMENT '团队ID',
  `status` tinyint DEFAULT '0' COMMENT '状态: 0-待确认, 1-已接受, 2-已拒绝, 3-已结束',
  `apply_reason` text COLLATE utf8mb4_unicode_ci COMMENT '申请理由',
  `teacher_comment` text COLLATE utf8mb4_unicode_ci COMMENT '教师回复',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_teacher_team` (`teacher_id`,`team_id`),
  KEY `idx_teacher_id` (`teacher_id`),
  KEY `idx_team_id` (`team_id`),
  CONSTRAINT `guidance_relation_ibfk_1` FOREIGN KEY (`teacher_id`) REFERENCES `user` (`id`),
  CONSTRAINT `guidance_relation_ibfk_2` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='师生指导关系表';

-- ----------------------------
-- Records of guidance_relation
-- ----------------------------
INSERT INTO `guidance_relation` VALUES ('1', '2', '1', '1', '11', '', '2026-03-26 12:58:34', '2026-03-26 14:18:09');
INSERT INTO `guidance_relation` VALUES ('2', '3', '1', '3', '11', null, '2026-03-26 13:02:53', '2026-03-26 13:22:07');

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知标题',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '通知内容',
  `type` tinyint DEFAULT '0' COMMENT '类型: 0-系统通知, 1-组队邀请, 2-指导邀请, 3-竞赛通知',
  `related_id` bigint DEFAULT NULL COMMENT '关联业务ID',
  `is_read` tinyint DEFAULT '0' COMMENT '是否已读: 0-未读, 1-已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知消息表';

-- ----------------------------
-- Records of notification
-- ----------------------------
INSERT INTO `notification` VALUES ('1', '4', '欢迎加入竞赛平台', '欢迎使用高校竞赛管理平台！您可以在这里查看竞赛信息、组建团队、申请指导。', '0', null, '1', '2026-03-24 19:18:18');
INSERT INTO `notification` VALUES ('2', '5', '欢迎加入竞赛平台', '欢迎使用高校竞赛管理平台！您可以在这里查看竞赛信息、组建团队、申请指导。', '0', null, '0', '2026-03-24 19:18:18');
INSERT INTO `notification` VALUES ('3', '6', '欢迎加入竞赛平台', '欢迎使用高校竞赛管理平台！您可以在这里查看竞赛信息、组建团队、申请指导。', '0', null, '0', '2026-03-24 19:18:18');
INSERT INTO `notification` VALUES ('5', '4', '团队加入申请', '肚小气 申请加入您的团队 代码先锋队', '1', '1', '1', '2026-03-24 23:15:39');
INSERT INTO `notification` VALUES ('7', '2', '团队指导申请', '团队 代码先锋队 申请您的指导', '2', '1', '1', '2026-03-26 12:58:34');
INSERT INTO `notification` VALUES ('8', '3', '团队指导申请', '团队 代码先锋队 申请您的指导', '2', '1', '0', '2026-03-26 13:02:53');
INSERT INTO `notification` VALUES ('9', '6', '团队加入申请', '有用户申请加入您的团队【AI探索者】，请及时处理', '1', '3', '0', '2026-03-26 13:06:11');
INSERT INTO `notification` VALUES ('10', '2', '指导申请通知', '团队【代码先锋队】申请您的指导，请及时处理', '2', '1', '1', '2026-03-26 14:14:17');
INSERT INTO `notification` VALUES ('11', '4', '指导申请已通过', '教师【张浩明】已接受您的指导申请', '2', '1', '1', '2026-03-26 14:18:10');
INSERT INTO `notification` VALUES ('12', '4', '团队加入申请', '肚小气 申请加入您的团队 代码先锋队', '1', '1', '1', '2026-04-05 19:58:04');
INSERT INTO `notification` VALUES ('13', '9', '加入团队申请已通过', '恭喜！您已成功加入团队【代码先锋队】', '1', '1', '1', '2026-04-05 19:58:15');

-- ----------------------------
-- Table structure for student_detail
-- ----------------------------
DROP TABLE IF EXISTS `student_detail`;
CREATE TABLE `student_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `student_no` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学号',
  `major` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '专业',
  `grade` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '年级',
  `class_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '班级',
  `skills` text COLLATE utf8mb4_unicode_ci COMMENT '技能标签(JSON数组)',
  `interests` text COLLATE utf8mb4_unicode_ci COMMENT '竞赛兴趣方向',
  `honors` text COLLATE utf8mb4_unicode_ci COMMENT '获奖经历',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `student_detail_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生详细信息表';

-- ----------------------------
-- Records of student_detail
-- ----------------------------
INSERT INTO `student_detail` VALUES ('1', '4', '2024001', '软件工程', '2024级', '软件工程1班', '[\"Java\",\"Spring Boot\",\"MySQL\",\"Vue\"]', '[\"程序设计\",\"创新创业\"]', '2024校程序设计大赛二等奖');
INSERT INTO `student_detail` VALUES ('2', '5', '2024002', '计算机科学与技术', '2024级', '计科2班', '[\"Python\",\"机器学习\",\"数据分析\",\"MySQL\"]', '[\"数学建模\",\"人工智能\"]', '全国大学生数学建模竞赛省一等奖');
INSERT INTO `student_detail` VALUES ('3', '6', '2024003', '人工智能', '2024级', '人工智能1班', '[\"Python\",\"TensorFlow\",\"深度学习\",\"算法\"]', '[\"人工智能\",\"数据挖掘\"]', 'Kaggle比赛Top10%');
INSERT INTO `student_detail` VALUES ('4', '7', '2024004', '软件工程', '2024级', '软件工程2班', '[\"Java\",\"前端开发\",\"React\",\"Node.js\"]', '[\"创新创业\",\"Web开发\"]', '互联网+大赛校金奖');
INSERT INTO `student_detail` VALUES ('5', '8', '2024005', '数据科学与大数据技术', '2024级', '大数据1班', '[\"Python\",\"数据挖掘\",\"Hadoop\",\"Spark\"]', '[\"大数据\",\"数据挖掘\"]', '全国大数据挑战赛三等奖');
INSERT INTO `student_detail` VALUES ('6', '9', '2024006', '软件工程', '2024级', '软件工程2班', '[\"Java\",\"数据结构\",\"后端开发\",\"Spring Boot\"]', '[\"程序设计\"]', '');

-- ----------------------------
-- Table structure for teacher_detail
-- ----------------------------
DROP TABLE IF EXISTS `teacher_detail`;
CREATE TABLE `teacher_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `teacher_no` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '教师工号',
  `department` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属学院/系',
  `title` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '职称',
  `research_direction` text COLLATE utf8mb4_unicode_ci COMMENT '研究方向',
  `max_teams` int DEFAULT '5' COMMENT '最多可指导队伍数',
  `current_teams` int DEFAULT '0' COMMENT '当前指导队伍数',
  `is_available` tinyint DEFAULT '1' COMMENT '是否可指导: 0-否, 1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `teacher_detail_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师详细信息表';

-- ----------------------------
-- Records of teacher_detail
-- ----------------------------
INSERT INTO `teacher_detail` VALUES ('1', '2', 'T2024001', '计算机学院', '副教授', '[\"软件工程\",\"人工智能\",\"数据挖掘\"]', '4', '1', '1');
INSERT INTO `teacher_detail` VALUES ('2', '3', 'T2024002', '计算机学院', '教授', '[\"机器学习\",\"深度学习\",\"计算机视觉\"]', '5', '0', '1');

-- ----------------------------
-- Table structure for team
-- ----------------------------
DROP TABLE IF EXISTS `team`;
CREATE TABLE `team` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '团队ID',
  `team_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '团队名称',
  `competition_id` bigint NOT NULL COMMENT '所属竞赛ID',
  `leader_id` bigint NOT NULL COMMENT '队长ID(学生)',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '团队简介',
  `status` tinyint DEFAULT '0' COMMENT '状态: 0-招募中, 1-已满员, 2-已解散',
  `max_members` int DEFAULT '5' COMMENT '最大人数',
  `current_members` int DEFAULT '1' COMMENT '当前人数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_competition_id` (`competition_id`),
  KEY `idx_leader_id` (`leader_id`),
  CONSTRAINT `team_ibfk_1` FOREIGN KEY (`competition_id`) REFERENCES `competition` (`id`) ON DELETE CASCADE,
  CONSTRAINT `team_ibfk_2` FOREIGN KEY (`leader_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队表';

-- ----------------------------
-- Records of team
-- ----------------------------
INSERT INTO `team` VALUES ('1', '代码先锋队', '1', '4', '我们是一群热爱编程的开发者，擅长Java和Spring Boot，期待志同道合的队友！', '0', '5', '2', '2026-03-24 19:18:17', '2026-04-05 19:14:37');
INSERT INTO `team` VALUES ('2', '数学之星', '2', '5', '数学建模爱好者，擅长Python数据分析，寻找建模伙伴！', '0', '3', '1', '2026-03-24 19:18:17', '2026-03-24 19:18:17');
INSERT INTO `team` VALUES ('3', 'AI探索者', '5', '6', '深度学习爱好者，有Kaggle经验，欢迎AI方向的同学加入！', '0', '5', '1', '2026-03-24 19:18:17', '2026-03-24 19:18:17');

-- ----------------------------
-- Table structure for team_member
-- ----------------------------
DROP TABLE IF EXISTS `team_member`;
CREATE TABLE `team_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `team_id` bigint NOT NULL COMMENT '团队ID',
  `user_id` bigint NOT NULL COMMENT '成员用户ID',
  `role` tinyint DEFAULT '0' COMMENT '角色: 0-队员, 1-队长',
  `join_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint DEFAULT '1' COMMENT '状态: 0-已退出, 1-正常, 2-待审核',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_user` (`team_id`,`user_id`),
  KEY `idx_team_id` (`team_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `team_member_ibfk_1` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`) ON DELETE CASCADE,
  CONSTRAINT `team_member_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队成员关系表';

-- ----------------------------
-- Records of team_member
-- ----------------------------
INSERT INTO `team_member` VALUES ('1', '1', '4', '1', '2026-03-24 19:18:17', '1');
INSERT INTO `team_member` VALUES ('2', '2', '5', '1', '2026-03-24 19:18:17', '1');
INSERT INTO `team_member` VALUES ('3', '3', '6', '1', '2026-03-24 19:18:17', '1');
INSERT INTO `team_member` VALUES ('7', '3', '4', '0', '2026-03-26 13:06:11', '2');
INSERT INTO `team_member` VALUES ('14', '1', '9', '0', '2026-04-05 19:58:04', '1');

-- ----------------------------
-- Table structure for team_recruitment
-- ----------------------------
DROP TABLE IF EXISTS `team_recruitment`;
CREATE TABLE `team_recruitment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `team_id` bigint NOT NULL COMMENT '团队ID',
  `need_skills` text COLLATE utf8mb4_unicode_ci COMMENT '需要的技能(JSON)',
  `need_count` int DEFAULT '1' COMMENT '需要人数',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '招募说明',
  `status` tinyint DEFAULT '1' COMMENT '状态: 0-已关闭, 1-招募中',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_team_id` (`team_id`),
  CONSTRAINT `team_recruitment_ibfk_1` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队招募需求表';

-- ----------------------------
-- Records of team_recruitment
-- ----------------------------
INSERT INTO `team_recruitment` VALUES ('1', '1', '[\"Vue\",\"前端开发\",\"UI设计\"]', '2', '我们需要前端开发高手，熟悉Vue框架，有项目经验者优先！', '1', '2026-03-24 19:18:18');
INSERT INTO `team_recruitment` VALUES ('2', '1', '[\"MySQL\",\"数据库设计\"]', '1', '需要数据库设计高手，熟悉MySQL优化。', '1', '2026-03-24 19:18:18');
INSERT INTO `team_recruitment` VALUES ('3', '2', '[\"Python\",\"数据分析\",\"统计学\"]', '2', '寻找数据分析队友，有数学建模经验优先！', '1', '2026-03-24 19:18:18');
INSERT INTO `team_recruitment` VALUES ('4', '3', '[\"Python\",\"深度学习\",\"计算机视觉\"]', '3', 'AI方向队友，有项目经验者优先！', '1', '2026-03-24 19:18:18');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码(加密)',
  `real_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系方式',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `role` tinyint NOT NULL DEFAULT '2' COMMENT '角色: 0-管理员, 1-教师, 2-学生',
  `status` tinyint DEFAULT '1' COMMENT '状态: 0-禁用, 1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_username` (`username`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('1', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '13800000000', 'admin@competition.com', '0', '1', '2026-03-24 19:18:17', '2026-03-24 19:18:17');
INSERT INTO `user` VALUES ('2', 'teacher_zhang', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张浩明', '13800000001', 'zhangming@univ.edu', '1', '1', '2026-03-24 19:18:17', '2026-03-24 19:18:17');
INSERT INTO `user` VALUES ('3', 'teacher_li', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李芳', '13800000002', 'lifang@univ.edu', '1', '1', '2026-03-24 19:18:17', '2026-03-24 19:18:17');
INSERT INTO `user` VALUES ('4', 'student_wang', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王小明', '13800000003', 'wangxm@univ.edu', '2', '1', '2026-03-24 19:18:17', '2026-03-24 19:18:17');
INSERT INTO `user` VALUES ('5', 'student_zhao', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '赵丽颖', '13800000004', 'zhaoly@univ.edu', '2', '1', '2026-03-24 19:18:17', '2026-03-24 19:18:17');
INSERT INTO `user` VALUES ('6', 'student_liu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '刘强', '13800000005', 'liuqiang@univ.edu', '2', '1', '2026-03-24 19:18:17', '2026-03-24 19:18:17');
INSERT INTO `user` VALUES ('7', 'student_chen', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '陈思思', '13800000006', 'chensisi@univ.edu', '2', '1', '2026-03-24 19:18:17', '2026-03-24 21:46:23');
INSERT INTO `user` VALUES ('8', 'student_zhou', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '周杰', '13800000007', 'zhoujie@univ.edu', '2', '1', '2026-03-24 19:18:17', '2026-03-24 19:18:17');
INSERT INTO `user` VALUES ('9', 'ice', '$2a$10$Nx6nPkQkk0/Aa5k.kdLc/OKhiHHCzIgEkvBC5Y7/Ckbqu1IwQJzA2', '肚小气', '17751314701', '17751314701@163.com', '2', '1', '2026-03-24 21:34:06', '2026-03-26 10:46:29');
