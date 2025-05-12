package generator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateModel {
    private List<Chapter> chapters;
    private Metadata metadata;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Chapter {
        private String name;
        private List<QuestionTemplate> questions;

        // Getters
        public String getName() { return name; }
        public List<QuestionTemplate> getQuestions() { return questions; }

        // Setters
        public void setName(String name) { this.name = name; }
        public void setQuestions(List<QuestionTemplate> questions) { this.questions = questions; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        private List<String> classNames;
        private List<String> methodNames;
        private List<String> returnTypes;
        private List<String> exceptionTypes;

        // Getters
        public List<String> getClassNames() { return classNames; }
        public List<String> getMethodNames() { return methodNames; }
        public List<String> getReturnTypes() { return returnTypes; }
        public List<String> getExceptionTypes() { return exceptionTypes; }

        // Setters
        public void setClassNames(List<String> classNames) { this.classNames = classNames; }
        public void setMethodNames(List<String> methodNames) { this.methodNames = methodNames; }
        public void setReturnTypes(List<String> returnTypes) { this.returnTypes = returnTypes; }
        public void setExceptionTypes(List<String> exceptionTypes) { this.exceptionTypes = exceptionTypes; }
    }

    // Nested class for question templates
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QuestionTemplate {
        private String type;
        private String question;
        private List<String> options;
        private String correct_answer;
        private List<String> correct_answers;
        private String code;

        // Getters
        public String getType() { return type; }
        public String getQuestion() { return question; }
        public List<String> getOptions() { return options; }
        public String getCorrect_answer() { return correct_answer; }
        public List<String> getCorrect_answers() { return correct_answers; }
        public String getCode() { return code; }

        // Setters
        public void setType(String type) { this.type = type; }
        public void setQuestion(String question) { this.question = question; }
        public void setOptions(List<String> options) { this.options = options; }
        public void setCorrect_answer(String correct_answer) { this.correct_answer = correct_answer; }
        public void setCorrect_answers(List<String> correct_answers) { this.correct_answers = correct_answers; }
        public void setCode(String code) { this.code = code; }
    }

    // Getters
    public List<Chapter> getChapters() { return chapters; }
    public Metadata getMetadata() { return metadata; }

    // Setters
    public void setChapters(List<Chapter> chapters) { this.chapters = chapters; }
    public void setMetadata(Metadata metadata) { this.metadata = metadata; }
}