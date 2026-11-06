# 在线考试系统（Java SE 控制台）

当前提交范围：截至 2026-11-06 的课程阶段，Java SE + MySQL 8.0 + JDBC。

## 已包含

- 六张表的建库脚本和初始数据。
- common / entity / dao / service / console 分层工程结构。
- DBUtil、PasswordUtil、ConsoleKit 与异常处理基础工具。
- BaseDao 泛型查询、RowMapper、仅装配 UserDao 的 DaoFactory；参数化 SQL。
- 学生注册、登录验证、教师/学生角色识别；注册不能自行指定教师身份。
- 题目继承体系及 QuestionFactory、判分策略接口、ExamSession 会话数据模型。

题库增删改查、组卷发布、在线考试、自动判分实现、成绩统计及 V2 功能留待后续阶段。

## 拉取、编译与运行

1. 安装 JDK 8 或更新版本，配置 java、javac 到 PATH，并准备 MySQL 8.0。
2. 克隆本仓库，在项目根目录操作。
3. 在 MySQL 客户端执行 `source /你的路径/sql/exam_db.sql`。脚本会删除并重建 exam_db，请用于课程测试数据库。
4. 修改根目录 db.properties，填写本机数据库用户名和密码。
5. 自行下载 MySQL Connector/J 8.0.33，将驱动放到 lib/mysql-connector-java-8.0.33.jar。编译不依赖驱动，连接数据库时需要。
6. Windows 执行 compile.bat，再执行 run.bat；IDE 中可将 src 标记为源码目录，添加驱动，运行 com.group.exam.Main，工作目录设为项目根目录。

初始账号：admin（教师）、stu001 / stu002（学生），初始密码均为 123456。
密码按课程方案以 MD5(密码 + group_exam_salt) 保存摘要。多选全对才得分，判断题答案约定为 T/F；本阶段仅定义判分接口。

## 阶段计划

11 月 3 日：需求分析、分层设计、开发环境与功能边界确定。
11 月 6 日：数据库、DAO 基础设施、登录注册及题目/会话模型设计。
下一阶段：题库管理和试卷管理。
