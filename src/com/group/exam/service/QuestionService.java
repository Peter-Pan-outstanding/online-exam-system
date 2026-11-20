package com.group.exam.service;

import com.group.exam.common.BizException;
import com.group.exam.dao.DaoFactory;
import com.group.exam.dao.PaperQuestionDao;
import com.group.exam.dao.QuestionDao;
import com.group.exam.entity.Question;
import com.group.exam.entity.QuestionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 题库服务：题目的增删改查、随机抽题与批量导入。
 */
public class QuestionService {

    private final QuestionDao questionDao = DaoFactory.questionDao();
    private final PaperQuestionDao paperQuestionDao = DaoFactory.paperQuestionDao();

    /**
     * 新增题目。
     */
    public int add(Question question, int createdBy) {
        validate(question);
        try {
            return questionDao.insert(question, createdBy);
        } catch (SQLException e) {
            throw new BizException("新增题目失败：" + e.getMessage(), e);
        }
    }

    /**
     * 修改题目。
     */
    public void update(Question question) {
        if (question.getQuestionId() <= 0) {
            throw new BizException("题目编号无效");
        }
        validate(question);
        try {
            questionDao.update(question);
        } catch (SQLException e) {
            throw new BizException("修改题目失败：" + e.getMessage(), e);
        }
    }

    /**
     * 删除题目：被试卷引用时禁止删除，保证数据完整性。
     */
    public void delete(int questionId) {
        try {
            if (paperQuestionDao.countByQuestionId(questionId) > 0) {
                throw new BizException("该题目已被试卷引用，无法删除。如需删除请先移除试卷中的引用");
            }
            questionDao.deleteById(questionId);
        } catch (SQLException e) {
            throw new BizException("删除题目失败：" + e.getMessage(), e);
        }
    }

    /**
     * 查询题目列表。
     *
     * @param typeFilter 题型过滤，null 或空表示全部
     */
    public List<Question> list(String typeFilter) {
        try {
            return questionDao.findAll(typeFilter);
        } catch (SQLException e) {
            throw new BizException("查询题目失败：" + e.getMessage(), e);
        }
    }

    /**
     * 按编号查询题目。
     */
    public Question findById(int questionId) {
        try {
            Question question = questionDao.findById(questionId);
            if (question == null) {
                throw new BizException("题目不存在");
            }
            return question;
        } catch (SQLException e) {
            throw new BizException("查询题目失败：" + e.getMessage(), e);
        }
    }

    /**
     * 随机抽取指定数量某题型题目；数量不足时抛出业务异常。
     */
    public List<Question> randomByType(String type, int count) {
        if (count <= 0) {
            return new java.util.ArrayList<Question>();
        }
        try {
            List<Question> result = questionDao.findRandomByType(type, count);
            if (result.size() < count) {
                throw new BizException("题库中「" + typeName(type) + "」数量不足（需要 " + count
                        + " 道，现有 " + result.size() + " 道），请先补充题目");
            }
            return result;
        } catch (SQLException e) {
            throw new BizException("随机抽题失败：" + e.getMessage(), e);
        }
    }

    public int countByType(String type) {
        try {
            return questionDao.countByType(type);
        } catch (SQLException e) {
            throw new BizException("统计题目失败：" + e.getMessage(), e);
        }
    }

