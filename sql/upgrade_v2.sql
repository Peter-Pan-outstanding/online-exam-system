-- =============================================================
-- 在线考试系统 V2 增量升级脚本
-- 适用于已存在 V1 数据库（exam_db）的环境，无需重建库即可获得新功能字段
-- 使用方式：mysql -uroot -p exam_db < sql/upgrade_v2.sql
-- =============================================================
USE exam_db;

-- 题目表：新增答案解析字段（错题本 / 成绩单解析展示）
ALTER TABLE `question`
  ADD COLUMN `analysis` VARCHAR(500) DEFAULT NULL COMMENT '答案解析' AFTER `difficulty`;

-- 试卷表：新增最大考试次数与乱序开关（重考管理 / 题目选项乱序）
ALTER TABLE `exam_paper`
  ADD COLUMN `max_attempts` INT NOT NULL DEFAULT 1 COMMENT '最大考试次数（默认 1 次）' AFTER `duration_minutes`,
  ADD COLUMN `shuffle` TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用题目/选项乱序（1=是）' AFTER `max_attempts`;
