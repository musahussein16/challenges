package com.aerodynamics.weatherwidget;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class AeroDynamicsWeatherWidget extends Application {
    
    // Brand colors
    private static final String AERO_PRIMARY = "#003366";
    private static final String AERO_SECONDARY = "#00509E";
    private static final String AERO_ACCENT = "#FF6B35";
    private static final String AERO_SAFE = "#4CAF50";
    private static final String AERO_CAUTION = "#FFC107";
    private static final String AERO_DANGER = "#D32F2F";
    private static final String AERO_BG = "#0A192F";
    private static final String AERO_PANEL = "#162447";
    private static final String AERO_TEXT = "#E6F1FF";
    private static final String AERO_TEXT_SECONDARY = "#8892B0";
    
    private Label statusText;
    private Label statusDetails;
    private StackPane statusDot;
    private Label temperatureLabel;
    private Label windValueLabel;
    private Label visibilityValueLabel;
    private Label turbulenceValueLabel;
    private Label precipitationValueLabel;
    private ComboBox<String> departureComboBox;
    private ComboBox<String> arrivalComboBox;
    private Label flightTimeLabel;
    private Label updateTimeLabel;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Aero Dynamics - Aviation Weather Widget");
        
        // Set application icon
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/plane_icon.png")));
        } catch (Exception e) {
            System.out.println("Icon not found, using default");
        }
        
        // Main container
        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(
            Color.web(AERO_BG), CornerRadii.EMPTY, Insets.EMPTY
        )));
        
        // Create header
        root.setTop(createHeader());
        
        // Create main content
        root.setCenter(createContent());
        
        // Create footer
        root.setBottom(createFooter());
        
        Scene scene = new Scene(root, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(650);
        primaryStage.show();
        
        // Initialize data
        initializeData();
    }
    
    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setBackground(new Background(new BackgroundFill(
            Color.web(AERO_PRIMARY), CornerRadii.EMPTY, Insets.EMPTY
        )));
        header.setBorder(new Border(new BorderStroke(
            Color.web(AERO_SECONDARY), BorderStrokeStyle.SOLID, 
            CornerRadii.EMPTY, new BorderWidths(0, 0, 1, 0)
        )));
        header.setSpacing(15);
        
        // Company logo section
        HBox logoSection = new HBox(15);
        logoSection.setAlignment(Pos.CENTER_LEFT);
        
        // Plane icon (using Unicode character since we can't use FontAwesome directly)
        Label planeIcon = new Label("✈");
        planeIcon.setStyle("-fx-font-size: 28px; -fx-text-fill: " + AERO_ACCENT + ";");
        
        VBox logoText = new VBox(5);
        Label companyName = new Label("AERO DYNAMICS");
        companyName.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + AERO_TEXT + ";");
        Label tagline = new Label("Aviation Weather Systems");
        tagline.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AERO_TEXT_SECONDARY + ";");
        
        logoText.getChildren().addAll(companyName, tagline);
        logoSection.getChildren().addAll(planeIcon, logoText);
        
        // Mission statement
        Label mission = new Label("Providing critical weather data for flight safety and planning");
        mission.setStyle("-fx-font-size: 11px; -fx-font-style: italic; -fx-text-fill: " + AERO_TEXT_SECONDARY + ";");
        mission.setMaxWidth(300);
        mission.setWrapText(true);
        mission.setAlignment(Pos.CENTER_RIGHT);
        
        // Header layout
        header.getChildren().addAll(logoSection);
        HBox.setHgrow(logoSection, Priority.ALWAYS);
        header.getChildren().add(mission);
        
        return header;
    }
    
    private HBox createContent() {
        HBox content = new HBox();
        
        // Flight panel (left)
        VBox flightPanel = createFlightPanel();
        flightPanel.setPrefWidth(350);
        
        // Weather display (right)
        VBox weatherDisplay = createWeatherDisplay();
        HBox.setHgrow(weatherDisplay, Priority.ALWAYS);
        
        // Separator
        Separator separator = new Separator();
        separator.setOrientation(javafx.geometry.Orientation.VERTICAL);
        
        content.getChildren().addAll(flightPanel, separator, weatherDisplay);
        
        return content;
    }
    
    private VBox createFlightPanel() {
        VBox flightPanel = new VBox(20);
        flightPanel.setPadding(new Insets(25));
        flightPanel.setBackground(new Background(new BackgroundFill(
            Color.web("rgba(0,0,0,0.2)"), new CornerRadii(0), Insets.EMPTY
        )));
        flightPanel.setBorder(new Border(new BorderStroke(
            Color.web("rgba(255,255,255,0.05)"), BorderStrokeStyle.SOLID, 
            CornerRadii.EMPTY, new BorderWidths(0, 1, 0, 0)
        )));
        
        // Panel title
        HBox panelTitle = new HBox(10);
        panelTitle.setAlignment(Pos.CENTER_LEFT);
        Label planeIcon = new Label("🛫");
        planeIcon.setStyle("-fx-font-size: 18px;");
        Label title = new Label("Flight Planning");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + AERO_ACCENT + ";");
        panelTitle.getChildren().addAll(planeIcon, title);
        
        // Flight selector
        VBox flightSelector = new VBox(15);
        
        // Departure airport
        VBox departureBox = new VBox(5);
        Label departureLabel = new Label("Departure Airport");
        departureLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + AERO_TEXT_SECONDARY + ";");
        departureComboBox = new ComboBox<>();
        departureComboBox.getItems().addAll(
            "KJFK - New York, NY",
            "KLAX - Los Angeles, CA",
            "KORD - Chicago, IL",
            "KDEN - Denver, CO",
            "KMIA - Miami, FL"
        );
        departureComboBox.setValue("KJFK - New York, NY");
        departureComboBox.setPrefWidth(300);
        departureComboBox.setStyle(getComboBoxStyle());
        departureBox.getChildren().addAll(departureLabel, departureComboBox);
        
        // Arrival airport
        VBox arrivalBox = new VBox(5);
        Label arrivalLabel = new Label("Arrival Airport");
        arrivalLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + AERO_TEXT_SECONDARY + ";");
        arrivalComboBox = new ComboBox<>();
        arrivalComboBox.getItems().addAll(
            "KLAX - Los Angeles, CA",
            "KJFK - New York, NY",
            "KDFW - Dallas, TX",
            "KSEA - Seattle, WA",
            "KMCO - Orlando, FL"
        );
        arrivalComboBox.setValue("KLAX - Los Angeles, CA");
        arrivalComboBox.setPrefWidth(300);
        arrivalComboBox.setStyle(getComboBoxStyle());
        arrivalBox.getChildren().addAll(arrivalLabel, arrivalComboBox);
        
        // Flight time
        VBox flightTimeBox = new VBox(5);
        Label flightTimeLabel = new Label("Estimated Flight Time");
        flightTimeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + AERO_TEXT_SECONDARY + ";");
        this.flightTimeLabel = new Label("5h 42m");
        this.flightTimeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + AERO_TEXT + ";");
        this.flightTimeLabel.setPadding(new Insets(10));
        this.flightTimeLabel.setBackground(new Background(new BackgroundFill(
            Color.web("rgba(255,255,255,0.07)"), new CornerRadii(6), Insets.EMPTY
        )));
        this.flightTimeLabel.setBorder(new Border(new BorderStroke(
            Color.web("rgba(255,255,255,0.1)"), BorderStrokeStyle.SOLID, 
            new CornerRadii(6), new BorderWidths(1)
        )));
        this.flightTimeLabel.setPrefWidth(300);
        flightTimeBox.getChildren().addAll(flightTimeLabel, this.flightTimeLabel);
        
        flightSelector.getChildren().addAll(departureBox, arrivalBox, flightTimeBox);
        
        // Flight status
        VBox flightStatus = new VBox(10);
        flightStatus.setPadding(new Insets(15));
        flightStatus.setBackground(new Background(new BackgroundFill(
            Color.web("rgba(0,0,0,0.3)"), new CornerRadii(8), Insets.EMPTY
        )));
        
        HBox statusHeader = new HBox();
        statusHeader.setAlignment(Pos.CENTER_LEFT);
        statusHeader.setSpacing(10);
        
        HBox statusIndicator = new HBox(8);
        statusIndicator.setAlignment(Pos.CENTER_LEFT);
        
        statusDot = new StackPane();
        statusDot.setPrefSize(10, 10);
        statusDot.setStyle("-fx-background-radius: 5px; -fx-background-color: " + AERO_SAFE + ";");
        
        statusText = new Label("Flight Conditions: SAFE");
        statusText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        statusIndicator.getChildren().addAll(statusDot, statusText);
        
        updateTimeLabel = new Label("Updated: 14:30 UTC");
        updateTimeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AERO_TEXT_SECONDARY + ";");
        
        statusHeader.getChildren().addAll(statusIndicator);
        HBox.setHgrow(statusIndicator, Priority.ALWAYS);
        statusHeader.getChildren().add(updateTimeLabel);
        
        statusDetails = new Label("Weather conditions are favorable for flight. Visibility is good with no significant turbulence expected along the route. Minor headwinds may increase flight time by approximately 12 minutes.");
        statusDetails.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AERO_TEXT_SECONDARY + ";");
        statusDetails.setWrapText(true);
        statusDetails.setLineSpacing(2);
        
        flightStatus.getChildren().addAll(statusHeader, statusDetails);
        
        flightPanel.getChildren().addAll(panelTitle, flightSelector, flightStatus);
        
        // Add event handlers
        departureComboBox.setOnAction(e -> updateFlightInfo());
        arrivalComboBox.setOnAction(e -> updateFlightInfo());
        
        return flightPanel;
    }
    
    private VBox createWeatherDisplay() {
        VBox weatherDisplay = new VBox(20);
        weatherDisplay.setPadding(new Insets(25));
        
        // Location header
        HBox locationHeader = new HBox();
        locationHeader.setAlignment(Pos.CENTER_LEFT);
        
        VBox locationInfo = new VBox(5);
        Label airportName = new Label("KJFK - John F. Kennedy Airport");
        airportName.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        Label locationDetails = new Label("New York, New York • Elevation: 13 ft");
        locationDetails.setStyle("-fx-font-size: 14px; -fx-text-fill: " + AERO_TEXT_SECONDARY + ";");
        locationInfo.getChildren().addAll(airportName, locationDetails);
        
        VBox updateInfo = new VBox(5);
        Label lastUpdate = new Label("Last updated: 14:28 UTC");
        Label nextUpdate = new Label("Next update: 14:58 UTC");
        lastUpdate.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AERO_TEXT_SECONDARY + ";");
        nextUpdate.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AERO_TEXT_SECONDARY + ";");
        updateInfo.getChildren().addAll(lastUpdate, nextUpdate);
        updateInfo.setAlignment(Pos.CENTER_RIGHT);
        
        locationHeader.getChildren().addAll(locationInfo);
        HBox.setHgrow(locationInfo, Priority.ALWAYS);
        locationHeader.getChildren().add(updateInfo);
        
        // Current weather
        HBox currentWeather = new HBox(20);
        currentWeather.setPadding(new Insets(20));
        currentWeather.setBackground(new Background(new BackgroundFill(
            Color.web("rgba(0,80,158,0.2)"), new CornerRadii(10), Insets.EMPTY
        )));
        currentWeather.setBorder(new Border(new BorderStroke(
            Color.web("rgba(255,255,255,0.05)"), BorderStrokeStyle.SOLID, 
            new CornerRadii(10), new BorderWidths(1)
        )));
        
        // Temperature
        VBox tempBox = new VBox();
        temperatureLabel = new Label("64°F");
        temperatureLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold;");
        tempBox.getChildren().add(temperatureLabel);
        
        // Weather icon
        VBox iconBox = new VBox();
        Label weatherIcon = new Label("⛅");
        weatherIcon.setStyle("-fx-font-size: 48px; -fx-text-fill: " + AERO_ACCENT + ";");
        iconBox.getChildren().add(weatherIcon);
        
        // Weather details
        VBox weatherDetails = new VBox(8);
        
        String[][] detailData = {
            {"Feels Like", "62°F"},
            {"Humidity", "65%"},
            {"Dew Point", "52°F"},
            {"Pressure", "1013 hPa"}
        };
        
        for (String[] detail : detailData) {
            HBox detailItem = new HBox();
            detailItem.setAlignment(Pos.CENTER_LEFT);
            detailItem.setSpacing(10);
            
            Label detailLabel = new Label(detail[0]);
            detailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + AERO_TEXT_SECONDARY + ";");
            detailLabel.setPrefWidth(100);
            
            Label detailValue = new Label(detail[1]);
            detailValue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            
            detailItem.getChildren().addAll(detailLabel, detailValue);
            weatherDetails.getChildren().add(detailItem);
        }
        
        currentWeather.getChildren().addAll(tempBox, iconBox, weatherDetails);
        
        // Critical indicators
        VBox criticalIndicators = new VBox(15);
        
        HBox indicatorsTitle = new HBox(10);
        indicatorsTitle.setAlignment(Pos.CENTER_LEFT);
        Label warningIcon = new Label("⚠");
        warningIcon.setStyle("-fx-font-size: 18px;");
        Label indicatorsLabel = new Label("Critical Flight Indicators");
        indicatorsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + AERO_ACCENT + ";");
        indicatorsTitle.getChildren().addAll(warningIcon, indicatorsLabel);
        
        // Indicators grid
        GridPane indicatorsGrid = new GridPane();
        indicatorsGrid.setHgap(15);
        indicatorsGrid.setVgap(15);
        
        String[][] indicators = {
            {"Visibility", "10 mi", AERO_SAFE, "👁"},
            {"Crosswind", "18 kt", AERO_CAUTION, "💨"},
            {"Precipitation", "None", AERO_SAFE, "🌧"},
            {"Turbulence Risk", "Moderate", AERO_DANGER, "⚡"}
        };
        
        for (int i = 0; i < indicators.length; i++) {
            VBox indicator = createIndicator(
                indicators[i][0], 
                indicators[i][1], 
                indicators[i][2],
                indicators[i][3]
            );
            
            GridPane.setConstraints(indicator, i % 2, i / 2);
            indicatorsGrid.getChildren().add(indicator);
        }
        
        criticalIndicators.getChildren().addAll(indicatorsTitle, indicatorsGrid);
        
        weatherDisplay.getChildren().addAll(locationHeader, currentWeather, criticalIndicators);
        
        return weatherDisplay;
    }
    
    private VBox createIndicator(String label, String value, String color, String icon) {
        VBox indicator = new VBox(8);
        indicator.setPadding(new Insets(15));
        indicator.setBackground(new Background(new BackgroundFill(
            Color.web("rgba(0,0,0,0.3)"), new CornerRadii(8), Insets.EMPTY
        )));
        indicator.setAlignment(Pos.CENTER);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: " + color + ";");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AERO_TEXT_SECONDARY + ";");
        
        indicator.getChildren().addAll(iconLabel, valueLabel, nameLabel);
        
        // Store references to dynamic indicators
        switch (label) {
            case "Crosswind":
                windValueLabel = valueLabel;
                break;
            case "Visibility":
                visibilityValueLabel = valueLabel;
                break;
            case "Turbulence Risk":
                turbulenceValueLabel = valueLabel;
                break;
            case "Precipitation":
                precipitationValueLabel = valueLabel;
                break;
        }
        
        return indicator;
    }
    
    private HBox createFooter() {
        HBox footer = new HBox(15);
        footer.setPadding(new Insets(15, 30, 15, 30));
        footer.setBackground(new Background(new BackgroundFill(
            Color.web("rgba(0,0,0,0.4)"), CornerRadii.EMPTY, Insets.EMPTY
        )));
        footer.setBorder(new Border(new BorderStroke(
            Color.web("rgba(255,255,255,0.05)"), BorderStrokeStyle.SOLID, 
            CornerRadii.EMPTY, new BorderWidths(1, 0, 0, 0)
        )));
        footer.setAlignment(Pos.CENTER_LEFT);
        
        // Footer info
        Label footerInfo = new Label("✈ Aero Dynamics Weather Systems - For Aviation Professionals");
        footerInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AERO_TEXT_SECONDARY + ";");
        
        // Footer buttons
        HBox footerButtons = new HBox(10);
        footerButtons.setAlignment(Pos.CENTER_RIGHT);
        
        Button alternateRoutesBtn = createFooterButton("Alternate Routes", false);
        Button flightBriefingBtn = createFooterButton("Flight Briefing", true);
        Button refreshBtn = createFooterButton("Refresh Data", false);
        
        footerButtons.getChildren().addAll(alternateRoutesBtn, flightBriefingBtn, refreshBtn);
        
        footer.getChildren().add(footerInfo);
        HBox.setHgrow(footerInfo, Priority.ALWAYS);
        footer.getChildren().add(footerButtons);
        
        // Add event handlers
        refreshBtn.setOnAction(e -> refreshData());
        
        return footer;
    }
    
    private Button createFooterButton(String text, boolean isAccent) {
        Button button = new Button(text);
        button.setPadding(new Insets(8, 15, 8, 15));
        button.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AERO_TEXT + ";");
        
        if (isAccent) {
            button.setStyle(button.getStyle() + 
                "-fx-background-color: " + AERO_ACCENT + ";" +
                "-fx-background-radius: 4px;");
        } else {
            button.setStyle(button.getStyle() + 
                "-fx-background-color: " + AERO_PRIMARY + ";" +
                "-fx-background-radius: 4px;");
        }
        
        // Hover effect
        button.setOnMouseEntered(e -> {
            if (isAccent) {
                button.setStyle(button.getStyle().replace(AERO_ACCENT, "#FF824F"));
            } else {
                button.setStyle(button.getStyle().replace(AERO_PRIMARY, AERO_SECONDARY));
            }
        });
        
        button.setOnMouseExited(e -> {
            if (isAccent) {
                button.setStyle(button.getStyle().replace("#FF824F", AERO_ACCENT));
            } else {
                button.setStyle(button.getStyle().replace(AERO_SECONDARY, AERO_PRIMARY));
            }
        });
        
        return button;
    }
    
    private String getComboBoxStyle() {
        return "-fx-background-color: rgba(255,255,255,0.07);" +
               "-fx-border-color: rgba(255,255,255,0.1);" +
               "-fx-border-radius: 6px;" +
               "-fx-text-fill: " + AERO_TEXT + ";" +
               "-fx-padding: 10px;" +
               "-fx-font-size: 14px;";
    }
    
    private void initializeData() {
        updateFlightInfo();
    }
    
    private void updateFlightInfo() {
        // Flight times database
        java.util.Map<String, String> flightTimes = new java.util.HashMap<>();
        flightTimes.put("KJFK-KLAX", "5h 42m");
        flightTimes.put("KJFK-KORD", "2h 15m");
        flightTimes.put("KJFK-KDEN", "3h 55m");
        flightTimes.put("KJFK-KMIA", "2h 50m");
        flightTimes.put("KLAX-KJFK", "5h 42m");
        flightTimes.put("KLAX-KORD", "3h 55m");
        flightTimes.put("KLAX-KDFW", "2h 45m");
        flightTimes.put("KLAX-KSEA", "2h 10m");
        flightTimes.put("KORD-KJFK", "2h 15m");
        
        // Get selected airports
        String departure = departureComboBox.getValue();
        String arrival = arrivalComboBox.getValue();
        
        if (departure != null && arrival != null) {
            String departureCode = departure.substring(0, 4);
            String arrivalCode = arrival.substring(0, 4);
            String routeKey = departureCode + "-" + arrivalCode;
            
            // Update flight time
            String flightTime = flightTimes.getOrDefault(routeKey, "3h 30m");
            flightTimeLabel.setText(flightTime);
            
            // Update status randomly (for demo)
            String[] statuses = {"SAFE", "CAUTION", "RISK"};
            String[] statusColors = {AERO_SAFE, AERO_CAUTION, AERO_DANGER};
            String[] statusDetails = {
                "Weather conditions are favorable for flight. Visibility is good with no significant turbulence expected.",
                "Moderate crosswinds expected at destination. Consider reviewing alternate landing options.",
                "Significant turbulence and low visibility reported along route. Consider delaying departure."
            };
            
            int randomIndex = (int)(Math.random() * statuses.length);
            String status = statuses[randomIndex];
            String color = statusColors[randomIndex];
            String details = statusDetails[randomIndex];
            
            statusText.setText("Flight Conditions: " + status);
            statusDot.setStyle("-fx-background-radius: 5px; -fx-background-color: " + color + ";");
            //statusDetails.setText(details);
            
            // Update time
            java.time.LocalTime now = java.time.LocalTime.now();
            String timeString = String.format("Updated: %02d:%02d UTC", now.getHour(), now.getMinute());
            updateTimeLabel.setText(timeString);
        }
    }
    
    private void refreshData() {
        updateFlightInfo();
        
        // Update weather data randomly (for demo)
        String[] temps = {"62°F", "64°F", "67°F", "59°F", "71°F"};
        String[] winds = {"12 kt", "18 kt", "22 kt", "15 kt", "25 kt"};
        String[] visibilities = {"8 mi", "10 mi", "15 mi", "5 mi", "20 mi"};
        String[] turbulences = {"Low", "Moderate", "High", "Low", "Moderate"};
        String[] precipitations = {"None", "Light", "None", "Heavy", "None"};
        String[] turbulenceColors = {AERO_SAFE, AERO_CAUTION, AERO_DANGER, AERO_SAFE, AERO_CAUTION};
        
        int randomIndex = (int)(Math.random() * temps.length);
        
        temperatureLabel.setText(temps[randomIndex]);
        windValueLabel.setText(winds[randomIndex]);
        visibilityValueLabel.setText(visibilities[randomIndex]);
        turbulenceValueLabel.setText(turbulences[randomIndex]);
        turbulenceValueLabel.setStyle("-fx-text-fill: " + turbulenceColors[randomIndex] + ";");
        precipitationValueLabel.setText(precipitations[randomIndex]);
        
        // Show refresh confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Data Refreshed");
        alert.setHeaderText(null);
        alert.setContentText("Weather data has been successfully updated.");
        alert.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}