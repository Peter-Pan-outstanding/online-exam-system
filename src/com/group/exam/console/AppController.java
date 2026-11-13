package com.group.exam.console;

import com.group.exam.common.GlobalExceptionHandler;
import com.group.exam.entity.User;

/**
 * 应用控制器：登录后按角色分发到教师端 / 学生端菜单，并统一拦截异常。
 */
public final class AppController {

    private AppController() {
    }

    /**
     * 运行角色主菜单。
     *
     * @return false 表示用户退出登录，返回主界面
     */
    public static boolean run(User user) {
        System.out.println("\n欢迎，" + user.getRealName() + "！");
        try {
            if (user.isTeacher()) {
                return TeacherConsole.showMenu(user);
            }
            return StudentConsole.showMenu(user);
        } catch (Exception e) {
            GlobalExceptionHandler.handle(e);
            return true;
        }
    }
}
