package com.group.exam.service;

import com.group.exam.common.BizException;
import com.group.exam.common.PasswordUtil;
import com.group.exam.common.Constants;
import com.group.exam.dao.DaoFactory;
import com.group.exam.dao.UserDao;
import com.group.exam.entity.User;

import java.sql.SQLException;

/**
 * 用户服务：登录与注册。
 */
public class UserService {

    private final UserDao userDao = DaoFactory.userDao();

    /**
     * 学生注册。
     *
     * @param username   登录名
     * @param password   密码
     * @param realName   真实姓名
     * @param studentNo  学号
     * @return 注册成功的用户
     */
    public User register(String username, String password, String realName, String studentNo) {
        if (username == null || username.trim().isEmpty()) {
            throw new BizException("用户名不能为空");
        }
        if (password == null || password.trim().length() < 6) {
            throw new BizException("密码不能少于 6 位");
        }
        if (realName == null || realName.trim().isEmpty()) {
            throw new BizException("真实姓名不能为空");
        }
        if (studentNo == null || studentNo.trim().isEmpty()) {
            throw new BizException("学号不能为空");
        }
        try {
            if (userDao.findByUsername(username.trim()) != null) {
                throw new BizException("用户名已被占用，请更换");
            }
            User user = new User(username.trim(), PasswordUtil.encrypt(password.trim()),
                    realName.trim(), Constants.ROLE_STUDENT, studentNo.trim());
            int userId = userDao.insert(user);
            user.setUserId(userId);
            return user;
        } catch (SQLException e) {
            throw new BizException("注册失败：" + e.getMessage(), e);
        }
    }

    /**
     * 登录校验。
     *
     * @return 登录成功的用户
     */
    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new BizException("用户名不能为空");
        }
        try {
            User user = userDao.findByUsername(username.trim());
            if (user == null || !PasswordUtil.verify(password == null ? "" : password, user.getPassword())) {
                throw new BizException("用户名或密码错误");
            }
            return user;
        } catch (SQLException e) {
            throw new BizException("登录失败：" + e.getMessage(), e);
        }
    }
}
