-- =============================================================
-- 在线考试系统（控制台版）数据库脚本
-- MySQL 8.0 / utf8mb4
-- 建库、建表、初始数据
-- =============================================================

DROP DATABASE IF EXISTS exam_db;
CREATE DATABASE exam_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE exam_db;

-- ---------- 用户表 ----------
CREATE TABLE `user` (
  `user_id`      INT          NOT NULL AUTO_INCREMENT,
  `username`     VARCHAR(50)  NOT NULL,
  `password`     VARCHAR(64)  NOT NULL,
  `real_name`    VARCHAR(50)  NOT NULL,
  `role`         VARCHAR(10)  NOT NULL COMMENT 'TEACHER / STUDENT',
  `student_no`   VARCHAR(20)  DEFAULT NULL COMMENT '学号（学生）',
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ---------- 题目表 ----------
CREATE TABLE `question` (
  `question_id`   INT           NOT NULL AUTO_INCREMENT,
  `question_type` VARCHAR(10)   NOT NULL COMMENT 'SINGLE / MULTIPLE / JUDGE',
  `content`       VARCHAR(500)  NOT NULL COMMENT '题干',
  `option_a`      VARCHAR(200)  DEFAULT NULL,
  `option_b`      VARCHAR(200)  DEFAULT NULL,
  `option_c`      VARCHAR(200)  DEFAULT NULL,
  `option_d`      VARCHAR(200)  DEFAULT NULL,
  `correct_answer` VARCHAR(10)  NOT NULL COMMENT '正确答案，判断题存 T/F，多选存字母组合',
  `score`         DECIMAL(5,1)  NOT NULL DEFAULT 5.0 COMMENT '默认分值',
  `difficulty`    TINYINT       NOT NULL DEFAULT 1 COMMENT '难度 1-3',
  `created_by`    INT           DEFAULT NULL,
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`question_id`),
  KEY `idx_type` (`question_type`),
  KEY `idx_difficulty` (`difficulty`),
  CONSTRAINT `fk_question_user` FOREIGN KEY (`created_by`) REFERENCES `user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '题目表';

-- ---------- 试卷表 ----------
CREATE TABLE `exam_paper` (
  `paper_id`        INT           NOT NULL AUTO_INCREMENT,
  `paper_name`      VARCHAR(100)  NOT NULL,
  `subject`         VARCHAR(50)   NOT NULL,
  `total_score`     DECIMAL(6,1)  NOT NULL DEFAULT 100.0,
  `duration_minutes` INT          NOT NULL DEFAULT 30 COMMENT '考试时长（分钟）',
  `status`          VARCHAR(10)   NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT / PUBLISHED',
  `created_by`      INT           DEFAULT NULL,
  `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`paper_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_paper_user` FOREIGN KEY (`created_by`) REFERENCES `user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '试卷表';

-- ---------- 试卷题目关联表 ----------
CREATE TABLE `paper_question` (
  `id`              INT           NOT NULL AUTO_INCREMENT,
  `paper_id`        INT           NOT NULL,
  `question_id`     INT           NOT NULL,
  `question_order`  INT           NOT NULL COMMENT '题序',
  `score`           DECIMAL(5,1)  NOT NULL DEFAULT 5.0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_paper_question` (`paper_id`, `question_id`),
  KEY `idx_question` (`question_id`),
  CONSTRAINT `fk_pq_paper`    FOREIGN KEY (`paper_id`)    REFERENCES `exam_paper` (`paper_id`),
  CONSTRAINT `fk_pq_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`question_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '试卷题目关联表';

-- ---------- 考试会话表 ----------
CREATE TABLE `exam_session` (
  `session_id`  INT          NOT NULL AUTO_INCREMENT,
  `paper_id`    INT          NOT NULL,
  `student_id`  INT          NOT NULL,
  `start_time`  DATETIME     NOT NULL,
  `end_time`    DATETIME     NOT NULL,
  `submit_time` DATETIME     DEFAULT NULL,
  `status`      VARCHAR(20)  NOT NULL DEFAULT 'IN_PROGRESS' COMMENT 'IN_PROGRESS / SUBMITTED / TIMEOUT',
  `total_score` DECIMAL(6,1) DEFAULT NULL,
  `passed`      TINYINT      DEFAULT NULL COMMENT '是否通过',
  PRIMARY KEY (`session_id`),
  KEY `idx_paper` (`paper_id`),
  KEY `idx_student` (`student_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_session_paper`  FOREIGN KEY (`paper_id`)   REFERENCES `exam_paper` (`paper_id`),
  CONSTRAINT `fk_session_user`   FOREIGN KEY (`student_id`) REFERENCES `user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '考试会话表';

-- ---------- 答题明细表 ----------
CREATE TABLE `answer_record` (
  `record_id`   INT           NOT NULL AUTO_INCREMENT,
  `session_id`  INT           NOT NULL,
  `question_id` INT           NOT NULL,
  `user_answer` VARCHAR(50)   NOT NULL,
  `is_correct`  TINYINT       NOT NULL DEFAULT 0,
  `score`       DECIMAL(5,1)  NOT NULL DEFAULT 0.0,
  PRIMARY KEY (`record_id`),
  UNIQUE KEY `uk_session_question` (`session_id`, `question_id`),
  KEY `idx_question` (`question_id`),
  CONSTRAINT `fk_ar_session`  FOREIGN KEY (`session_id`)  REFERENCES `exam_session` (`session_id`),
  CONSTRAINT `fk_ar_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`question_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '答题明细表';

-- =============================================================
-- 初始数据
-- 密码加密方式：MD5(明文 + salt)，salt = group_exam_salt
-- 初始密码统一为 123456，对应摘要：add505e76493a513be857aba6ca81c2f
-- =============================================================
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `student_no`) VALUES
('admin',  'add505e76493a513be857aba6ca81c2f', '系统管理员', 'TEACHER', NULL),
('stu001', 'add505e76493a513be857aba6ca81c2f', '张三', 'STUDENT', '20260001'),
('stu002', 'add505e76493a513be857aba6ca81c2f', '李四', 'STUDENT', '20260002');

INSERT INTO `question`
(`question_type`, `content`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `score`, `difficulty`, `created_by`) VALUES
('SINGLE',  'Java 中基本数据类型 int 的默认值是？',     '0',     '1',     'null',    '不确定', 'A',   5, 1, 1),
('SINGLE',  '以下哪个类位于 java.util 包中？',         'String', 'ArrayList', 'System', 'Math', 'B',   5, 1, 1),
('SINGLE',  '下列关键字中用于定义接口的是？',           'class',  'interface', 'extends', 'new',  'B',   5, 1, 1),
('MULTIPLE','下列哪些属于 Java 面向对象的基本特征？',   '封装',   '继承',     '多态',     '反射', 'ABC', 10, 2, 1),
('MULTIPLE','以下哪些是合法的 Java 标识符？',           '_name',  '2name',    'class',    '$value', 'AD', 10, 2, 1),
('JUDGE',   'Java 中 == 用于比较两个对象的引用是否相同。', NULL, NULL, NULL, NULL, 'T',   5, 1, 1),
('JUDGE',   'ArrayList 与 LinkedList 的随机查询效率相同。', NULL, NULL, NULL, NULL, 'F',   5, 1, 1);
