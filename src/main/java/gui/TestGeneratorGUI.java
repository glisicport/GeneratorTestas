package gui;

import generator.TemplateModel;
import generator.TestGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import service.QuestionService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.ArrayList;
import java.util.List;
import java.awt.Font;
import java.io.InputStream;

public class TestGeneratorGUI extends JFrame {
    private final JTextField numQuestionsField;
    private final JTextField outputPathField;
    private final JTextField templatePathField;
    private final JTextField subjectNameField;
    private final JButton generateButton;
    private final JButton browseButton;
    private final JButton browseTemplateButton;
    private final JComboBox<String> testTypeCombo;
    private final List<JCheckBox> chapterCheckboxes;
    private final JPanel chaptersPanel;
    private final QuestionService questionService;

    // Modern color scheme
    private final Color PRIMARY_COLOR = new Color(63, 81, 181);    // Material Indigo
    private final Color SECONDARY_COLOR = new Color(92, 107, 192); // Lighter Indigo
    private final Color BACKGROUND_COLOR = new Color(250, 250, 250);
    private final Color HEADER_COLOR = new Color(48, 63, 159);     // Darker Indigo
    private final Color TEXT_COLOR = new Color(33, 33, 33);
    private final Color BORDER_COLOR = new Color(224, 224, 224);
    
    // Fonts
    private Font HEADER_FONT;
    private Font TITLE_FONT;
    private Font REGULAR_FONT;
    
    // Dimensions
    private final int FIELD_HEIGHT = 30;
    private final int BUTTON_HEIGHT = 35;

    public TestGeneratorGUI() {
        initializeFonts();
        
        setTitle("TFZR Generator Testova");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(800, 800));

        // Create header panel with logo
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(HEADER_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        // Add TFZR logo
        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/imgs/tfyrin.png"));
            Image scaledImage = logoIcon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
            headerPanel.add(logoLabel);
        } catch (Exception e) {
            System.err.println("Could not load TFZR logo");
        }

        // Add title with enhanced styling
        JLabel titleLabel = new JLabel("Generator Testova");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        headerPanel.add(titleLabel);

        add(headerPanel, BorderLayout.NORTH);

        // Main Panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        mainPanel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Create input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        inputPanel.setBackground(BACKGROUND_COLOR);
        inputPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(inputPanel, gbc);

        GridBagConstraints inputGbc = new GridBagConstraints();
        inputGbc.fill = GridBagConstraints.HORIZONTAL;
        inputGbc.insets = new Insets(5, 5, 5, 5);

        // Subject Name Field
        inputGbc.gridx = 0;
        inputGbc.gridy = 0;
        JLabel subjectLabel = new JLabel("Naziv predmeta:");
        subjectLabel.setFont(REGULAR_FONT);
        inputPanel.add(subjectLabel, inputGbc);

        inputGbc.gridx = 1;
        inputGbc.gridy = 0;
        inputGbc.gridwidth = 2;
        subjectNameField = new JTextField("OBJEKTNO ORIJENTISANO PROGRAMIRANJE");
        subjectNameField.setPreferredSize(new Dimension(300, FIELD_HEIGHT));
        subjectNameField.setFont(REGULAR_FONT);
        inputPanel.add(subjectNameField, inputGbc);
        inputGbc.gridwidth = 1;

        // Template Selection
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel templateLabel = new JLabel("Šablon:");
        templateLabel.setFont(REGULAR_FONT);
        mainPanel.add(templateLabel, gbc);

        gbc.gridx = 1;
        templatePathField = new JTextField();
        templatePathField.setFont(REGULAR_FONT);
        templatePathField.setPreferredSize(new Dimension(300, FIELD_HEIGHT));
        mainPanel.add(templatePathField, gbc);

        gbc.gridx = 2;
        browseTemplateButton = new JButton("Izaberi");
        styleButton(browseTemplateButton, SECONDARY_COLOR);
        browseTemplateButton.addActionListener(e -> browseTemplateLocation());
        mainPanel.add(browseTemplateButton, gbc);

