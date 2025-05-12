package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import model.Question;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionService {
    private String templatePath;
    private final ObjectMapper objectMapper;

    public QuestionService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void setTemplatePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Template path cannot be null or empty");
        }
        this.templatePath = path;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    private void checkTemplatePath() throws IOException {
        if (templatePath == null || templatePath.trim().isEmpty()) {
            throw new IOException("Template path not set. Please select a template file first.");
        }
    }

    public void loadTemplate(String path) throws IOException {
        if (path == null || path.trim().isEmpty()) {
            throw new IOException("Template path cannot be null or empty");
        }
        
        File file = new File(path);
        if (!file.exists()) {
            throw new IOException("Template file does not exist: " + path);
        }
        
        // Verify the file is a valid JSON template
        Map<String, Object> template = objectMapper.readValue(file, Map.class);
        if (!template.containsKey("chapters")) {
            throw new IOException("Invalid template format: missing 'chapters' array");
        }
        
        this.templatePath = path;
    }

    public void createNewTemplate(String name) throws IOException {
        checkTemplatePath();
        // Create a new template with basic structure
        Map<String, Object> template = new HashMap<>();
        template.put("name", name);
        template.put("chapters", new ArrayList<>());
        
        // Write the template to the current path
        writeTemplate(template);
    }

    public void addChapter(String chapterName) throws IOException {
        checkTemplatePath();
        Map<String, Object> template = readTemplate();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> chapters = (List<Map<String, Object>>) template.get("chapters");
        
        if (chapters == null) {
            chapters = new ArrayList<>();
            template.put("chapters", chapters);
        }
        
        // Check if chapter already exists
        for (Map<String, Object> chapter : chapters) {
            if (chapterName.equals(chapter.get("name"))) {
                return; // Chapter already exists
            }
        }
        
        // Add new chapter
        Map<String, Object> newChapter = new HashMap<>();
        newChapter.put("name", chapterName);
        newChapter.put("questions", new ArrayList<>());
        chapters.add(newChapter);
        
        writeTemplate(template);
    }

    public void saveTemplate() throws IOException {
        checkTemplatePath();
        // Read and write the template to ensure proper formatting
        Map<String, Object> template = readTemplate();
        writeTemplate(template);
    }

    public List<String> getChapters() throws IOException {
        checkTemplatePath();
        Map<String, Object> template = readTemplate();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> chapters = (List<Map<String, Object>>) template.get("chapters");
        List<String> chapterNames = new ArrayList<>();
        
        if (chapters != null) {
            for (Map<String, Object> chapter : chapters) {
                chapterNames.add((String) chapter.get("name"));
            }
        }
        
        return chapterNames;
    }

    public List<Question> getQuestionsByChapter(String chapterName) throws IOException {
        checkTemplatePath();
        Map<String, Object> template = readTemplate();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> chapters = (List<Map<String, Object>>) template.get("chapters");
        List<Question> questions = new ArrayList<>();

        if (chapters != null) {
            for (Map<String, Object> chapter : chapters) {
                if (chapterName.equals(chapter.get("name"))) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> chapterQuestions = (List<Map<String, Object>>) chapter.get("questions");
                    if (chapterQuestions != null) {
                        for (Map<String, Object> q : chapterQuestions) {
                            Question question = new Question();
                            question.setType((String) q.get("type"));
                            question.setQuestion((String) q.get("question"));
                            @SuppressWarnings("unchecked")
                            List<String> options = (List<String>) q.get("options");
                            question.setOptions(options != null ? options : new ArrayList<>());
                            question.setCode((String) q.get("code"));
                            
                            // Handle both correct_answers and correct_answer fields
                            if (q.containsKey("correct_answers")) {
                                @SuppressWarnings("unchecked")
                                List<String> correctAnswers = (List<String>) q.get("correct_answers");
                                if (correctAnswers != null && !correctAnswers.isEmpty()) {
                                    question.setCorrect_answer(String.join(",", correctAnswers));
                                }
                            } 
                            if (question.getCorrect_answer() == null && q.containsKey("correct_answer")) {
                                String correctAnswer = (String) q.get("correct_answer");
                                if (correctAnswer != null) {
                                    question.setCorrect_answer(correctAnswer);
                                }
                            }
                            
                            // Only add questions that have all required fields
                            if (question.getType() != null && question.getQuestion() != null && question.getCorrect_answer() != null) {
                                questions.add(question);
                            }
                        }
                    }
                    break;
                }
            }
        }

        return questions;
    }

    public void addQuestion(String chapterName, Question question) throws IOException {
        checkTemplatePath();
        Map<String, Object> template = readTemplate();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> chapters = (List<Map<String, Object>>) template.get("chapters");
        
        if (chapters != null) {
            for (Map<String, Object> chapter : chapters) {
                if (chapterName.equals(chapter.get("name"))) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> questions = (List<Map<String, Object>>) chapter.get("questions");
                    if (questions == null) {
                        questions = new ArrayList<>();
                        chapter.put("questions", questions);
                    }
                    
                    Map<String, Object> questionMap = new HashMap<>();
                    questionMap.put("type", question.getType());
                    questionMap.put("question", question.getQuestion());
                    questionMap.put("options", question.getOptions() != null ? question.getOptions() : new ArrayList<>());
                    questionMap.put("code", question.getCode());

                    // Handle correct answers based on question type
                    if (question.getType().equals("fill_in_the_blank")) {
                        String[] answers = question.getCorrect_answer().split(",");
                        if (answers.length > 1) {
                            questionMap.put("correct_answers", Arrays.asList(answers));
                        } else {
                            questionMap.put("correct_answer", answers[0].trim());
                        }
                    } else {
                        questionMap.put("correct_answer", question.getCorrect_answer());
                        // Ensure we don't have both fields for consistency
                        questionMap.remove("correct_answers");
                    }
                    
                    questions.add(questionMap);
                    writeTemplate(template);
                    return;
                }
            }
        }
        
        throw new IOException("Chapter not found: " + chapterName);
    }

    public void updateQuestion(String chapterName, int index, Question question) throws IOException {
        checkTemplatePath();
        Map<String, Object> template = readTemplate();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> chapters = (List<Map<String, Object>>) template.get("chapters");
        
        if (chapters != null) {
            for (Map<String, Object> chapter : chapters) {
                if (chapterName.equals(chapter.get("name"))) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> questions = (List<Map<String, Object>>) chapter.get("questions");
                    if (questions != null && index >= 0 && index < questions.size()) {
                        Map<String, Object> questionMap = new HashMap<>();
                        questionMap.put("type", question.getType());
                        questionMap.put("question", question.getQuestion());
                        questionMap.put("options", question.getOptions());
                        questionMap.put("code", question.getCode());

                        // Handle correct answers based on question type
                        if (question.getType().equals("fill_in_the_blank")) {
                            String[] answers = question.getCorrect_answer().split(",");
                            if (answers.length > 1) {
                                questionMap.put("correct_answers", Arrays.asList(answers));
                            } else {
                                questionMap.put("correct_answer", answers[0].trim());
                            }
                        } else {
                            questionMap.put("correct_answer", question.getCorrect_answer());
                        }
                        
                        questions.set(index, questionMap);
                        writeTemplate(template);
                    }
                    break;
                }
            }
        }
    }

    public void deleteQuestion(String chapterName, int index) throws IOException {
        checkTemplatePath();
        Map<String, Object> template = readTemplate();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> chapters = (List<Map<String, Object>>) template.get("chapters");

        if (chapters != null) {
            for (Map<String, Object> chapter : chapters) {
                if (chapterName.equals(chapter.get("name"))) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> questions = (List<Map<String, Object>>) chapter.get("questions");
                    if (questions != null && index >= 0 && index < questions.size()) {
                        questions.remove(index);
                        writeTemplate(template);
                    }
                    break;
                }
            }
        }
    }

    private Map<String, Object> readTemplate() throws IOException {
        checkTemplatePath();
        File file = new File(templatePath);
        if (!file.exists()) {
            throw new IOException("Template file does not exist: " + templatePath);
        }
        return objectMapper.readValue(file, Map.class);
    }

    private void writeTemplate(Map<String, Object> template) throws IOException {
        objectMapper.writeValue(new File(templatePath), template);
    }

    private Map<String, Object> questionToMap(Question question) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", question.getType());
        map.put("question", question.getQuestion());
        map.put("options", question.getOptions());
        map.put("code", question.getCode());
        map.put("correct_answer", question.getCorrect_answer());
        return map;
    }
}
