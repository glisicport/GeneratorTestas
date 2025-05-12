package generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class TestGenerator {

    private static class QuestionWithHeight {
        TemplateModel.QuestionTemplate question;
        float height;
        int index;

        public QuestionWithHeight(TemplateModel.QuestionTemplate question, float height, int index) {
            this.question = question;
            this.height = height;
            this.index = index;
        }
    }

    private static String templatePath;

    public static void setTemplatePath(String path) {
        templatePath = path;
    }

    public static void generateTest(int numQuestions, String outputPath,
                                    String testType, List<String> selectedChapters, String subjectName) throws Exception {
        if (templatePath == null || templatePath.isEmpty()) {
            throw new IllegalArgumentException("Template path not set");
        }

        // Load template
        ObjectMapper objectMapper = new ObjectMapper();
        TemplateModel templateModel = objectMapper.readValue(new File(templatePath), TemplateModel.class);

        // Filter questions based on selected chapters
        ArrayList<TemplateModel.QuestionTemplate> availableQuestions = new ArrayList<>();
        for (TemplateModel.Chapter chapter : templateModel.getChapters()) {
            String chapterName = chapter.getName().trim();
            
            boolean isChapterSelected = false;
            for (String selectedChapter : selectedChapters) {
                if (selectedChapter.trim().equals(chapterName)) {
                    isChapterSelected = true;
                    break;
                }
            }

            if (isChapterSelected && chapter.getQuestions() != null) {
                for (TemplateModel.QuestionTemplate question : chapter.getQuestions()) {
                    if (question.getType() == null || question.getQuestion() == null) {
                        continue;
                    }

                    // For multiple choice questions, ensure options exist
                    if (question.getType().equals("multiple_choice")) {
                        if (question.getOptions() == null || question.getOptions().isEmpty()) {
                            continue;
                        }
                    }

                    // For fill in the blank questions
                    if (question.getType().equals("fill_in_the_blank")) {
                        question.setQuestion(question.getQuestion().replace("___","______________________"));
                        if (question.getCorrect_answers() == null && question.getCorrect_answer() != null) {
                            List<String> answers = new ArrayList<>();
                            answers.add(question.getCorrect_answer());
                            question.setCorrect_answers(answers);
                        } else if (question.getCorrect_answers() == null && question.getCorrect_answer() == null) {
                            continue;
                        }
                    }

                    availableQuestions.add(question);
                }
            }
        }

        if (availableQuestions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Nema dostupnih pitanja u odabranim poglavljima."
            );
        }

        if (numQuestions > availableQuestions.size()) {
            throw new IllegalArgumentException(
                    "Zahtevano je " + numQuestions + " pitanja, ali je dostupno samo " +
                            availableQuestions.size() + " pitanja u odabranim poglavljima."
            );
        }

        // Randomly select questions with code question limitation
        ArrayList<TemplateModel.QuestionTemplate> selectedTemplates = new ArrayList<>();
        ArrayList<TemplateModel.QuestionTemplate> codeQuestions = new ArrayList<>();
        ArrayList<TemplateModel.QuestionTemplate> nonCodeQuestions = new ArrayList<>();
        Random random = new Random();

        // Separate code and non-code questions
        for (TemplateModel.QuestionTemplate question : availableQuestions) {
            if (question.getCode() != null && !question.getCode().isEmpty()) {
                codeQuestions.add(question);
            } else {
                nonCodeQuestions.add(question);
            }
        }

        // Calculate number of code questions needed (1 per 8 questions)
        int codeQuestionsNeeded = Math.max(1, numQuestions / 8);

        // Add code questions if available
        if (!codeQuestions.isEmpty()) {
            for (int i = 0; i < codeQuestionsNeeded && !codeQuestions.isEmpty(); i++) {
                int randomCodeIndex = random.nextInt(codeQuestions.size());
                selectedTemplates.add(codeQuestions.remove(randomCodeIndex));
            }
        }

        // Fill remaining slots with non-code questions
        int remainingQuestions = numQuestions - selectedTemplates.size();
        for (int i = 0; i < remainingQuestions && !nonCodeQuestions.isEmpty(); i++) {
            int index = random.nextInt(nonCodeQuestions.size());
            selectedTemplates.add(nonCodeQuestions.remove(index));
        }

        // Shuffle the final selection
        Collections.shuffle(selectedTemplates, random);

        // Create PDF document
        try (PdfWriter pdfWriter = new PdfWriter(outputPath);
             PdfDocument pdfDocument = new PdfDocument(pdfWriter);
             Document document = new Document(pdfDocument, PageSize.A4)) {

            // Set margins to use more of the page
            document.setMargins(36, 36, 36, 36); // 0.5 inch margins all around

            // Load custom fonts
            PdfFont titleFont = PdfFontFactory.createFont("src/main/resources/fonts/Roboto-Bold.ttf");
            PdfFont bodyFont = PdfFontFactory.createFont("src/main/resources/fonts/Roboto-Regular.ttf");
            PdfFont codeFont = PdfFontFactory.createFont("src/main/resources/fonts/RobotoMono-Regular.ttf");

            // Add header to first page
            document.add(createHeader(titleFont, testType, subjectName));

            // Add decorative line
            SolidLine line = new SolidLine(1.5f);
            line.setColor(new DeviceRgb(52, 152, 219));
            document.add(new LineSeparator(line)
                    .setMarginTop(10)
            );

            // Process questions by filling columns based on available space
            float pageHeight = document.getPdfDocument().getDefaultPageSize().getHeight()
                    - document.getTopMargin()
                    - document.getBottomMargin()
                    - 180; // Reduced from 200

            float columnWidth = (document.getPdfDocument().getDefaultPageSize().getWidth()
                    - document.getLeftMargin()
                    - document.getRightMargin()
                    - 20) / 2; // Divide by 2 for two columns, subtract spacing

            int currentQuestion = 0;

            // Pre-calculate heights and sort questions
            List<QuestionWithHeight> questionsWithHeight = new ArrayList<>();
            List<QuestionWithHeight> codeQuestionsWithHeight = new ArrayList<>();
            List<QuestionWithHeight> regularQuestionsWithHeight = new ArrayList<>();

            for (int i = 0; i < selectedTemplates.size(); i++) {
                TemplateModel.QuestionTemplate question = selectedTemplates.get(i);
                float height = estimateContentHeight(question);
                QuestionWithHeight qwh = new QuestionWithHeight(question, height, i);

                if (question.getCode() != null && !question.getCode().isEmpty()) {
                    codeQuestionsWithHeight.add(qwh);
                } else {
                    regularQuestionsWithHeight.add(qwh);
                }
            }

            int totalQuestions = selectedTemplates.size();
            int codeQuestionCount = codeQuestionsWithHeight.size();
            int spacingBetweenCode = Math.max(3, totalQuestions / (codeQuestionCount + 1));

            int regularIndex = 0;
            int codeIndex = 0;
            int questionsUntilNextCode = spacingBetweenCode;

            while (regularIndex < regularQuestionsWithHeight.size() ||
                   codeIndex < codeQuestionsWithHeight.size()) {

                while (questionsUntilNextCode > 0 && regularIndex < regularQuestionsWithHeight.size()) {
                    questionsWithHeight.add(regularQuestionsWithHeight.get(regularIndex++));
                    questionsUntilNextCode--;
                }

                if (codeIndex < codeQuestionsWithHeight.size()) {
                    questionsWithHeight.add(codeQuestionsWithHeight.get(codeIndex++));
                    questionsUntilNextCode = spacingBetweenCode;
                }

                if (codeIndex >= codeQuestionsWithHeight.size()) {
                    while (regularIndex < regularQuestionsWithHeight.size()) {
                        questionsWithHeight.add(regularQuestionsWithHeight.get(regularIndex++));
                    }
                }
            }

            float maxColumnHeight = pageHeight - 30; // Reduced from 40

            while (!questionsWithHeight.isEmpty()) {
                Table mainTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                        .setMarginTop(15) // Reduced from 20
                        .setWidth(UnitValue.createPercentValue(100));

                Cell leftColumn = new Cell().setBorder(null).setPadding(8); // Reduced from 10
                List<QuestionWithHeight> leftColumnQuestions = new ArrayList<>();
                float leftHeight = 0;

                Iterator<QuestionWithHeight> leftIt = questionsWithHeight.iterator();
                while (leftIt.hasNext()) {
                    QuestionWithHeight qwh = leftIt.next();
                    if (leftHeight + qwh.height <= maxColumnHeight) {
                        leftColumnQuestions.add(qwh);
                        leftHeight += qwh.height;
                        leftIt.remove();
                    } else {
                        break;
                    }
                }

                Cell rightColumn = new Cell().setBorder(null).setPadding(8); // Reduced from 10
                List<QuestionWithHeight> rightColumnQuestions = new ArrayList<>();
                float rightHeight = 0;

                Iterator<QuestionWithHeight> rightIt = questionsWithHeight.iterator();
                while (rightIt.hasNext()) {
                    QuestionWithHeight qwh = rightIt.next();
                    if (rightHeight + qwh.height <= maxColumnHeight) {
                        rightColumnQuestions.add(qwh);
                        rightHeight += qwh.height;
                        rightIt.remove();
                    } else {
                        break;
                    }
                }

                leftColumnQuestions.sort(Comparator.comparingInt(q -> q.index));
                rightColumnQuestions.sort(Comparator.comparingInt(q -> q.index));

                for (QuestionWithHeight qwh : leftColumnQuestions) {
                    addQuestionToCell(leftColumn, qwh.question, qwh.index + 1,
                            titleFont, bodyFont, codeFont);
                }

                for (QuestionWithHeight qwh : rightColumnQuestions) {
                    addQuestionToCell(rightColumn, qwh.question, qwh.index + 1,
                            titleFont, bodyFont, codeFont);
                }

                mainTable.addCell(leftColumn);
                mainTable.addCell(rightColumn);
                document.add(mainTable);

                if (!questionsWithHeight.isEmpty()) {
                    document.add(new AreaBreak());
                }
            }

            System.out.println("PDF generisann.");
        }
    }

    private static float estimateContentHeight(TemplateModel.QuestionTemplate template) {
        float totalHeight = 0;
        float lineHeight = 14; // Base line height
        float containerPadding = 16; // Container padding

        // Add container padding
        totalHeight += containerPadding;

        // Calculate question text height with more accurate estimation
        int charsPerLine = 80; // Adjusted for better line estimation
        String questionText = template.getQuestion().trim();
        int questionLines = Math.max(1, (int) Math.ceil((float) questionText.length() / charsPerLine));
        totalHeight += (questionLines * lineHeight) + 10;

        // Multiple choice options with better spacing
        if (template.getType().equals("multiple_choice")) {
            float optionLineHeight = 10;
            float optionPadding = 4;
            for (String option : template.getOptions()) {
                int optionLines = Math.max(1, (int) Math.ceil((float) option.trim().length() / charsPerLine));
                totalHeight += (optionLines * optionLineHeight) + optionPadding;
            }
            totalHeight += 10;
        } else if (template.getType().equals("fill_in_the_blank")) {
            int blankCount = template.getQuestion().split("______________________").length - 1;
            totalHeight += (blankCount * 15);
        } else if (template.getType().equals("question")) {
            totalHeight += 40; // Space for written answers
        }

        // Code block with better height calculation
        if (template.getCode() != null && !template.getCode().isEmpty()) {
            totalHeight += 12; // Title space
            float codeLineHeight = 16;
            String[] codeLines = template.getCode().split("\n");
            float codeHeight = (codeLines.length * codeLineHeight);
            totalHeight += codeHeight + 25;
        }

        // Add margin between questions
        totalHeight += 15;

        return totalHeight + 10; // Additional buffer
    }

    private static void addQuestionToCell(Cell cell, TemplateModel.QuestionTemplate template,
                                           int questionNumber, PdfFont titleFont, PdfFont bodyFont, PdfFont codeFont) throws IOException {
        // Create a modern question container with consistent spacing
        Table questionContainer = new Table(1)
                .useAllAvailableWidth()
                .setBackgroundColor(new DeviceRgb(252, 252, 252))
                .setPadding(10)
                .setMarginBottom(12);

        // Question number and text with proper alignment
        Paragraph questionPara = generateQuestionParagraph(template, questionNumber, bodyFont, 11)
                .setMarginBottom(8)
                .setFixedLeading(14);

        questionContainer.addCell(new Cell().add(questionPara).setBorder(Border.NO_BORDER));

        // Multiple choice options with consistent spacing
        if (template.getType().equals("multiple_choice")) {
            char optionLetter = 'a';
            for (String option : template.getOptions()) {
                Table optionContainer = new Table(1)
                        .useAllAvailableWidth()
                        .setMarginLeft(12)  // Reduced from 15
                        .setMarginBottom(2); // Reduced from 4
                
                Paragraph optionPara = new Paragraph()
                        .add(new Text(optionLetter + ") ")
                                .setFont(titleFont)
                                .setFontColor(new DeviceRgb(52, 152, 219)))
                        .add(new Text(option.trim())
                                .setFont(bodyFont))
                        .setFontSize(10)
                        .setFixedLeading(12)  // Reduced from 14 for tighter line spacing
                        .setMultipliedLeading(1.0f); // Added for consistent spacing
                
                optionContainer.addCell(new Cell()
                        .add(optionPara)
                        .setBorder(Border.NO_BORDER)
                        .setPadding(6)  // Reduced from 8
                        .setBackgroundColor(new DeviceRgb(248, 249, 250))
                        .setBorderRadius(new BorderRadius(4)));
                
                questionContainer.addCell(new Cell().add(optionContainer).setBorder(Border.NO_BORDER));
                optionLetter++;
            }
        }

        // Code block with enhanced styling
        if (template.getCode() != null && !template.getCode().isEmpty()) {
            Paragraph codePara = new Paragraph("Primer koda:")
                    .setFont(titleFont)
                    .setFontSize(10)
                    .setFontColor(new DeviceRgb(41, 128, 185))
                    .setMarginTop(8)
                    .setMarginBottom(4);
            questionContainer.addCell(new Cell().add(codePara).setBorder(Border.NO_BORDER));

            Table codeTable = new Table(1).useAllAvailableWidth();
            Cell codeCell = new Cell()
                    .add(new Paragraph(formatCode(template.getCode()))
                            .setFont(codeFont)
                            .setFontSize(9)
                            .setFixedLeading(14))
                    .setBorder(new SolidBorder(new DeviceRgb(52, 152, 219), 0.5f))
                    .setBorderRadius(new BorderRadius(3))
                    .setBackgroundColor(new DeviceRgb(250, 250, 250))
                    .setPadding(8);
            codeTable.addCell(codeCell);
            questionContainer.addCell(new Cell().add(codeTable).setBorder(Border.NO_BORDER));
        }

        cell.add(questionContainer);
    }

    private static Paragraph generateQuestionParagraph(TemplateModel.QuestionTemplate question, int index,
                                                       PdfFont font, float fontSize) throws IOException {
        Paragraph p = new Paragraph();
        p.setFont(font)
         .setFontSize(fontSize)
         .setFixedLeading(14);
        
        // Add question number with proper formatting
        Text numberText = new Text(String.format("%d. ", index))
                .setFont(font)
                .setFontSize(fontSize);
        p.add(numberText);
        
        // Add question text
        p.add(question.getQuestion());
        
        // Add type-specific content
        switch (question.getType()) {
            case "multiple_choice":
                p.add("\n");
                break;
            case "fill_in_the_blank":
                // No additional formatting needed as blanks are in the question text
                break;
            case "question":
                // For simple questions, add space for answer with proper formatting
                p.add("\n\n");
                for (int i = 0; i < 3; i++) {
                    p.add(new Text("_".repeat(50)).setFont(font).setFontSize(fontSize));
                    p.add("\n");
                }
                break;
        }
        

        
        return p;
    }

    private static Table createNewPageTable() {
        return new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setMarginTop(30)
                .setMarginBottom(30);
    }

    private static Table createHeader(PdfFont titleFont, String testType, String subjectName) throws IOException {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(15);

        // Add left faculty logo
        Image leftLogo = new Image(ImageDataFactory.create("src/main/resources/imgs/tfzr.png"));
        leftLogo.setWidth(90);
        leftLogo.setHeight(90);

        Cell leftLogoCell = new Cell()
                .add(leftLogo)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setHorizontalAlignment(HorizontalAlignment.LEFT);
        headerTable.addCell(leftLogoCell);

        // Header title with reduced spacing
        Cell titleCell = new Cell()
                .add(new Paragraph(subjectName)
                        .setFont(titleFont)
                        .setFontSize(16)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(new DeviceRgb(41, 128, 185)))
                .add(new Paragraph(testType.toUpperCase())
                        .setFont(titleFont)
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(new DeviceRgb(52, 152, 219)))
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        headerTable.addCell(titleCell);

        // Add right faculty logo
        Image rightLogo = new Image(ImageDataFactory.create("src/main/resources/imgs/tfzr.png"));
        rightLogo.setWidth(90);
        rightLogo.setHeight(90);
        Cell rightLogoCell = new Cell()
                .add(rightLogo)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setHorizontalAlignment(HorizontalAlignment.RIGHT);
        headerTable.addCell(rightLogoCell);

        return headerTable;
    }

    private static String formatCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return "";
        }

        String[] segments = code.split("(?<=;)|(?<=\\{)|(?<=\\})");
        StringBuilder formattedCode = new StringBuilder();
        int indentLevel = 0;

        for (String segment : segments) {
            String line = segment.trim();

            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("}")) {
                indentLevel = Math.max(0, indentLevel - 1);
            }

            formattedCode.append("    ".repeat(indentLevel))
                    .append(line)
                    .append("\n");

            if (line.endsWith("{")) {
                indentLevel++;
            }
        }

        String result = formattedCode.toString().trim();

        if (!result.isEmpty()) {
            result += "\n";
        }

        return result;
    }
}