        // Test Type Selection
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        JLabel typeLabel = new JLabel("Tip testa:");
        typeLabel.setFont(REGULAR_FONT);
        mainPanel.add(typeLabel, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        testTypeCombo = new JComboBox<>(new String[]{"Kolokvijum", "Ispit"});
        testTypeCombo.setFont(REGULAR_FONT);
        testTypeCombo.setBackground(Color.WHITE);
        testTypeCombo.setPreferredSize(new Dimension(300, FIELD_HEIGHT));
        mainPanel.add(testTypeCombo, gbc);

        // Chapters Panel
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        // Create checkboxes panel
        chaptersPanel = new JPanel();
        chaptersPanel.setLayout(new GridLayout(0, 2, 10, 5));
        chaptersPanel.setBackground(Color.WHITE);
        chaptersPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        chapterCheckboxes = new ArrayList<>();

        // Create scroll pane with fixed height
        JScrollPane scrollPane = new JScrollPane(chaptersPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            "Izaberite poglavlja",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            TITLE_FONT
        ));
        scrollPane.setPreferredSize(new Dimension(300, 250));
        mainPanel.add(scrollPane, gbc);

        // Selection Buttons Panel
        gbc.gridy = 4;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel selectionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        selectionButtonsPanel.setBackground(BACKGROUND_COLOR);

        JButton selectAllButton = new JButton("Izaberi sve");
        JButton selectNoneButton = new JButton("Poništi izbor");
        styleButton(selectAllButton, SECONDARY_COLOR);
        styleButton(selectNoneButton, SECONDARY_COLOR);

        selectAllButton.addActionListener(e -> chapterCheckboxes.forEach(cb -> cb.setSelected(true)));
        selectNoneButton.addActionListener(e -> chapterCheckboxes.forEach(cb -> cb.setSelected(false)));

        selectionButtonsPanel.add(selectAllButton);
        selectionButtonsPanel.add(selectNoneButton);
        mainPanel.add(selectionButtonsPanel, gbc);

