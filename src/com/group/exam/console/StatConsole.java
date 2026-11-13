package com.group.exam.console;

import com.group.exam.common.Constants;
import com.group.exam.common.ConsoleKit;
import com.group.exam.entity.ExamSession;
import com.group.exam.service.StatService;

import java.util.List;
import java.util.Map;

/**
 * 成绩统计控制台：汇总指标与明细列表。
 */
public final class StatConsole {

    private static final StatService STAT_SERVICE = new StatService();

    private StatConsole() {
    }

    public static void showSummary() {
        Map<String, Object> stat = STAT_SERVICE.summary();
        System.out.println("\n========== 成绩统计 ==========");
        System.out.println("已结束考试人次：" + stat.get("count"));
        if ((Integer) stat.get("count") == 0) {
            System.out.println("暂无已结束的考试记录。");
            return;
        }
        System.out.println("平均分：" + stat.get("avg")
                + " | 最高分：" + stat.get("max")
                + " | 最低分：" + stat.get("min")
                + " | 及格率：" + stat.get("passRate") + "%");
        @SuppressWarnings("unchecked")
        Map<String, Integer> dist = (Map<String, Integer>) stat.get("dist");
        System.out.print("分数段分布：");
        for (Map.Entry<String, Integer> entry : dist.entrySet()) {
            System.out.print(entry.getKey() + "分: " + entry.getValue() + "人  ");
        }
        System.out.println();

        List<ExamSession> records = STAT_SERVICE.allFinished();
        System.out.println("---------- 明细 ----------");
        System.out.println("会话号  学生    试卷名称                    总分   及格  状态        交卷时间");
        for (ExamSession s : records) {
            System.out.printf(" %-6d %-6s %-24s %-6.1f %-4s %-8s %s%n",
                    s.getSessionId(), s.getStudentName(), s.getPaperName(),
                    s.getTotalScore(), s.getPassed() == 1 ? "是" : "否",
                    statusName(s.getStatus()),
                    s.getSubmitTime() == null ? "-" : s.getSubmitTime().substring(0, 19));
        }
    }

    private static String statusName(String status) {
        if (Constants.SESSION_SUBMITTED.equals(status)) {
            return "正常交卷";
        }
        if (Constants.SESSION_TIMEOUT.equals(status)) {
            return "超时收卷";
        }
        return status;
    }
}
