package com.group.exam.entity;

/**
 * 用户实体：教师 / 学生。
 */
public class User {

    private int userId;
    private String username;
    private String password;
    private String realName;
    private String role;
    private String studentNo;
    private String createdAt;

    public User() {
    }

    public User(String username, String password, String realName, String role, String studentNo) {
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.role = role;
        this.studentNo = studentNo;
    }

    public boolean isTeacher() {
        return com.group.exam.common.Constants.ROLE_TEACHER.equals(role);
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
