package com.group.exam.dao;

/** 本阶段仅装配用户 DAO，其他模块后续接入。 */
public final class DaoFactory {
    private static final UserDao USER_DAO = new UserDao();
    private DaoFactory() { }
    public static UserDao userDao() { return USER_DAO; }
}
