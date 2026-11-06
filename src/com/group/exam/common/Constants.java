package com.group.exam.common;

/**
 * 系统常量定义：角色、题型、状态等。
 */
public final class Constants {

    /** 角色：教师 */
    public static final String ROLE_TEACHER = "TEACHER";
    /** 角色：学生 */
    public static final String ROLE_STUDENT = "STUDENT";

    /** 题型：单选题 */
    public static final String TYPE_SINGLE = "SINGLE";
    /** 题型：多选题 */
    public static final String TYPE_MULTIPLE = "MULTIPLE";
    /** 题型：判断题 */
    public static final String TYPE_JUDGE = "JUDGE";

    /** 试卷状态：草稿 */
    public static final String PAPER_DRAFT = "DRAFT";
    /** 试卷状态：已发布 */
    public static final String PAPER_PUBLISHED = "PUBLISHED";

    /** 考试会话状态：进行中 */
    public static final String SESSION_IN_PROGRESS = "IN_PROGRESS";
    /** 考试会话状态：已交卷 */
    public static final String SESSION_SUBMITTED = "SUBMITTED";
    /** 考试会话状态：超时自动收卷 */
    public static final String SESSION_TIMEOUT = "TIMEOUT";

    /** 及格分数线（百分制） */
    public static final double PASS_SCORE = 60.0;

    private Constants() {
    }
}
