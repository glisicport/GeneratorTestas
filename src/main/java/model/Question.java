package model;

import java.util.List;

public class Question {
    private String type;
    private String question;
    private List<String> options;
    private String correct_answer;
    private String code;
    private String chapter;

    public Question() {}

    public Question(String type, String question, List<String> options, String correct_answer, String code, String chapter) {
        this.type = type;
        this.question = question;
        this.options = options;
        this.correct_answer = correct_answer;
        this.code = code;
        this.chapter = chapter;
    }

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public String getCorrect_answer() { return correct_answer; }
    public void setCorrect_answer(String correct_answer) { this.correct_answer = correct_answer; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getChapter() { return chapter; }
    public void setChapter(String chapter) { this.chapter = chapter; }
}
