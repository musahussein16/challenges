package diarymanagergui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.collections.*;

public class DiaryManagerGUI extends Application {
    
    // Application colors
    private static final String PRIMARY_COLOR = "#4A90E2";
    private static final String SECONDARY_COLOR = "#50E3C2";
    private static final String ACCENT_COLOR = "#F5A623";
    private static final String DARK_BG = "#1A1A2E";
    private static final String DARK_CARD = "#16213E";
    private static final String DARK_TEXT = "#E6E6E6";
    private static final String DARK_TEXT_SECONDARY = "#A0A0A0";
    
    private static final String LIGHT_BG = "#F8F9FA";
    private static final String LIGHT_CARD = "#FFFFFF";
    private static final String LIGHT_TEXT = "#212529";
    private static final String LIGHT_TEXT_SECONDARY = "#6C757D";
    
    // UI Components
    private BorderPane root;
    private TextArea entryContent;
    private ListView<String> entriesListView;
    private TextField searchField;
    private TextField titleField;
    private ComboBox<String> categoryCombo;
    private ComboBox<String> moodCombo;
    private Label statusLabel;
    private ToggleButton darkModeToggle;
    private TabPane mainTabPane;
    private TextArea readArea;
    private Label entryTitleLabel;
    
    // Data
    private ObservableList<String> entries = FXCollections.observableArrayList();
    private File diaryDirectory;
    private boolean darkMode = true;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Personal Diary Manager");
        
        // Create diary directory
        diaryDirectory = new File(System.getProperty("user.home"), "PersonalDiary");
        if (!diaryDirectory.exists()) {
            diaryDirectory.mkdirs();
        }
        
        // Initialize UI
        initializeUI();
        
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        
        // Load existing entries
        loadEntries();
        
