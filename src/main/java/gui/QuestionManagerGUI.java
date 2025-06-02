package gui;

import model.Question;
import service.QuestionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestionManagerGUI extends JFrame {
    private final QuestionService questionService;
    private JComboBox<String> chapterComboBox;
    private JTable questionTable;
    private DefaultTableModel tableModel;
    private JTextArea questionPreview;
    private String currentChapter;
    private final Color PRIMARY_COLOR = new Color(63, 81, 181);    // Material Indigo
    private final Color SECONDARY_COLOR = new Color(92, 107, 192);  // Lighter Indigo
    private final Color BACKGROUND_COLOR = new Color(250, 250, 250);
    private final Color TEXT_COLOR = new Color(33, 33, 33);
    private final Color BORDER_COLOR = new Color(224, 224, 224);
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private final int FIELD_HEIGHT = 28;
    private final int FIELD_WIDTH = 350;
    private final int VERTICAL_GAP = 8;
    private final int HORIZONTAL_GAP = 10;

    public QuestionManagerGUI() {
        questionService = new QuestionService();
        showWelcomeScreen();
    }

    private void showWelcomeScreen() {
        JDialog welcomeDialog = new JDialog((Frame)null, "Menadžer Pitanja", true);
        welcomeDialog.setSize(600, 400);
        welcomeDialog.setLocationRelativeTo(null);
        welcomeDialog.setLayout(new BorderLayout());
        welcomeDialog.getContentPane().setBackground(BACKGROUND_COLOR);

        // Header Panel
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(30, 0, 30, 0));
        
        JLabel welcomeLabel = new JLabel("Menadžer Pitanja");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel);

        // Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(new EmptyBorder(40, 60, 40, 60));

        // Create Template Panel
        JPanel createPanel = createOptionPanel(
            "Novi Šablon",
            "Kreiraj novi šablon",
            e -> {
                welcomeDialog.dispose();
                showCreateTemplateDialog();
            }
        );

        // Open Template Panel
        JPanel openPanel = createOptionPanel(
            "Otvori Šablon",
            "Otvori postojeću šablon",
            e -> {
                welcomeDialog.dispose();
                showOpenTemplateDialog();
            }
        );

        contentPanel.add(createPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(openPanel);

        welcomeDialog.add(headerPanel, BorderLayout.NORTH);
        welcomeDialog.add(contentPanel, BorderLayout.CENTER);

        welcomeDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        welcomeDialog.setVisible(true);
    }

    // pomocna metoda za kreiranje panela sa opcijom
    private JPanel createOptionPanel(String title, String description, java.awt.event.ActionListener listener) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        panel.setMaximumSize(new Dimension(450, 100));
        
        // panel za tekst
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(REGULAR_FONT);
        descLabel.setForeground(Color.GRAY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(descLabel);
        
        // dugme za akciju
        JButton button = new JButton("Izaberi");
        styleButton(button, SECONDARY_COLOR);
        
        panel.add(textPanel, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
        
        // hover efekat
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                panel.setBackground(new Color(250, 250, 250));
                textPanel.setBackground(new Color(250, 250, 250));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                panel.setBackground(Color.WHITE);
                textPanel.setBackground(Color.WHITE);
            }
        });
        
        button.addActionListener(listener);
        return panel;
    }

    // stilizovanje dugmeta
    private void styleButton(JButton button, Color bgColor) {
        button.setFont(REGULAR_FONT);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, FIELD_HEIGHT));

        // hover efekat za dugme
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
    }

    private void showCreateTemplateDialog() {
        JDialog dialog = new JDialog(this, "Novi Šablon", true);
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Template Name
        JLabel nameLabel = new JLabel("Naziv Šablona");
        nameLabel.setFont(HEADER_FONT);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField nameField = new JTextField(20);
        nameField.setFont(REGULAR_FONT);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // File Location
        JLabel locationLabel = new JLabel("Lokacija Čuvanja");
        locationLabel.setFont(HEADER_FONT);
        locationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel locationPanel = new JPanel();
        locationPanel.setLayout(new BoxLayout(locationPanel, BoxLayout.X_AXIS));
        locationPanel.setBackground(BACKGROUND_COLOR);
        locationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        locationPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JTextField locationField = new JTextField(20);
        locationField.setFont(REGULAR_FONT);
        locationField.setEditable(false);
        locationField.setText("src/main/java/res/");

        JButton browseButton = new JButton("Pretraži");
        styleButton(browseButton, SECONDARY_COLOR);
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Izaberi Lokaciju");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setCurrentDirectory(new File("src/main/java/res/"));
            
            if (fileChooser.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                locationField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        locationPanel.add(locationField);
        locationPanel.add(Box.createHorizontalStrut(10));
        locationPanel.add(browseButton);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        JButton cancelButton = new JButton("Otkaži");
        styleButton(cancelButton, Color.GRAY);
        
        JButton createButton = new JButton("Kreiraj");
        styleButton(createButton, SECONDARY_COLOR);

        cancelButton.addActionListener(e -> dialog.dispose());
        createButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String location = locationField.getText().trim();
            
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Naziv šablona ne može biti prazan", "Greška", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!name.toLowerCase().endsWith(".json")) {
                name += ".json";
            }

            try {
                String fullPath = new File(location, name).getAbsolutePath();
                questionService.setTemplatePath(fullPath);
                questionService.createNewTemplate(name);
                setupUI();
                setVisible(true);
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Šablona je uspešno kreirana u: " + fullPath, "Informacija", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Greška pri kreiranju šablone", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(createButton);

        // Add components
        contentPanel.add(nameLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(nameField);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(locationLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(locationPanel);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Set default button
        dialog.getRootPane().setDefaultButton(createButton);
        dialog.setVisible(true);
    }

    private void showOpenTemplateDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Otvori Šablon");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON Files", "json"));
        fileChooser.setCurrentDirectory(new File("src/main/java/res/"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                questionService.loadTemplate(selectedFile.getAbsolutePath());
                setupUI();
                loadChapters(); // Explicitly load chapters after setting up UI
                setVisible(true);
                JOptionPane.showMessageDialog(this, "Šablona je uspešno otvorena: " + selectedFile.getName(), "Informacija", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Greška pri otvaranju šablone", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // postavljanje glavnog interfejsa
    private void setupUI() {
        setTitle("Menadzer Pitanja");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // postavljanje izgleda
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // navigaciona traka
        JToolBar navBar = createNavigationBar();
        add(navBar, BorderLayout.NORTH);

        // glavni panel sa listom pitanja i pregledom
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(800);
        mainSplitPane.setResizeWeight(0.7);

        // levi panel sa izborom poglavlja i tabelom pitanja
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // izbor poglavlja
        JPanel chapterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        chapterPanel.setBackground(Color.WHITE);
        JLabel chapterLabel = new JLabel("Dodaj Poglavlje");
        chapterLabel.setFont(HEADER_FONT);
        chapterComboBox = new JComboBox<>();
        chapterComboBox.setFont(REGULAR_FONT);
        chapterComboBox.setPreferredSize(new Dimension(200, 30));
        chapterPanel.add(chapterLabel);
        chapterPanel.add(chapterComboBox);
        leftPanel.add(chapterPanel, BorderLayout.NORTH);

        // tabela pitanja
        String[] columns = {"Tip", "Pitanje", "Tacan Odgovor"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        questionTable = new JTable(tableModel);
        questionTable.setFont(REGULAR_FONT);
        questionTable.setRowHeight(25);
        questionTable.setShowGrid(false);
        questionTable.setIntercellSpacing(new Dimension(0, 0));
        
        // zaglavlje tabele
        JTableHeader header = questionTable.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);

        JScrollPane tableScrollPane = new JScrollPane(questionTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        leftPanel.add(tableScrollPane, BorderLayout.CENTER);

        // panel sa dugmicima
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton addButton = createStyledButton("Dodaj Pitanje", "/icons/add.png");
        JButton editButton = createStyledButton("Izmeni", "/icons/edit.png");
        JButton deleteButton = createStyledButton("Obrisi", "/icons/delete.png");
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);

        // desni panel sa pregledom pitanja
        questionPreview = new JTextArea();
        questionPreview.setFont(new Font("Monospaced", Font.PLAIN, 12));
        questionPreview.setEditable(false);
        questionPreview.setBorder(new EmptyBorder(10, 10, 10, 10));
        questionPreview.setLineWrap(true);
        questionPreview.setWrapStyleWord(true);
        JScrollPane previewScrollPane = new JScrollPane(questionPreview);
        previewScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            "Pregled Pitanja",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            HEADER_FONT
        ));

        mainSplitPane.setLeftComponent(leftPanel);
        mainSplitPane.setRightComponent(previewScrollPane);
        add(mainSplitPane, BorderLayout.CENTER);

        // dodavanje event listenera
        setupEventListeners(addButton, editButton, deleteButton);
    }

    // kreiranje navigacione trake
    private JToolBar createNavigationBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(PRIMARY_COLOR);
        toolBar.setBorder(new EmptyBorder(5, 5, 5, 5));


        JButton addChapterBtn = createNavButton("Dodaj Poglavlje", "/icons/add_chapter.png");



        toolBar.add(addChapterBtn);


        addChapterBtn.addActionListener(e -> showAddChapterDialog());

        return toolBar;
    }

    // kreiranje navigacionog dugmeta
    private JButton createNavButton(String text, String iconPath) {
        JButton button = new JButton(text);
        button.setFont(REGULAR_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(true);
        return button;
    }

    private JButton createStyledButton(String text, String iconPath) {
        JButton button = new JButton(text);
        button.setFont(REGULAR_FONT);
        button.setBackground(SECONDARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(130, 35));
        return button;
    }

    private void setupEventListeners(JButton addButton, JButton editButton, JButton deleteButton) {
        chapterComboBox.addActionListener(e -> {
            if (chapterComboBox.getSelectedItem() != null) {
                currentChapter = chapterComboBox.getSelectedItem().toString();
                loadQuestions(currentChapter);
            }
        });

        questionTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && questionTable.getSelectedRow() != -1) {
                updatePreview();
            }
        });

        addButton.addActionListener(e -> addNewQuestion());
        editButton.addActionListener(e -> handleEditQuestion());
        deleteButton.addActionListener(e -> handleDeleteQuestion());
    }

    private void handleEditQuestion() {
        int selectedRow = questionTable.getSelectedRow();
        if (selectedRow != -1) {
            try {
                List<Question> questions = questionService.getQuestionsByChapter(currentChapter);
                showQuestionDialog(questions.get(selectedRow), selectedRow);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Greska pri ucitavanju pitanja", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Molimo vas da izaberete pitanje za izmenu.", "Informacija", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleDeleteQuestion() {
        int selectedRow = questionTable.getSelectedRow();
        if (selectedRow != -1) {
            if (JOptionPane.showConfirmDialog(this, "Da li ste sigurni da zelite da obrisete ovo pitanje?", "Potvrda", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    questionService.deleteQuestion(currentChapter, selectedRow);
                    loadQuestions(currentChapter);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Greska pri brisanju pitanja", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Molimo vas da izaberete pitanje za brisanje.", "Informacija", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void createNewTemplate() {
        String name = JOptionPane.showInputDialog(this, 
            "Unesite naziv za novu šablonu:", 
            "Novi Šablon", 
            JOptionPane.PLAIN_MESSAGE);
        
        if (name != null && !name.trim().isEmpty()) {
            try {
                questionService.createNewTemplate(name);
                loadChapters();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Greška pri kreiranju šablone", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void loadChapters() {
        if (chapterComboBox != null) {
            try {
                List<String> chapters = questionService.getChapters();
                chapterComboBox.removeAllItems();
                for (String chapter : chapters) {
                    chapterComboBox.addItem(chapter);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Greška pri učitavanju poglavlja", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadQuestions(String chapter) {
        tableModel.setRowCount(0);
        try {
            List<Question> questions = questionService.getQuestionsByChapter(chapter);
            for (Question q : questions) {
                tableModel.addRow(new Object[]{q.getType(), q.getQuestion(), q.getCorrect_answer()});
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Greška pri učitavanju pitanja", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePreview() {
        try {
            List<Question> questions = questionService.getQuestionsByChapter(currentChapter);
            Question question = questions.get(questionTable.getSelectedRow());
            StringBuilder preview = new StringBuilder();
            preview.append("Tip: ").append(question.getType().equals("multiple_choice") ? "Višestruki izbor" : 
                                         question.getType().equals("fill_in_the_blank") ? "Popuni prazninu" : 
                                         question.getType()).append("\n\n");
            
            // Format question based on type
            String questionText = question.getQuestion();
            if (question.getType().equals("fill_in_the_blank")) {
                // For fill-in-the-blank, show the blank where specified with underscores
                String answer = question.getCorrect_answer();
                if (answer != null && !answer.isEmpty()) {
                    questionText = questionText.replace("___", "_".repeat(Math.max(answer.length(), 10)));
                }
            }
            preview.append("Pitanje: ").append(questionText).append("\n\n");
            
            if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                preview.append("Opcije:\n");
                char optionLetter = 'A';
                for (String option : question.getOptions()) {
                    preview.append(optionLetter++).append(") ").append(option).append("\n");
                }
                preview.append("\n");
            }
            
            if (question.getCode() != null && !question.getCode().isEmpty()) {
                preview.append("Kod:\n").append(question.getCode()).append("\n\n");
            }
            
            preview.append("Tačan odgovor: ").append(question.getCorrect_answer());
            questionPreview.setText(preview.toString());
            questionPreview.setCaretPosition(0);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Greška pri učitavanju pregleda pitanja", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addNewQuestion() {
        if (currentChapter == null) {
            JOptionPane.showMessageDialog(this, "Molimo vas da prvo izaberete poglavlje", "Informacija", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Dodaj Pitanje", true);
        dialog.setSize(900, 650);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);

        // Initialize lists
        List<JTextField> optionFields = new ArrayList<>();

        // Main split pane with fixed divider
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(450);
        splitPane.setResizeWeight(0.5);
        splitPane.setBackground(BACKGROUND_COLOR);
        splitPane.setBorder(null);
        splitPane.setDividerSize(1);
        splitPane.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        // Left panel with GridBagLayout for precise control
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(BACKGROUND_COLOR);
        leftPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;

        // Question Type
        JLabel typeLabel = new JLabel("Tip Pitanja");
        typeLabel.setFont(HEADER_FONT);
        addComponent(leftPanel, typeLabel, gbc, 0);

        JComboBox<String> typeCombo = new JComboBox<>(new String[]{
            "multiple_choice", "fill_in_the_blank", "question"
        });
        typeCombo.setFont(REGULAR_FONT);
        typeCombo.setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        addComponent(leftPanel, typeCombo, gbc, 5);

        // Question Text
        JLabel questionLabel = new JLabel("Tekst Pitanja");
        questionLabel.setFont(HEADER_FONT);
        addComponent(leftPanel, questionLabel, gbc, 15);

        JTextArea questionArea = createStyledTextArea(4, 20);
        JScrollPane questionScroll = new JScrollPane(questionArea);
        questionScroll.setPreferredSize(new Dimension(FIELD_WIDTH, 100));
        addComponent(leftPanel, questionScroll, gbc, 5);

        // Code Section
        JLabel codeLabel = new JLabel("Kod");
        codeLabel.setFont(HEADER_FONT);
        addComponent(leftPanel, codeLabel, gbc, 15);

        JTextArea codeArea = createStyledTextArea(4, 20);
        codeArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        JScrollPane codeScroll = new JScrollPane(codeArea);
        codeScroll.setPreferredSize(new Dimension(FIELD_WIDTH, 100));
        addComponent(leftPanel, codeScroll, gbc, 5);

        // Options Section
        JLabel optionsLabel = new JLabel("Opcije");
        optionsLabel.setFont(HEADER_FONT);
        addComponent(leftPanel, optionsLabel, gbc, 15);

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBackground(BACKGROUND_COLOR);
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        addComponent(leftPanel, optionsPanel, gbc, 5);

        // Add Option Button
        JButton addOptionBtn = new JButton("Dodaj Opciju");
        addOptionBtn.setBackground(new Color(94, 99, 173));
        addOptionBtn.setForeground(Color.WHITE);
        addOptionBtn.setFocusPainted(false);
        addOptionBtn.setBorderPainted(false);
        addOptionBtn.setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        addComponent(leftPanel, addOptionBtn, gbc, 5);

        // Correct Answer
        JLabel answerLabel = new JLabel("Tačan Odgovor");
        answerLabel.setFont(HEADER_FONT);
        addComponent(leftPanel, answerLabel, gbc, 15);

        JTextField correctAnswerField = createStyledTextField(20);
        correctAnswerField.setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        addComponent(leftPanel, correctAnswerField, gbc, 5);

        // Preview Panel
        JPanel rightPanel = new JPanel(new BorderLayout(0, 5));
        rightPanel.setBackground(BACKGROUND_COLOR);
        rightPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel previewLabel = new JLabel("Pregled Pitanja");
        previewLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        previewLabel.setForeground(new Color(63, 81, 181));
        
        JTextArea previewArea = createStyledTextArea(20, 30);
        previewArea.setEditable(false);
        JScrollPane previewScroll = new JScrollPane(previewArea);
        previewScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        rightPanel.add(previewLabel, BorderLayout.NORTH);
        rightPanel.add(previewScroll, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JButton cancelButton = new JButton("Otkaži");
        styleButton(cancelButton, new Color(158, 158, 158));
        
        JButton saveButton = new JButton("Sačuvaj");
        styleButton(saveButton, new Color(63, 81, 181));

        // Add event handlers
        cancelButton.addActionListener(e -> dialog.dispose());
        saveButton.addActionListener(e -> {
            String questionText = questionArea.getText().trim();
            String correctAnswer = correctAnswerField.getText().trim();
            
            if (!validateForm(questionText, correctAnswer)) {
                return;
            }
            
            try {
                Question question = new Question();
                question.setType((String) typeCombo.getSelectedItem());
                question.setQuestion(questionText);
                
                if (typeCombo.getSelectedItem().equals("multiple_choice")) {
                    List<String> options = new ArrayList<>();
                    for (JTextField field : optionFields) {
                        options.add(field.getText().trim());
                    }
                    question.setOptions(options);
                }
                
                String code = codeArea.getText().trim();
                if (!code.isEmpty()) {
                    question.setCode(code);
                }
                
                question.setCorrect_answer(correctAnswer);
                
                questionService.addQuestion(currentChapter, question);
                refreshQuestionList();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Pitanje je uspešno dodato", "Informacija", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Greška pri dodavanju pitanja", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Live preview listeners
        DocumentListener previewUpdater = createPreviewUpdater(
            typeCombo, questionArea, optionFields, codeArea, correctAnswerField, previewArea
        );
        
        questionArea.getDocument().addDocumentListener(previewUpdater);
        codeArea.getDocument().addDocumentListener(previewUpdater);
        correctAnswerField.getDocument().addDocumentListener(previewUpdater);

        // Add option button functionality
        addOptionBtn.addActionListener(e -> {
            JPanel optionPanel = new JPanel(new BorderLayout(5, 0));
            optionPanel.setBackground(BACKGROUND_COLOR);
            optionPanel.setMaximumSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
            
            JTextField optionField = createStyledTextField(20);
            optionFields.add(optionField);
            optionField.getDocument().addDocumentListener(previewUpdater);
            
            JButton removeBtn = new JButton("×");
            removeBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
            styleButton(removeBtn, new Color(239, 83, 80));
            removeBtn.setPreferredSize(new Dimension(FIELD_HEIGHT, FIELD_HEIGHT));
            
            removeBtn.addActionListener(evt -> {
                optionFields.remove(optionField);
                optionsPanel.remove(optionPanel);
                optionsPanel.revalidate();
                optionsPanel.repaint();
                updatePreview(typeCombo, questionArea, optionFields, codeArea, correctAnswerField, previewArea);
            });
            
            optionPanel.add(optionField, BorderLayout.CENTER);
            optionPanel.add(removeBtn, BorderLayout.EAST);
            
            optionsPanel.add(optionPanel);
            optionsPanel.add(Box.createVerticalStrut(5));
            optionsPanel.revalidate();
            optionsPanel.repaint();
            updatePreview(typeCombo, questionArea, optionFields, codeArea, correctAnswerField, previewArea);
        });

        // Type change listener
        typeCombo.addActionListener(e -> {
            String selectedType = (String) typeCombo.getSelectedItem();
            
            // Show/hide options panel only for multiple choice
            optionsLabel.setVisible(selectedType.equals("multiple_choice"));
            optionsPanel.setVisible(selectedType.equals("multiple_choice"));
            addOptionBtn.setVisible(selectedType.equals("multiple_choice"));
            



            // Adjust answer field label and tooltip based on type
            if (selectedType.equals("multiple_choice")) {
                answerLabel.setText("Tačan Odgovor (a, b, c...)");
                correctAnswerField.setToolTipText("Unesite slovo tačnog odgovora");
            } else if (selectedType.equals("fill_in_the_blank")) {
                answerLabel.setText("Reč/Fraza za Prazninu");
                correctAnswerField.setToolTipText("Unesite reč ili frazu koja treba da se upiše u prazninu");
            } else {
                answerLabel.setText("Tačan Odgovor");
                correctAnswerField.setToolTipText("Unesite tačan odgovor na pitanje");
            }
            
            // Clear options if switching from multiple choice
            if (!selectedType.equals("multiple_choice")) {
                optionFields.clear();
                optionsPanel.removeAll();
                optionsPanel.revalidate();
                optionsPanel.repaint();
            }
            
            // Adjust layout
            leftPanel.revalidate();
            leftPanel.repaint();
            
            // Update preview
            updatePreview(typeCombo, questionArea, optionFields, codeArea, correctAnswerField, previewArea);
        });

        // Initial visibility
        String initialType = (String) typeCombo.getSelectedItem();
        optionsLabel.setVisible(initialType.equals("multiple_choice"));
        optionsPanel.setVisible(initialType.equals("multiple_choice"));
        addOptionBtn.setVisible(initialType.equals("multiple_choice"));
        codeLabel.setVisible(!initialType.equals("question"));
        codeScroll.setVisible(!initialType.equals("question"));

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        // Add panels to split pane
        splitPane.setLeftComponent(new JScrollPane(leftPanel));
        splitPane.setRightComponent(rightPanel);

        // Add to dialog
        dialog.add(splitPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }

    private void addComponent(JPanel panel, JComponent comp, GridBagConstraints gbc, int topPadding) {
        gbc.insets.top = topPadding;
        panel.add(comp, gbc);
    }

    private boolean validateForm(String questionText, String correctAnswer) {
        if (questionText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tekst pitanja ne može biti prazan", "Greška", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (correctAnswer.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tačan odgovor ne može biti prazan", "Greška", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }

    private void refreshQuestionList() {
        loadQuestions(currentChapter);
        questionTable.clearSelection();
        questionPreview.setText("");
    }

    private DocumentListener createPreviewUpdater(
            JComboBox<String> typeCombo,
            JTextArea questionArea,
            List<JTextField> optionFields,
            JTextArea codeArea,
            JTextField correctAnswerField,
            JTextArea previewArea) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePreview(typeCombo, questionArea, optionFields, codeArea, correctAnswerField, previewArea);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePreview(typeCombo, questionArea, optionFields, codeArea, correctAnswerField, previewArea);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePreview(typeCombo, questionArea, optionFields, codeArea, correctAnswerField, previewArea);
            }
        };
    }

    private void updatePreview(
            JComboBox<String> typeCombo,
            JTextArea questionArea,
            List<JTextField> optionFields,
            JTextArea codeArea,
            JTextField correctAnswerField,
            JTextArea previewArea) {
        StringBuilder preview = new StringBuilder();
        String type = (String) typeCombo.getSelectedItem();
        String questionText = questionArea.getText().trim();
        String code = codeArea.getText().trim();
        String correctAnswer = correctAnswerField.getText().trim();

        preview.append("═══════════════ PREGLED PITANJA ═══════════════\n\n");
        
        // Question type with icon
        preview.append("📝 Tip: ").append(type).append("\n\n");
        
        // Question text with formatting
        if (!questionText.isEmpty()) {
            preview.append("❓ Tekst Pitanja:\n").append(questionText).append("\n\n");
        }
        
        // Code section if present
        if (!code.isEmpty()) {
            preview.append("💻 Kod:\n").append(code).append("\n\n");
        }
        
        // Options with better formatting
        if (type.equals("multiple_choice") && !optionFields.isEmpty()) {
            preview.append("🔤 Opcije:\n");
            char optionLetter = 'A';
            for (JTextField field : optionFields) {
                String optionText = field.getText().trim();
                if (!optionText.isEmpty()) {
                    preview.append("  ").append(optionLetter).append(") ").append(optionText).append("\n");
                    optionLetter++;
                }
            }
            preview.append("\n");
        }
        
        // Correct answer if present
        if (!correctAnswer.isEmpty()) {
            preview.append("✅ Tačan Odgovor: ").append(correctAnswer);
        }

        previewArea.setText(preview.toString());
        previewArea.setCaretPosition(0);
    }

    private JTextArea createStyledTextArea(int rows, int cols) {
        JTextArea textArea = new JTextArea(rows, cols);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return textArea;
    }

    private JTextField createStyledTextField(int cols) {
        JTextField textField = new JTextField(cols);
        textField.setFont(REGULAR_FONT);
        textField.setMaximumSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        return textField;
    }
    private void showAddChapterDialog() {
        JDialog dialog = new JDialog(this, "Dodaj Novo Poglavlje", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Chapter Name Input
        JLabel nameLabel = new JLabel("Naziv Poglavlja");
        nameLabel.setFont(HEADER_FONT);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField nameField = createStyledTextField(20);
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        JButton cancelButton = new JButton("Otkaži");
        styleButton(cancelButton, Color.GRAY);
        
        JButton addButton = new JButton("Dodaj");
        styleButton(addButton, SECONDARY_COLOR);

        cancelButton.addActionListener(e -> dialog.dispose());
        addButton.addActionListener(e -> {
            String chapterName = nameField.getText().trim();
            if (chapterName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Naziv poglavlja ne može biti prazan", "Greška", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                questionService.addChapter(chapterName);
                loadChapters();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Poglavlje '" + chapterName + "' je uspešno dodato", "Informacija", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Greška pri dodavanju poglavlja", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(addButton);

        // Add components
        contentPanel.add(nameLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(nameField);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Set default button and key bindings
        dialog.getRootPane().setDefaultButton(addButton);
        nameField.addActionListener(e -> addButton.doClick());

        dialog.setVisible(true);
    }

    private void showQuestionDialog(Question questionToEdit, int questionIndex) {
        JDialog dialog = new JDialog(this, questionToEdit == null ? "Dodaj Pitanje" : "Izmeni Pitanje", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(600, 700);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Type
        addFormField(formPanel, gbc, 0, "Tip Pitanja", new JComboBox<>(new String[]{
            "multiple_choice", "fill_in_the_blank", "question"
        }));
        JComboBox<String> typeCombo = (JComboBox<String>) getLastComponent(formPanel);

        // Question
        addFormField(formPanel, gbc, 1, "Tekst Pitanja", new JScrollPane(createStyledTextArea(3, 40)));
        JTextArea questionArea = (JTextArea) ((JScrollPane) getLastComponent(formPanel)).getViewport().getView();

        // Options
        addFormField(formPanel, gbc, 2, "Opcije", new JScrollPane(createStyledTextArea(3, 40)));
        JTextArea optionsArea = (JTextArea) ((JScrollPane) getLastComponent(formPanel)).getViewport().getView();

        // Code
        addFormField(formPanel, gbc, 3, "Kod", new JScrollPane(createStyledTextArea(5, 40)));
        JTextArea codeArea = (JTextArea) ((JScrollPane) getLastComponent(formPanel)).getViewport().getView();

        // Correct Answer
        addFormField(formPanel, gbc, 4, "Tačan Odgovor", createStyledTextField(40));
        JTextField correctAnswerField = (JTextField) getLastComponent(formPanel);

        if (questionToEdit != null) {
            typeCombo.setSelectedItem(questionToEdit.getType());
            questionArea.setText(questionToEdit.getQuestion());
            if (questionToEdit.getOptions() != null) {
                optionsArea.setText(String.join(", ", questionToEdit.getOptions()));
            }
            if (questionToEdit.getCode() != null) {
                codeArea.setText(questionToEdit.getCode());
            }
            correctAnswerField.setText(questionToEdit.getCorrect_answer());
        }

        dialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton saveButton = new JButton("Sačuvaj");
        styleButton(saveButton, PRIMARY_COLOR);
        
        JButton cancelButton = new JButton("Otkaži");
        styleButton(cancelButton, Color.GRAY);
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> {
            if (validateForm(questionArea.getText(), correctAnswerField.getText())) {
                try {
                    Question question = new Question();
                    question.setType((String) typeCombo.getSelectedItem());
                    question.setQuestion(questionArea.getText().trim());
                    question.setCorrect_answer(correctAnswerField.getText().trim());
                    question.setChapter(currentChapter);

                    String optionsText = optionsArea.getText().trim();
                    if (!optionsText.isEmpty()) {
                        List<String> options = Arrays.asList(optionsText.split("\\s*,\\s*"));
                        question.setOptions(options);
                    }

                    String code = codeArea.getText().trim();
                    if (!code.isEmpty()) {
                        question.setCode(code);
                    }

                    if (questionToEdit == null) {
                        questionService.addQuestion(currentChapter, question);
                    } else {
                        questionService.updateQuestion(currentChapter, questionIndex, question);
                    }

                    loadQuestions(currentChapter);
                    dialog.dispose();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Greška pri snimanju pitanja", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(HEADER_FONT);
        panel.add(jLabel, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(field, gbc);
    }

    private Component getLastComponent(Container container) {
        return container.getComponent(container.getComponentCount() - 1);
    }

    public static void main(String[] args) {
        try {
            // Set system look and feel for better native appearance
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Set better rendering hints for fonts
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create and show GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                QuestionManagerGUI gui = new QuestionManagerGUI();
                gui.setTitle("Menadžer Pitanja");
                gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                gui.pack();
                gui.setLocationRelativeTo(null); // Center on screen
                gui.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Error starting application: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
