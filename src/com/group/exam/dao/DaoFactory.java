package com.group.exam.dao;

/**
 * DAO 工厂：集中管理各 DAO 单例，供 Service 层使用。
 */
public final class DaoFactory {

    private static final UserDao USER_DAO = new UserDao();
    private static final QuestionDao QUESTION_DAO = new QuestionDao();
    private static final PaperDao PAPER_DAO = new PaperDao();
    private static final PaperQuestionDao PAPER_QUESTION_DAO = new PaperQuestionDao();
    private static final ExamSessionDao EXAM_SESSION_DAO = new ExamSessionDao();
    private static final AnswerRecordDao ANSWER_RECORD_DAO = new AnswerRecordDao();

    private DaoFactory() {
    }

    public static UserDao userDao() {
        return USER_DAO;
    }

    public static QuestionDao questionDao() {
        return QUESTION_DAO;
    }

    public static PaperDao paperDao() {
        return PAPER_DAO;
    }

    public static PaperQuestionDao paperQuestionDao() {
        return PAPER_QUESTION_DAO;
    }

    public static ExamSessionDao examSessionDao() {
        return EXAM_SESSION_DAO;
    }

    public static AnswerRecordDao answerRecordDao() {
        return ANSWER_RECORD_DAO;
    }
}
