package com.group.exam.console;

import com.group.exam.common.BizException;
import com.group.exam.common.ConsoleKit;
import com.group.exam.common.Constants;
import com.group.exam.entity.ExamPaper;
import com.group.exam.entity.PaperItem;
import com.group.exam.entity.User;
import com.group.exam.service.PaperService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 试卷管理控制台：创建、随机组卷、发布、列表、详情。
 */
public final class PaperConsole {

    private static final PaperService PAPER_SERVICE = new PaperService();

    private PaperConsole() {
    }

    public static void showMenu(User teacher) {
        while (true) {
            System.out.println("\n---------- 试卷管理 ----------");
            System.out.println(" 1. 创建试卷");
            System.out.println(" 2. 随机组卷");
            System.out.println(" 3. 发布试卷");
            System.out.println(" 4. 试卷列表");
            System.out.println(" 5. 查看试卷详情");
            System.out.println(" 0. 返回上级");
            int choice = ConsoleKit.readIntRange("请选择：", 0, 5);
            switch (choice) {
                case 1:
                    createPaper(teacher);
                    break;
                case 2:
                    generatePaper(teacher);
                    break;
                case 3:
                    publishPaper();
                    break;
                case 4:
                    listPapers(null);
                    break;
                case 5:
                    showDetail();
                    break;
                case 0:
                    return;
                default:
                    break;
            }
        }
    }

    private static void createPaper(User teacher) {
        String name = ConsoleKit.readLine("试卷名称：");
        if (name.isEmpty()) {
            throw new BizException("试卷名称不能为空");
        }
        String subject = ConsoleKit.readLine("科目：");
        if (subject.isEmpty()) {
            throw new BizException("科目不能为空");
        }
        int duration = ConsoleKit.readIntRange("考试时长（分钟，1-180）：", 1, 180);
        ExamPaper paper = PAPER_SERVICE.create(name, subject, duration, teacher.getUserId());
        System.out.println("创建成功，试卷编号：" + paper.getPaperId() + "（状态：草稿）");
    }

    private static void generatePaper(User teacher) {
        listPapers(Constants.PAPER_DRAFT);
        int paperId = ConsoleKit.readInt("请输入要组卷的试卷编号（草稿）：");
        System.out.println("请输入各题型抽取数量（输入 0 表示不抽该题型）：");
        Map<String, Integer> rules = new LinkedHashMap<String, Integer>();
        int single = ConsoleKit.readIntRange("单选题数量：", 0, 50);
        int multiple = ConsoleKit.readIntRange("多选题数量：", 0, 50);
        int judge = ConsoleKit.readIntRange("判断题数量：", 0, 50);
        if (single > 0) {
            rules.put(Constants.TYPE_SINGLE, single);
        }
        if (multiple > 0) {
            rules.put(Constants.TYPE_MULTIPLE, multiple);
        }
        if (judge > 0) {
            rules.put(Constants.TYPE_JUDGE, judge);
        }
        List<PaperItem> items = PAPER_SERVICE.generateRandom(paperId, rules);
        double total = 0;
        for (PaperItem item : items) {
            total += item.getPaperQuestion().getScore();
        }
        System.out.println("组卷成功：共 " + items.size() + " 题，总分 " + total + " 分。");
    }

    private static void publishPaper() {
        listPapers(Constants.PAPER_DRAFT);
        int paperId = ConsoleKit.readInt("请输入要发布的试卷编号：");
        PAPER_SERVICE.publish(paperId);
        System.out.println("发布成功，学生现在可以参加该考试。");
    }

    private static void listPapers(String statusFilter) {
        List<ExamPaper> list = PAPER_SERVICE.list(statusFilter);
        if (list.isEmpty()) {
            System.out.println("没有符合条件的试卷。");
            return;
        }
        System.out.println("编号  试卷名称                科目        总分   时长(分)  状态");
        for (ExamPaper p : list) {
            System.out.printf(" %-5d %-20s %-10s %-6.1f %-8d %s%n",
                    p.getPaperId(), p.getPaperName(), p.getSubject(),
                    p.getTotalScore(), p.getDurationMinutes(), statusName(p.getStatus()));
        }
    }

    private static void showDetail() {
        int paperId = ConsoleKit.readInt("请输入试卷编号：");
        PaperService.PaperDetail detail = PAPER_SERVICE.detail(paperId);
        ExamPaper paper = detail.getPaper();
        System.out.println("试卷：" + paper.getPaperName() + " | 科目：" + paper.getSubject()
                + " | 总分：" + paper.getTotalScore() + " | 时长：" + paper.getDurationMinutes()
                + " 分钟 | 状态：" + statusName(paper.getStatus()));
        if (detail.getItems().isEmpty()) {
            System.out.println("该试卷尚未组卷。");
            return;
        }
        System.out.println("---------- 试卷题目 ----------");
        for (PaperItem item : detail.getItems()) {
            System.out.println("第 " + item.getPaperQuestion().getQuestionOrder() + " 题（"
                    + item.getPaperQuestion().getScore() + " 分）");
            System.out.println(item.getQuestion().toDisplay());
            System.out.println("参考答案：" + item.getQuestion().getCorrectAnswer());
            System.out.println();
        }
    }

    private static String statusName(String status) {
        if (Constants.PAPER_DRAFT.equals(status)) {
            return "草稿";
        }
        if (Constants.PAPER_PUBLISHED.equals(status)) {
            return "已发布";
        }
        return status;
    }
}