        primaryStage.show();
    }
    
    private void initializeUI() {
        root = new BorderPane();
        root.setPadding(new Insets(10));
        applyTheme();
        
        // Create components
        root.setTop(createHeader());
        root.setLeft(createNavigation());
        root.setCenter(createContent());
        root.setBottom(createFooter());
    }
    
    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-background-radius: 10px;");
        
        // Logo/Title
        Label title = new Label("📖 Personal Diary Manager");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        // Theme toggle
        darkModeToggle = new ToggleButton("🌙 Dark Mode");
        darkModeToggle.setSelected(true);
        darkModeToggle.setStyle("-fx-background-color: white; -fx-text-fill: " + PRIMARY_COLOR + 
                               "; -fx-font-weight: bold; -fx-padding: 8px 15px; -fx-background-radius: 20px;");
        
        darkModeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            darkMode = newVal;
            darkModeToggle.setText(newVal ? "🌙 Dark Mode" : "☀️ Light Mode");
            toggleTheme();
        });
        
        header.getChildren().addAll(title);
        HBox.setHgrow(title, Priority.ALWAYS);
        header.getChildren().add(darkModeToggle);
        
        return header;
    }
    
    private VBox createNavigation() {
        VBox navigation = new VBox(15);
        navigation.setPadding(new Insets(20));
        navigation.setPrefWidth(250);
        navigation.setStyle("-fx-background-color: " + (darkMode ? DARK_CARD : LIGHT_CARD) + 
                          "; -fx-background-radius: 10px;");
        
        Label navTitle = new Label("Recent Entries");
        navTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + 
                         (darkMode ? DARK_TEXT : LIGHT_TEXT) + ";");
        
        // Entry list
        entriesListView = new ListView<>(entries);
        entriesListView.setPrefHeight(400);
        entriesListView.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        // Double-click to read
        entriesListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selected = entriesListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    loadEntry(selected);
                    mainTabPane.getSelectionModel().select(1); // Switch to read tab
                }
            }
        });
        
        // New entry button
        Button newEntryBtn = new Button("📝 New Entry");
        newEntryBtn.setMaxWidth(Double.MAX_VALUE);
        newEntryBtn.setStyle(getButtonStyle(PRIMARY_COLOR));
        newEntryBtn.setOnAction(e -> {
            titleField.clear();
            entryContent.clear();
            categoryCombo.setValue("Personal");
            moodCombo.setValue("😊 Happy");
            mainTabPane.getSelectionModel().select(0);
        });
        
        navigation.getChildren().addAll(navTitle, entriesListView, newEntryBtn);
        
        return navigation;
    }
    
    private BorderPane createContent() {
        BorderPane content = new BorderPane();
        content.setStyle("-fx-background-color: transparent;");
        
        mainTabPane = new TabPane();
        mainTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Write Tab
        Tab writeTab = new Tab("Write Entry");
        writeTab.setContent(createWritePanel());
        
        // Read Tab
        Tab readTab = new Tab("Read Entry");
        readTab.setContent(createReadPanel());
        
        // Search Tab
        Tab searchTab = new Tab("Search");
        searchTab.setContent(createSearchPanel());
        
        mainTabPane.getTabs().addAll(writeTab, readTab, searchTab);
        
        content.setCenter(mainTabPane);
        return content;
    }
    
    private VBox createWritePanel() {
        VBox writePanel = new VBox(15);
        writePanel.setPadding(new Insets(20));
        writePanel.setStyle("-fx-background-color: " + (darkMode ? DARK_CARD : LIGHT_CARD) + 
                          "; -fx-background-radius: 10px;");
        
        // Title
        Label titleLabel = new Label("Title:");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + 
                           (darkMode ? DARK_TEXT : LIGHT_TEXT) + ";");
        
        titleField = new TextField();
        titleField.setPromptText("Enter entry title...");
        titleField.setStyle(getTextFieldStyle());
        
        // Category and mood
        HBox optionsBox = new HBox(15);
        optionsBox.setAlignment(Pos.CENTER_LEFT);
        
        Label categoryLabel = new Label("Category:");
        categoryLabel.setStyle("-fx-text-fill: " + (darkMode ? DARK_TEXT : LIGHT_TEXT) + ";");
        
        categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Personal", "Work", "Travel", "Ideas", "Goals", "Reflections");
        categoryCombo.setValue("Personal");
        categoryCombo.setStyle(getComboBoxStyle());
        
        Label moodLabel = new Label("Mood:");
        moodLabel.setStyle("-fx-text-fill: " + (darkMode ? DARK_TEXT : LIGHT_TEXT) + ";");
        
        moodCombo = new ComboBox<>();
        moodCombo.getItems().addAll("😊 Happy", "😢 Sad", "😡 Angry", "🤔 Thoughtful", "🎉 Excited", "😌 Peaceful");
        moodCombo.setValue("😊 Happy");
        moodCombo.setStyle(getComboBoxStyle());
        
        optionsBox.getChildren().addAll(categoryLabel, categoryCombo, moodLabel, moodCombo);
        
        // Content
        Label contentLabel = new Label("Content:");
        contentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + 
                             (darkMode ? DARK_TEXT : LIGHT_TEXT) + ";");
        
        entryContent = new TextArea();
        entryContent.setPromptText("Write your thoughts here...");
        entryContent.setPrefHeight(300);
        entryContent.setStyle(getTextAreaStyle());
        
        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button saveBtn = new Button("💾 Save Entry");
        saveBtn.setStyle(getButtonStyle(SECONDARY_COLOR));
        saveBtn.setOnAction(e -> saveEntry());
        
        Button clearBtn = new Button("🗑️ Clear");
        clearBtn.setStyle(getButtonStyle("#FF6B6B"));
        clearBtn.setOnAction(e -> {
            titleField.clear();
            entryContent.clear();
        });
        
        buttonBox.getChildren().addAll(clearBtn, saveBtn);
        
        writePanel.getChildren().addAll(titleLabel, titleField, optionsBox, contentLabel, entryContent, buttonBox);
        return writePanel;
    }
    
    private VBox createReadPanel() {
        VBox readPanel = new VBox(15);
        readPanel.setPadding(new Insets(20));
        readPanel.setStyle("-fx-background-color: " + (darkMode ? DARK_CARD : LIGHT_CARD) + 
                          "; -fx-background-radius: 10px;");
        
        // Entry info
        HBox infoBox = new HBox(15);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        
        entryTitleLabel = new Label("No entry selected");
        entryTitleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + 
                                (darkMode ? DARK_TEXT : LIGHT_TEXT) + ";");
        
        infoBox.getChildren().add(entryTitleLabel);
        
        // Content display
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        
        readArea = new TextArea();
        readArea.setEditable(false);
        readArea.setWrapText(true);
        readArea.setStyle(getTextAreaStyle());
        
        scrollPane.setContent(readArea);
        
        // Action buttons
        HBox actionBox = new HBox(15);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button deleteBtn = new Button("🗑️ Delete");
        deleteBtn.setStyle(getButtonStyle("#FF6B6B"));
        deleteBtn.setOnAction(e -> deleteEntry());
        
        actionBox.getChildren().addAll(deleteBtn);
        
        readPanel.getChildren().addAll(infoBox, scrollPane, actionBox);
        return readPanel;
    }
    
    private VBox createSearchPanel() {
        VBox searchPanel = new VBox(15);
        searchPanel.setPadding(new Insets(20));
        searchPanel.setStyle("-fx-background-color: " + (darkMode ? DARK_CARD : LIGHT_CARD) + 
                           "; -fx-background-radius: 10px;");
        
        // Search box
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        searchField = new TextField();
        searchField.setPromptText("Search entries...");
        searchField.setPrefWidth(300);
        searchField.setStyle(getTextFieldStyle());
        
        Button searchBtn = new Button("🔍 Search");
        searchBtn.setStyle(getButtonStyle(PRIMARY_COLOR));
        searchBtn.setOnAction(e -> performSearch());
        
        searchBox.getChildren().addAll(searchField, searchBtn);
        
        // Results
        ListView<String> resultsList = new ListView<>();
        resultsList.setPrefHeight(400);
        resultsList.setStyle("-fx-background-color: transparent;");
        
        // Search when Enter is pressed
        searchField.setOnAction(e -> performSearch());
        
        searchPanel.getChildren().addAll(searchBox, resultsList);
        return searchPanel;
    }
    
    private HBox createFooter() {
        HBox footer = new HBox(15);
        footer.setPadding(new Insets(10, 20, 10, 20));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: " + (darkMode ? DARK_CARD : LIGHT_CARD) + 
                       "; -fx-background-radius: 10px;");
        
        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: " + (darkMode ? DARK_TEXT_SECONDARY : LIGHT_TEXT_SECONDARY) + ";");
        
        footer.getChildren().add(statusLabel);
        return footer;
    }
    
    // Helper methods for styling
    private String getButtonStyle(String color) {
        return "-fx-background-color: " + color + ";" +
               "-fx-text-fill: white;" +
               "-fx-font-size: 14px;" +
               "-fx-font-weight: bold;" +
               "-fx-padding: 8px 15px;" +
               "-fx-background-radius: 6px;" +
               "-fx-cursor: hand;";
    }
    
    private String getTextFieldStyle() {
        return "-fx-background-color: " + (darkMode ? "#2D3047" : "#FFFFFF") + ";" +
               "-fx-text-fill: " + (darkMode ? DARK_TEXT : LIGHT_TEXT) + ";" +
               "-fx-border-color: " + (darkMode ? DARK_TEXT_SECONDARY : LIGHT_TEXT_SECONDARY) + ";" +
               "-fx-border-radius: 6px;" +
               "-fx-background-radius: 6px;" +
               "-fx-padding: 8px;" +
               "-fx-font-size: 14px;";
    }
    
    private String getTextAreaStyle() {
        return "-fx-background-color: " + (darkMode ? "#2D3047" : "#FFFFFF") + ";" +
               "-fx-text-fill: " + (darkMode ? DARK_TEXT : LIGHT_TEXT) + ";" +
               "-fx-border-color: " + (darkMode ? DARK_TEXT_SECONDARY : LIGHT_TEXT_SECONDARY) + ";" +
               "-fx-border-radius: 6px;" +
               "-fx-background-radius: 6px;" +
               "-fx-padding: 8px;" +
               "-fx-font-size: 14px;" +
               "-fx-control-inner-background: " + (darkMode ? "#2D3047" : "#FFFFFF") + ";";
    }
    
    private String getComboBoxStyle() {
        return "-fx-background-color: " + (darkMode ? "#2D3047" : "#FFFFFF") + ";" +
               "-fx-text-fill: " + (darkMode ? DARK_TEXT : LIGHT_TEXT) + ";" +
               "-fx-border-color: " + (darkMode ? DARK_TEXT_SECONDARY : LIGHT_TEXT_SECONDARY) + ";" +
               "-fx-border-radius: 6px;" +
               "-fx-background-radius: 6px;" +
               "-fx-padding: 6px;" +
               "-fx-font-size: 14px;";
    }
    
    // Core functionality
    private void loadEntries() {
        File[] files = diaryDirectory.listFiles((dir, name) -> name.endsWith(".txt"));
        entries.clear();
        
        if (files != null) {
            for (File file : files) {
                try {
                    String firstLine = Files.readAllLines(file.toPath()).get(0);
                    String entryName = firstLine.replace("Title: ", "") + " - " + 
                                     file.getName().replace("entry_", "").replace(".txt", "");
                    entries.add(entryName);
                } catch (IOException e) {
                    System.out.println("Error reading file: " + file.getName());
                }
            }
        }
        
        statusLabel.setText("Loaded " + entries.size() + " entries");
    }
    
    private void saveEntry() {
        if (titleField.getText().isEmpty()) {
            showAlert("Error", "Please enter a title");
            return;
        }
        
        if (entryContent.getText().isEmpty()) {
            showAlert("Error", "Please write some content");
            return;
        }
        
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File entryFile = new File(diaryDirectory, "entry_" + timestamp + ".txt");
            
            List<String> content = new ArrayList<>();
            content.add("Title: " + titleField.getText());
            content.add("Category: " + categoryCombo.getValue());
            content.add("Mood: " + moodCombo.getValue());
            content.add("Date: " + LocalDateTime.now().toString());
            content.add("");
            content.add(entryContent.getText());
            
            Files.write(entryFile.toPath(), content);
            
            // Update UI
            entries.add(0, titleField.getText() + " - " + timestamp);
            statusLabel.setText("Entry saved successfully!");
            
            // Clear fields
            titleField.clear();
            entryContent.clear();
            
        } catch (IOException e) {
            statusLabel.setText("Error saving entry: " + e.getMessage());
        }
    }
    
    private void loadEntry(String entryInfo) {
        try {
            // Parse entry info to get filename
            String[] parts = entryInfo.split(" - ");
            String timestamp = parts[parts.length - 1];
            String filename = "entry_" + timestamp + ".txt";
            
            File entryFile = new File(diaryDirectory, filename);
            if (entryFile.exists()) {
                List<String> lines = Files.readAllLines(entryFile.toPath());
                
                if (lines.size() >= 6) {
                    entryTitleLabel.setText(lines.get(0).replace("Title: ", ""));
                    
                    StringBuilder content = new StringBuilder();
                    content.append("Category: ").append(lines.get(1).replace("Category: ", "")).append("\n");
                    content.append("Mood: ").append(lines.get(2).replace("Mood: ", "")).append("\n");
                    content.append("Date: ").append(lines.get(3).replace("Date: ", "")).append("\n\n");
                    
                    for (int i = 5; i < lines.size(); i++) {
                        content.append(lines.get(i)).append("\n");
                    }
                    
                    readArea.setText(content.toString());
                    statusLabel.setText("Loaded entry: " + entryTitleLabel.getText());
                }
            }
        } catch (IOException e) {
            statusLabel.setText("Error loading entry: " + e.getMessage());
        }
    }
    
    private void deleteEntry() {
        String selected = entriesListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "No entry selected");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Entry");
        alert.setHeaderText("Delete this entry?");
        alert.setContentText("This action cannot be undone.");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                // Parse entry info to get filename
                String[] parts = selected.split(" - ");
                String timestamp = parts[parts.length - 1];
                String filename = "entry_" + timestamp + ".txt";
                
                File entryFile = new File(diaryDirectory, filename);
                if (entryFile.delete()) {
                    entries.remove(selected);
                    entryTitleLabel.setText("No entry selected");
                    readArea.clear();
                    statusLabel.setText("Entry deleted successfully");
                } else {
                    statusLabel.setText("Error deleting entry");
                }
            } catch (Exception e) {
                statusLabel.setText("Error: " + e.getMessage());
            }
        }
    }
    
    private void performSearch() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            showAlert("Search", "Please enter search text");
            return;
        }
        
        List<String> results = new ArrayList<>();
        File[] files = diaryDirectory.listFiles((dir, name) -> name.endsWith(".txt"));
        
        if (files != null) {
            for (File file : files) {
                try {
                    List<String> lines = Files.readAllLines(file.toPath());
                    String content = String.join(" ", lines).toLowerCase();
                    
                    if (content.contains(searchText)) {
                        String entryName = lines.get(0).replace("Title: ", "") + " - " + 
                                         file.getName().replace("entry_", "").replace(".txt", "");
                        results.add(entryName);
                    }
                } catch (IOException e) {
                    System.out.println("Error searching file: " + file.getName());
                }
            }
        }
        
        // Show results in dialog
        Alert resultsAlert = new Alert(Alert.AlertType.INFORMATION);
        resultsAlert.setTitle("Search Results");
        resultsAlert.setHeaderText("Found " + results.size() + " matching entries");
        
        TextArea resultsArea = new TextArea(String.join("\n", results));
        resultsArea.setEditable(false);
        resultsArea.setPrefSize(400, 300);
        
        resultsAlert.getDialogPane().setContent(resultsArea);
        resultsAlert.showAndWait();
        
        statusLabel.setText("Found " + results.size() + " matches for '" + searchText + "'");
    }
    
    private void toggleTheme() {
        applyTheme();
    }
    
    private void applyTheme() {
        String bgColor = darkMode ? DARK_BG : LIGHT_BG;
        String cardColor = darkMode ? DARK_CARD : LIGHT_CARD;
        String textColor = darkMode ? DARK_TEXT : LIGHT_TEXT;
        
        root.setStyle("-fx-background-color: " + bgColor + ";");
        
        // Update navigation
        if (root.getLeft() != null) {
            ((VBox) root.getLeft()).setStyle("-fx-background-color: " + cardColor + "; -fx-background-radius: 10px;");
        }
        
        // Update status label
        if (statusLabel != null) {
            statusLabel.setStyle("-fx-text-fill: " + (darkMode ? DARK_TEXT_SECONDARY : LIGHT_TEXT_SECONDARY) + ";");
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}