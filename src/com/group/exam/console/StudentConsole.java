package com.group.exam.console;

import com.group.exam.common.ConsoleKit;
import com.group.exam.common.Constants;
import com.group.exam.entity.ExamPaper;
import com.group.exam.entity.ExamSession;
import com.group.exam.entity.User;
import com.group.exam.entity.AnswerRecord;
import com.group.exam.service.PaperService;
import com.group.exam.service.StatService;

import java.util.List;

/**
 * 学生端控制台：查看试卷、参加考试、查看成绩。
 */
public final class StudentConsole {

    private static final PaperService PAPER_SERVICE = new PaperService();
    private static final StatService STAT_SERVICE = new StatService();

    private StudentConsole() {
    }

    /**
     * 学生主菜单。
     *
     * @return false 表示退出登录
     */
    public static boolean showMenu(User student) {
        while (true) {
            System.out.println("\n========== 学生端 ==========");
            System.out.println(" 1. 查看可考试卷");
            System.out.println(" 2. 开始考试");
            System.out.println(" 3. 我的成绩");
            System.out.println(" 4. 错题本");
            System.out.println(" 0. 退出登录");
            int choice = ConsoleKit.readIntRange("请选择：", 0, 4);
            switch (choice) {
                case 1:
                    listPublishedPapers();
                    break;
                case 2:
                    startExam(student);
                    break;
                case 3:
                    showMyScores(student);
                    break;
                case 4:
                    showWrongBook(student);
                    break;
                case 0:
                    System.out.println("已退出登录。");
                    return false;
                default:
                    break;
            }
        }
    }

    private static void listPublishedPapers() {
        List<ExamPaper> list = PAPER_SERVICE.list(Constants.PAPER_PUBLISHED);
        if (list.isEmpty()) {
            System.out.println("当前没有可参加的考试。");
            return;
        }
        System.out.println("编号  试卷名称                科目        总分   时长(分)");
        for (ExamPaper p : list) {
            System.out.printf(" %-5d %-20s %-10s %-6.1f %-8d%n",
                    p.getPaperId(), p.getPaperName(), p.getSubject(), p.getTotalScore(),
                    p.getDurationMinutes());
        }
    }

    private static void startExam(User student) {
        listPublishedPapers();
        int paperId = ConsoleKit.readInt("请输入要参加的试卷编号：");
        ExamConsole.start(student, paperId);
    }

    private static void showMyScores(User student) {
        List<ExamSession> list = STAT_SERVICE.myScores(student.getUserId());
        if (list.isEmpty()) {
            System.out.println("您还没有考试记录。");
            return;
        }
        System.out.println("会话号  试卷名称                    总分    及格  状态        交卷时间");
        for (ExamSession s : list) {
            System.out.printf(" %-6d %-24s %-7.1f %-4s %-8s %s%n",
                    s.getSessionId(), s.getPaperName(), s.getTotalScore(),
                    s.getPassed() == 1 ? "是" : "否",
                    statusName(s.getStatus()),
                    s.getSubmitTime() == null ? "-" : s.getSubmitTime().substring(0, 19));
        }
    }

    private static String statusName(String status) {
        if (Constants.SESSION_IN_PROGRESS.equals(status)) {
            return "进行中";
        }
        if (Constants.SESSION_SUBMITTED.equals(status)) {
            return "正常交卷";
        }
        if (Constants.SESSION_TIMEOUT.equals(status)) {
            return "超时收卷";
        }
        return status;
    }

    private static void showWrongBook(User student) {
        List<AnswerRecord> list = STAT_SERVICE.wrongQuestions(student.getUserId());
        if (list.isEmpty()) {
            System.out.println("错题本为空，继续保持！");
            return;
        }
        System.out.println("\n========== 错题本 ==========");
        System.out.println("共 " + list.size() + " 道错题：");
        for (int i = 0; i < list.size(); i++) {
            AnswerRecord r = list.get(i);
            System.out.println((i + 1) + ". " + r.getQuestionContent());
            System.out.println("   你的答案：" + (r.getUserAnswer().isEmpty() ? "（未作答）" : r.getUserAnswer())
                    + " | 正确答案：" + r.getCorrectAnswer());
            System.out.println("   解析：" + (r.getAnalysis() == null || r.getAnalysis().isEmpty()
                    ? "（暂无解析）" : r.getAnalysis()));
        }
        System.out.println("===========================");
    }
}
