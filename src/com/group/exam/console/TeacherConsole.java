package com.group.exam.console;

import com.group.exam.common.ConsoleKit;
import com.group.exam.entity.User;

/**
 * 教师端控制台：题库管理、试卷管理、成绩统计。
 */
public final class TeacherConsole {

    private TeacherConsole() {
    }

    /**
     * 教师主菜单。
     *
     * @return false 表示退出登录
     */
    public static boolean showMenu(User teacher) {
        while (true) {
            System.out.println("\n========== 教师端 ==========");
            System.out.println(" 1. 题库管理");
            System.out.println(" 2. 试卷管理");
            System.out.println(" 3. 成绩统计");
            System.out.println(" 0. 退出登录");
            int choice = ConsoleKit.readIntRange("请选择：", 0, 3);
            switch (choice) {
                case 1:
                    QuestionConsole.showMenu(teacher);
                    break;
                case 2:
                    PaperConsole.showMenu(teacher);
                    break;
                case 3:
                    StatConsole.showSummary();
                    break;
                case 0:
                    System.out.println("已退出登录。");
                    return false;
                default:
                    break;
            }
        }
    }
}
