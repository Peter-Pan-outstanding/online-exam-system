package com.group.exam.console;

import com.group.exam.common.ConsoleKit;
import com.group.exam.common.GlobalExceptionHandler;
import com.group.exam.entity.User;
import com.group.exam.service.UserService;

/** 登录注册控制台；后续业务菜单尚未接入。 */
public final class AuthConsole {
    private AuthConsole() { }
    public static void run() {
        UserService service = new UserService();
        while (true) {
            System.out.println("\n在线考试系统：1 登录 / 2 学生注册 / 0 退出");
            String action = ConsoleKit.readLine("请选择：");
            if ("0".equals(action) || action.isEmpty()) return;
            try {
                if ("1".equals(action)) {
                    User user = service.login(ConsoleKit.readLine("用户名："), ConsoleKit.readLine("密码："));
                    if (user.isTeacher()) {
                        System.out.println("教师登录成功：" + user.getRealName());
                    } else if ("STUDENT".equals(user.getRole())) {
                        System.out.println("学生登录成功：" + user.getRealName());
                    } else {
                        throw new com.group.exam.common.BizException("不支持的用户角色");
                    }
                    System.out.println("本阶段已完成身份验证，退出登录返回首页。");
                } else if ("2".equals(action)) {
                    User user = service.register(ConsoleKit.readLine("用户名："), ConsoleKit.readLine("密码（至少6位）："),
                            ConsoleKit.readLine("真实姓名："), ConsoleKit.readLine("学号："));
                    System.out.println("注册成功：" + user.getUsername() + "，请登录。");
                } else {
                    System.out.println("请输入 0、1 或 2。");
                }
            } catch (Exception e) {
                GlobalExceptionHandler.handle(e);
            }
        }
    }
}