        // Number of Questions
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        JLabel questionsLabel = new JLabel("Broj pitanja:");
        questionsLabel.setFont(REGULAR_FONT);
        mainPanel.add(questionsLabel, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        numQuestionsField = new JTextField("10");
        numQuestionsField.setFont(REGULAR_FONT);
        numQuestionsField.setPreferredSize(new Dimension(300, FIELD_HEIGHT));
        mainPanel.add(numQuestionsField, gbc);

        // Output Path
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        JLabel locationLabel = new JLabel("Lokacija:");
        locationLabel.setFont(REGULAR_FONT);
        mainPanel.add(locationLabel, gbc);

        gbc.gridx = 1;
        outputPathField = new JTextField("test.pdf");
        outputPathField.setFont(REGULAR_FONT);
        outputPathField.setPreferredSize(new Dimension(300, FIELD_HEIGHT));
        mainPanel.add(outputPathField, gbc);

        gbc.gridx = 2;
        browseButton = new JButton("Izaberi");
        styleButton(browseButton, SECONDARY_COLOR);
        browseButton.addActionListener(e -> browseSaveLocation());
        mainPanel.add(browseButton, gbc);

        // Generate Button
        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(25, 8, 8, 8);
        generateButton = new JButton("Generiši Test");
        generateButton.setFont(TITLE_FONT);
        styleButton(generateButton, PRIMARY_COLOR);
        generateButton.setPreferredSize(new Dimension(200, BUTTON_HEIGHT));
        generateButton.addActionListener(e -> {
            try {
                String templatePath = templatePathField.getText();
                String outputPath = outputPathField.getText();
                String testType = (String) testTypeCombo.getSelectedItem();
                String subjectName = subjectNameField.getText();
                
                if (templatePath.isEmpty() || outputPath.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Molimo vas da popunite sva polja.",
                            "Greška",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int numQuestions;
                try {
                    numQuestions = Integer.parseInt(numQuestionsField.getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Broj pitanja mora biti validan broj.",
                            "Greška",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                List<String> selectedChapters = new ArrayList<>();
                for (JCheckBox checkBox : chapterCheckboxes) {
                    if (checkBox.isSelected()) {
                        selectedChapters.add(checkBox.getText());
                    }
                }

                if (selectedChapters.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Molimo vas da izaberete bar jedno poglavlje.",
                            "Greška",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                TestGenerator.setTemplatePath(templatePath);
                TestGenerator.generateTest(numQuestions, outputPath, testType, selectedChapters, subjectName);

                JOptionPane.showMessageDialog(this,
                        "Test je uspešno generisan!",
                        "Uspeh",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Greška prilikom generisanja testa: " + ex.getMessage(),
                        "Greška",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
        JPanel generateButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        generateButtonPanel.setBackground(BACKGROUND_COLOR);
        generateButtonPanel.add(generateButton);
        mainPanel.add(generateButtonPanel, gbc);

        add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);

        questionService = new QuestionService();
    }

    private void initializeFonts() {
        try {
            // First try to load from resources
            InputStream segoeStream = getClass().getResourceAsStream("/fonts/segoeui.ttf");
            if (segoeStream != null) {
                Font baseFont = Font.createFont(Font.TRUETYPE_FONT, segoeStream);
                HEADER_FONT = baseFont.deriveFont(Font.BOLD, 24f);
                TITLE_FONT = baseFont.deriveFont(Font.BOLD, 14f);
                REGULAR_FONT = baseFont.deriveFont(Font.PLAIN, 13f);
            } else {
                // Fallback to system fonts if resource not found
                HEADER_FONT = new Font("Dialog", Font.BOLD, 24);
                TITLE_FONT = new Font("Dialog", Font.BOLD, 14);
                REGULAR_FONT = new Font("Dialog", Font.PLAIN, 13);
            }
        } catch (Exception e) {
            System.err.println("Could not load custom font, falling back to system font");
            HEADER_FONT = new Font("Dialog", Font.BOLD, 24);
            TITLE_FONT = new Font("Dialog", Font.BOLD, 14);
            REGULAR_FONT = new Font("Dialog", Font.PLAIN, 13);
        }
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(REGULAR_FONT);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, FIELD_HEIGHT));

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
    }

    private void loadChaptersFromTemplate() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TemplateModel templateModel = objectMapper.readValue(
                new File(templatePathField.getText()),
                TemplateModel.class
            );
            
            // Clear existing checkboxes
            chaptersPanel.removeAll();
            chapterCheckboxes.clear();
            
            // Add checkboxes for each chapter
            for (TemplateModel.Chapter chapter : templateModel.getChapters()) {
                String chapterName = chapter.getName();
                System.out.println("Loading chapter: " + chapterName);
                JCheckBox checkbox = new JCheckBox(chapterName);
                checkbox.setFont(REGULAR_FONT);
                checkbox.setBackground(Color.WHITE);
                chapterCheckboxes.add(checkbox);
                chaptersPanel.add(checkbox);
            }
            
            // Update UI
            chaptersPanel.revalidate();
            chaptersPanel.repaint();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Greška pri učitavanju šablona: " + e.getMessage(),
                "Greška",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void browseTemplateLocation() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON files", "json"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            templatePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            loadChaptersFromTemplate();
        }
    }

    private void browseSaveLocation() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setSelectedFile(new File("test.pdf"));
        fileChooser.setDialogTitle("Sačuvaj test kao");
        
        // Set file filter for PDF files
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF files", "pdf"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            if (!path.toLowerCase().endsWith(".pdf")) {
                path += ".pdf";
            }
            outputPathField.setText(path);
        }
    }

    private void browseTemplate() {
        JFileChooser fileChooser = new JFileChooser("src/main/java/res");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON files", "json"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String selectedPath = fileChooser.getSelectedFile().getAbsolutePath();
            templatePathField.setText(selectedPath);
            TestGenerator.setTemplatePath(selectedPath);
            // questionService.setTemplatePath(selectedPath); // This line is commented out because questionService is not defined in this class
            loadChaptersFromTemplate();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new TestGeneratorGUI().setVisible(true);
        });
    }
}