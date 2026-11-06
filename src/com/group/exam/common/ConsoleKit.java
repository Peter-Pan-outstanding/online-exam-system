package com.group.exam.common;

import java.util.Scanner;

/**
 * 控制台输入输出辅助工具：统一管理 Scanner，提供带提示的读取方法。
 */
public final class ConsoleKit {

    private static final Scanner SCANNER = new Scanner(System.in);

    private ConsoleKit() {
    }

    /**
     * 读取一行文本（去除首尾空格）。
     *
     * @param tip 输入提示
     * @return 用户输入
     */
    public static String readLine(String tip) {
        System.out.print(tip);
        if (SCANNER.hasNextLine()) {
            return SCANNER.nextLine().trim();
        }
        return "";
    }

    /**
     * 读取一行文本；输入为空时返回默认值。
     *
     * @param tip         输入提示
     * @param defaultValue 默认值（可为 null）
     * @return 用户输入或默认值
     */
    public static String readLineOrDefault(String tip, String defaultValue) {
        String input = readLine(tip);
        return input.isEmpty() ? defaultValue : input;
    }

    /**
     * 读取整数，非法输入时循环重试。
     */
    public static int readInt(String tip) {
        while (true) {
            System.out.print(tip);
            if (SCANNER.hasNextInt()) {
                int value = SCANNER.nextInt();
                SCANNER.nextLine();
                return value;
            }
            SCANNER.nextLine();
            System.out.println("【提示】请输入整数。");
        }
    }

    /**
     * 读取 [min, max] 范围内的整数。
     */
    public static int readIntRange(String tip, int min, int max) {
        while (true) {
            int value = readInt(tip);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.println("【提示】请输入 " + min + " 到 " + max + " 之间的数字。");
        }
    }

    /**
     * 读取整数；输入为空时返回默认值。
     */
    public static int readIntOrDefault(String tip, int defaultValue) {
        String input = readLine(tip);
        if (input.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("【提示】输入的不是整数，已使用默认值 " + defaultValue);
            return defaultValue;
        }
    }

    /**
     * 读取 [min, max] 范围内的整数；输入为空时返回默认值（默认值须在范围内）。
     */
    public static int readIntRangeOrDefault(String tip, int defaultValue) {
        while (true) {
            int value = readIntOrDefault(tip, defaultValue);
            if (value >= 1 && value <= 3) {
                return value;
            }
            System.out.println("【提示】请输入 1 到 3 之间的数字。");
        }
    }
}