    /**
     * 批量导入题目：从 UTF-8 文本文件按行读取，每行一题、以 | 分隔共 10 列。
     * 格式：题型|题干|选项A|选项B|选项C|选项D|正确答案|分值|难度|解析
     * 题型支持 SINGLE/MULTIPLE/JUDGE（或 单选/多选/判断）；# 开头为注释行，空行忽略。
     * 单行解析或校验失败不影响其他行，逐行记录错误信息。
     *
     * @param filePath 导入文件路径（相对项目根目录）
     * @param createdBy 创建人（教师）
     * @return 导入结果：成功条数与逐行错误
     */
    public ImportResult importFromFile(String filePath, int createdBy) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BizException("读取导入文件失败：" + e.getMessage(), e);
        }
        ImportResult result = new ImportResult();
        List<Question> validQuestions = new ArrayList<Question>();
        int lineNo = 0;
        for (String raw : lines) {
            lineNo++;
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            try {
                Question question = parseImportLine(line);
                validate(question);
                validQuestions.add(question);
            } catch (RuntimeException e) {
                result.errors.add("第 " + lineNo + " 行：" + e.getMessage());
            }
        }
        try {
            questionDao.batchInsert(validQuestions, createdBy);
            result.success = validQuestions.size();
        } catch (SQLException e) {
            throw new BizException("批量导入写库失败：" + e.getMessage(), e);
        }
        return result;
    }

    /** 解析导入文件中的一行题目 */
    private Question parseImportLine(String line) {
        String[] fields = line.split("\\|", -1);
        if (fields.length != 10) {
            throw new BizException("字段数应为 10 列（当前 " + fields.length + " 列），请检查分隔符与列数");
        }
        String type = normalizeType(fields[0]);
        String content = fields[1].trim();
        if (content.isEmpty()) {
            throw new BizException("题干不能为空");
        }
        List<String> options = new ArrayList<String>();
        if (com.group.exam.common.Constants.TYPE_JUDGE.equals(type)) {
            options.add(null);
            options.add(null);
            options.add(null);
            options.add(null);
        } else {
            for (int i = 0; i < 4; i++) {
                String opt = fields[i + 2] == null ? "" : fields[i + 2].trim();
                if (opt.isEmpty()) {
                    throw new BizException("选项 " + (char) ('A' + i) + " 不能为空");
                }
                options.add(opt);
            }
        }
        String answer = fields[6] == null ? "" : fields[6].trim().toUpperCase();
        double score;
        try {
            score = Double.parseDouble(fields[7].trim());
        } catch (NumberFormatException e) {
            throw new BizException("分值不是有效数字：" + fields[7]);
        }
        int difficulty;
        try {
            difficulty = Integer.parseInt(fields[8].trim());
        } catch (NumberFormatException e) {
            throw new BizException("难度不是有效整数：" + fields[8]);
        }
        String analysis = fields[9] == null ? null : fields[9].trim();
        return QuestionFactory.create(type, content, options, answer, score, difficulty, analysis);
    }

    /** 题型别名归一化：SINGLE/单选 -> SINGLE，依次类推 */
    private String normalizeType(String raw) {
        String type = raw == null ? "" : raw.trim().toUpperCase();
        if (com.group.exam.common.Constants.TYPE_SINGLE.equals(type) || "单选".equals(type)) {
            return com.group.exam.common.Constants.TYPE_SINGLE;
        }
        if (com.group.exam.common.Constants.TYPE_MULTIPLE.equals(type) || "多选".equals(type)) {
            return com.group.exam.common.Constants.TYPE_MULTIPLE;
        }
        if (com.group.exam.common.Constants.TYPE_JUDGE.equals(type) || "判断".equals(type)) {
            return com.group.exam.common.Constants.TYPE_JUDGE;
        }
        throw new BizException("未知题型：" + raw + "（支持 SINGLE/MULTIPLE/JUDGE）");
    }

    /**
     * 批量导入结果。
     */
    public static class ImportResult {
        private int success;
        private List<String> errors = new ArrayList<String>();

        public int getSuccess() {
            return success;
        }

        public List<String> getErrors() {
            return errors;
        }
    }

    private void validate(Question question) {
        if (question.getContent() == null || question.getContent().trim().isEmpty()) {
            throw new BizException("题干不能为空");
        }
        if (question.getScore() <= 0) {
            throw new BizException("分值必须大于 0");
        }
        if (question.getDifficulty() < 1 || question.getDifficulty() > 3) {
            throw new BizException("难度等级只能为 1-3");
        }
        String answer = question.getCorrectAnswer();
        if (answer == null || !question.isValidAnswer(answer)) {
            throw new BizException("答案格式不正确：" + question.answerTip().replace("请输入", "请按格式输入"));
        }
    }

    private String typeName(String type) {
        if (com.group.exam.common.Constants.TYPE_SINGLE.equals(type)) {
            return "单选题";
        }
        if (com.group.exam.common.Constants.TYPE_MULTIPLE.equals(type)) {
            return "多选题";
        }
        if (com.group.exam.common.Constants.TYPE_JUDGE.equals(type)) {
            return "判断题";
        }
        return type;
    }
}
