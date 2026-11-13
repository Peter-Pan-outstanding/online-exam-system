package com.group.exam.service;

import com.group.exam.common.BizException;
import com.group.exam.dao.DaoFactory;
import com.group.exam.dao.ExamSessionDao;
import com.group.exam.entity.ExamSession;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 成绩统计服务：学生个人成绩与教师班级统计。
 */
public class StatService {

    private final ExamSessionDao sessionDao = DaoFactory.examSessionDao();

    /**
     * 某学生全部考试记录。
     */
    public List<ExamSession> myScores(int studentId) {
        try {
            return sessionDao.findByStudentId(studentId);
        } catch (SQLException e) {
            throw new BizException("查询成绩失败：" + e.getMessage(), e);
        }
    }

    /**
     * 全部已结束考试记录。
     */
    public List<ExamSession> allFinished() {
        try {
            return sessionDao.findAllFinished();
        } catch (SQLException e) {
            throw new BizException("查询成绩失败：" + e.getMessage(), e);
        }
    }

    /**
     * 汇总统计：考试人次、平均分、最高/最低分、及格率、分数段分布。
     */
    public Map<String, Object> summary() {
        List<ExamSession> all = allFinished();
        Map<String, Object> stat = new LinkedHashMap<String, Object>();
        stat.put("count", all.size());
        if (all.isEmpty()) {
            stat.put("avg", 0.0);
            stat.put("max", 0.0);
            stat.put("min", 0.0);
            stat.put("passRate", 0.0);
            stat.put("dist", new LinkedHashMap<String, Integer>());
            return stat;
        }
        double sum = 0;
        double max = 0;
        double min = Double.MAX_VALUE;
        int passCount = 0;
        Map<String, Integer> dist = new LinkedHashMap<String, Integer>();
        dist.put("90-100", 0);
        dist.put("80-89", 0);
        dist.put("70-79", 0);
        dist.put("60-69", 0);
        dist.put("0-59", 0);
        for (ExamSession s : all) {
            double score = s.getTotalScore();
            sum += score;
            if (score > max) {
                max = score;
            }
            if (score < min) {
                min = score;
            }
            if (s.getPassed() == 1) {
                passCount++;
            }
            if (score >= 90) {
                dist.put("90-100", dist.get("90-100") + 1);
            } else if (score >= 80) {
                dist.put("80-89", dist.get("80-89") + 1);
            } else if (score >= 70) {
                dist.put("70-79", dist.get("70-79") + 1);
            } else if (score >= 60) {
                dist.put("60-69", dist.get("60-69") + 1);
            } else {
                dist.put("0-59", dist.get("0-59") + 1);
            }
        }
        stat.put("avg", round1(sum / all.size()));
        stat.put("max", round1(max));
        stat.put("min", round1(min));
        stat.put("passRate", round1(passCount * 100.0 / all.size()));
        stat.put("dist", dist);
        return stat;
    }

    private double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
