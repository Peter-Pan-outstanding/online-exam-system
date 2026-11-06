package com.group.exam.common;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 全局异常处理器：
 * 业务异常（BizException）直接向用户输出提示；
 * 其他异常记录到 error.log 并给出友好提示，保证程序不因单次操作崩溃。
 */
public final class GlobalExceptionHandler {

    private GlobalExceptionHandler() {
    }

    public static void handle(Throwable e) {
        if (e instanceof BizException) {
            System.out.println("【提示】" + e.getMessage());
            return;
        }
        System.out.println("【系统错误】操作失败，详情已写入 error.log，请报告给开发组。");
        appendLog(e);
    }

    private static void appendLog(Throwable e) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("error.log", true))) {
            writer.println("===== " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " =====");
            e.printStackTrace(writer);
        } catch (IOException ignored) {
            // 日志写入失败不影响程序继续运行
        }
    }
}
