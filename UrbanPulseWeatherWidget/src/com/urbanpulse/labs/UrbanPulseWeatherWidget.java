package com.urbanpulse.labs;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.animation.*;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.chart.*;

public class UrbanPulseWeatherWidget extends Application {
    
    // Urban tech brand colors
    private static final String URBAN_PRIMARY = "#1565C0";
    private static final String URBAN_SECONDARY = "#2196F3";
    private static final String URBAN_ACCENT = "#00BCD4";
    private static final String URBAN_DARK = "#0D47A1";
    private static final String URBAN_LIGHT = "#E3F2FD";
    private static final String URBAN_NEUTRAL = "#607D8B";
    private static final String URBAN_BG = "#1A237E";
    private static final String URBAN_PANEL = "#283593";
    private static final String URBAN_TEXT = "#E3F2FD";
    private static final String URBAN_TEXT_SECONDARY = "#90A4AE";
    
    // UI Components
    private Label temperatureLabel;
    private Label cityNameLabel;
    private Label trafficLevelLabel;
    private Label airQualityLabel;
    private Label publicTransitLabel;
    private Label updateTimeLabel;
    private ProgressBar trafficProgress;
    private ProgressBar transitProgress;
    private LineChart<String, Number> tempChart;
    private BarChart<String, Number> pollutionChart;
    private AreaChart<String, Number> trafficChart;
    private ComboBox<String> cityComboBox;
    private ToggleButton liveDataToggle;
    private Timeline liveUpdateTimeline;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Urban Pulse Labs - Smart City Weather Dashboard");
        
        // Main container with dark tech background
        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(
            Color.web(URBAN_BG), CornerRadii.EMPTY, Insets.EMPTY
        )));
        
        // Create header
        root.setTop(createHeader());
        
        // Create main content
        root.setCenter(createContent());
        
        // Create footer
        root.setBottom(createFooter());
        
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(750);
        
        primaryStage.show();
        
        // Initialize data
        initializeData();
        startLiveUpdates();
    }
    
    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(15, 30, 15, 30));
        header.setBackground(new Background(new BackgroundFill(
            Color.web(URBAN_PANEL), CornerRadii.EMPTY, Insets.EMPTY
        )));
        header.setBorder(new Border(new BorderStroke(
            Color.web(URBAN_ACCENT), BorderStrokeStyle.SOLID, 
            CornerRadii.EMPTY, new BorderWidths(0, 0, 2, 0)
        )));
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Logo section
        HBox logoSection = new HBox(15);
        logoSection.setAlignment(Pos.CENTER_LEFT);
        
        // Tech icon
        StackPane iconContainer = new StackPane();
        Circle iconCircle = new Circle(22);
        iconCircle.setFill(Color.web(URBAN_ACCENT));
        iconCircle.setStroke(Color.web(URBAN_PRIMARY));
        iconCircle.setStrokeWidth(2);
        
        // Create a wifi-like icon
        Path wifiIcon = new Path();
        wifiIcon.getElements().addAll(
            new MoveTo(15, 25),
            new ArcTo(20, 20, 0, -15, 25, false, true),
            new MoveTo(10, 25),
            new ArcTo(15, 15, 0, -10, 25, false, true),
            new MoveTo(5, 25),
            new ArcTo(10, 10, 0, -5, 25, false, true),
            new MoveTo(0, 25),
            new LineTo(0, 30),
            new LineTo(0, 25)
        );
        wifiIcon.setStroke(Color.web(URBAN_LIGHT));
        wifiIcon.setStrokeWidth(2);
        wifiIcon.setFill(Color.TRANSPARENT);
        
        iconContainer.getChildren().addAll(iconCircle, wifiIcon);
        
        VBox logoText = new VBox(3);
        Label companyName = new Label("URBAN PULSE LABS");
        companyName.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + URBAN_ACCENT + ";");
        Label tagline = new Label("Smart City Intelligence Platform");
        tagline.setStyle("-fx-font-size: 12px; -fx-text-fill: " + URBAN_TEXT_SECONDARY + ";");
        logoText.getChildren().addAll(companyName, tagline);
        
        logoSection.getChildren().addAll(iconContainer, logoText);
        
        // City selector
        HBox citySelector = new HBox(10);
        citySelector.setAlignment(Pos.CENTER_LEFT);
        
        Label cityLabel = new Label("CITY:");
        cityLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + URBAN_TEXT_SECONDARY + ";");
        
        cityComboBox = new ComboBox<>();
        cityComboBox.getItems().addAll(
            "🏙️ METROPOLIS CENTRAL",
            "🌉 BAYSIDE URBAN ZONE",
            "🚇 TRANSIT HUB DISTRICT",
            "🏗️ DOWNTOWN CORE",
            "🌃 RIVERSIDE PRECINCT"
        );
        cityComboBox.setValue("🏙️ METROPOLIS CENTRAL");
        cityComboBox.setPrefWidth(250);
        cityComboBox.setStyle(getComboBoxStyle());
        
        citySelector.getChildren().addAll(cityLabel, cityComboBox);
        
        // Live data toggle
        HBox toggleBox = new HBox(10);
        toggleBox.setAlignment(Pos.CENTER_RIGHT);
        
        liveDataToggle = new ToggleButton();
        liveDataToggle.setText("LIVE DATA");
        liveDataToggle.setSelected(true);
        liveDataToggle.setStyle(getToggleButtonStyle(true));
        
        liveDataToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            liveDataToggle.setStyle(getToggleButtonStyle(newVal));
            if (newVal) {
                startLiveUpdates();
            } else {
                stopLiveUpdates();
            }
        });
        
        updateTimeLabel = new Label("SYNC: --:--:--");
        updateTimeLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + URBAN_ACCENT + ";");
        
        toggleBox.getChildren().addAll(updateTimeLabel, liveDataToggle);
        
        // Header layout
        header.getChildren().addAll(logoSection);
        HBox.setHgrow(logoSection, Priority.ALWAYS);
        header.getChildren().addAll(citySelector, toggleBox);
        
        // Add event handler for city change
        cityComboBox.setOnAction(e -> updateCityData());
        
        return header;
    }
    
    private GridPane createContent() {
        GridPane content = new GridPane();
        content.setPadding(new Insets(20));
        content.setHgap(20);
        content.setVgap(20);
        
        // Column constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(40);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(60);
        content.getColumnConstraints().addAll(col1, col2);
        
        // Row 1: Current Conditions and Temperature Chart
        content.add(createCurrentConditionsCard(), 0, 0);
        content.add(createTemperatureChartCard(), 1, 0);
        
        // Row 2: Urban Metrics and Traffic Chart
        content.add(createUrbanMetricsCard(), 0, 1);
        content.add(createTrafficChartCard(), 1, 1);
        
        // Row 3: Air Quality and Public Transit
        content.add(createAirQualityCard(), 0, 2);
        content.add(createTransitCard(), 1, 2);
        
        GridPane.setRowSpan(createTemperatureChartCard(), 2);
        GridPane.setRowSpan(createTrafficChartCard(), 2);
        
        return content;
    }
    
    private VBox createCurrentConditionsCard() {
        VBox card = createTechCard();
        
        Label title = new Label("CURRENT URBAN CONDITIONS");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + URBAN_ACCENT + ";");
        title.setPadding(new Insets(0, 0, 15, 0));
        
        // City name
        cityNameLabel = new Label("METROPOLIS CENTRAL");
        cityNameLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + URBAN_TEXT + ";");
        
        // Temperature display
        HBox tempRow = new HBox(15);
        tempRow.setAlignment(Pos.CENTER_LEFT);
        
        temperatureLabel = new Label("68°F");
        temperatureLabel.setStyle("-fx-font-size: 64px; -fx-font-weight: bold; -fx-text-fill: " + URBAN_TEXT + ";");
        
        VBox tempDetails = new VBox(10);
        Label feelsLike = new Label("Feels like 66°F");
        feelsLike.setStyle("-fx-font-size: 14px; -fx-text-fill: " + URBAN_TEXT_SECONDARY + ";");
        
        Label condition = new Label("Partly Cloudy");
        condition.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + URBAN_TEXT + ";");
        
        tempDetails.getChildren().addAll(feelsLike, condition);
        tempRow.getChildren().addAll(temperatureLabel, tempDetails);
        
        // Weather metrics grid
        GridPane metricsGrid = new GridPane();
        metricsGrid.setHgap(15);
        metricsGrid.setVgap(15);
        metricsGrid.setPadding(new Insets(20, 0, 0, 0));
        
        String[][] metrics = {
            {"💨", "Wind", "12 mph NE", URBAN_ACCENT},
            {"💧", "Humidity", "64%", URBAN_SECONDARY},
            {"🌫️", "Visibility", "9 mi", URBAN_NEUTRAL},
            {"📡", "Pressure", "1012 hPa", URBAN_PRIMARY}
        };
        
        for (int i = 0; i < metrics.length; i++) {
            VBox metricBox = createMetricBox(metrics[i][0], metrics[i][1], metrics[i][2], metrics[i][3]);
            GridPane.setConstraints(metricBox, i % 2, i / 2);
            metricsGrid.getChildren().add(metricBox);
        }
        
        // Weather alerts
        VBox alertsBox = new VBox(10);
        alertsBox.setPadding(new Insets(20, 0, 0, 0));
        
        Label alertsTitle = new Label("URBAN ALERTS");
        alertsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + URBAN_ACCENT + ";");
        
        TextArea alertsText = new TextArea();
        alertsText.setText("• Street cleaning suspended due to weather\n• Bike lane usage optimal conditions\n• Public parks: Open with restrictions\n• EV charging stations: 78% availability");
        alertsText.setEditable(false);
        alertsText.setWrapText(true);
        alertsText.setPrefHeight(80);
        alertsText.setStyle(
            "-fx-control-inner-background: " + URBAN_PANEL + ";" +
            "-fx-text-fill: " + URBAN_TEXT + ";" +
            "-fx-border-color: " + URBAN_ACCENT + ";" +
            "-fx-border-width: 1px;" +
            "-fx-font-size: 12px;"
        );
        
        alertsBox.getChildren().addAll(alertsTitle, alertsText);
        
        card.getChildren().addAll(title, cityNameLabel, tempRow, metricsGrid, alertsBox);
        
        return card;
    }
    
    private VBox createTemperatureChartCard() {
        VBox card = createTechCard();
        
        Label title = new Label("24-HOUR TEMPERATURE TREND");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + URBAN_ACCENT + ";");
        title.setPadding(new Insets(0, 0, 15, 0));
        
        // Create temperature chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Temperature (°F)");
        
        tempChart = new LineChart<>(xAxis, yAxis);
        tempChart.setLegendVisible(false);
        tempChart.setAnimated(true);
        tempChart.setPrefHeight(300);
        
        // Style the chart
        tempChart.setStyle(
            "-fx-chart-background-color: transparent;" +
            "-fx-legend-visible: false;" +
            "-fx-text-fill: " + URBAN_TEXT + ";"
        );
        
        tempChart.getXAxis().setStyle("-fx-text-fill: " + URBAN_TEXT_SECONDARY + ";");
        tempChart.getYAxis().setStyle("-fx-text-fill: " + URBAN_TEXT_SECONDARY + ";");
        
        // Add sample data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] hours = {"00:00", "04:00", "08:00", "12:00", "16:00", "20:00", "24:00"};
        int[] temps = {62, 60, 64, 68, 70, 66, 63};
        
        for (int i = 0; i < hours.length; i++) {
            series.getData().add(new XYChart.Data<>(hours[i], temps[i]));
        }
        
        tempChart.getData().add(series);
        
        // Style the line
        series.getNode().setStyle("-fx-stroke: " + URBAN_ACCENT + "; -fx-stroke-width: 3px;");
        
        // City comparison
        VBox comparisonBox = new VBox(10);
        comparisonBox.setPadding(new Insets(15, 0, 0, 0));
        
        Label compTitle = new Label("NEIGHBORHOOD COMPARISON");
        compTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + URBAN_ACCENT + ";");
        
        String[][] neighborhoods = {
            {"Downtown Core", "72°F", "+4°F"},
            {"Financial District", "70°F", "+2°F"},
            {"Residential Zone", "66°F", "-2°F"},
            {"Industrial Sector", "68°F", "±0°F"}
        };
        
        for (String[] neighborhood : neighborhoods) {
            HBox neighborhoodRow = new HBox(10);
            neighborhoodRow.setAlignment(Pos.CENTER_LEFT);
            
            Label name = new Label(neighborhood[0]);
            name.setStyle("-fx-font-size: 12px; -fx-text-fill: " + URBAN_TEXT + ";");
            name.setPrefWidth(120);
            
            Label temp = new Label(neighborhood[1]);
            temp.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + URBAN_TEXT + ";");
            
            Label diff = new Label(neighborhood[2]);
            String diffColor = neighborhood[2].contains("+") ? "#4CAF50" : 
                              neighborhood[2].contains("-") ? "#F44336" : URBAN_NEUTRAL;
            diff.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 2px 8px;" +
                "-fx-background-radius: 10px;" +
                "-fx-background-color: " + diffColor + ";" +
                "-fx-text-fill: white;"
            );
            
            neighborhoodRow.getChildren().addAll(name, temp);
            HBox.setHgrow(name, Priority.ALWAYS);
            neighborhoodRow.getChildren().add(diff);
            comparisonBox.getChildren().add(neighborhoodRow);
        }
        
        card.getChildren().addAll(title, tempChart, compTitle, comparisonBox);
        
        return card;
    }
    
    private VBox createUrbanMetricsCard() {
        VBox card = createTechCard();
        
        Label title = new Label("URBAN PERFORMANCE METRICS");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + URBAN_ACCENT + ";");
        title.setPadding(new Insets(0, 0, 15, 0));
        
        VBox metricsContainer = new VBox(20);
        
        // Traffic flow
        VBox trafficBox = new VBox(10);
        HBox trafficHeader = new HBox(10);
        trafficHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label trafficIcon = new Label("🚗");
        trafficIcon.setStyle("-fx-font-size: 20px;");
        Label trafficTitle = new Label("TRAFFIC FLOW");
        trafficTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        trafficHeader.getChildren().addAll(trafficIcon, trafficTitle);
        
        trafficProgress = new ProgressBar(0.65);
        trafficProgress.setPrefWidth(300);
        trafficProgress.setStyle(getProgressBarStyle(URBAN_ACCENT));
        
        HBox trafficInfo = new HBox();
        trafficInfo.setAlignment(Pos.CENTER_LEFT);
        trafficLevelLabel = new Label("MODERATE CONGESTION");
        trafficLevelLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + URBAN_ACCENT + ";");
        Label trafficDetail = new Label("65% of capacity");
        trafficDetail.setStyle("-fx-font-size: 11px; -fx-text-fill: " + URBAN_TEXT_SECONDARY + ";");
        trafficInfo.getChildren().addAll(trafficLevelLabel);
        HBox.setHgrow(trafficLevelLabel, Priority.ALWAYS);
        trafficInfo.getChildren().add(trafficDetail);
        
        trafficBox.getChildren().addAll(trafficHeader, trafficProgress, trafficInfo);
        
        // Air quality
        VBox airBox = new VBox(10);
        HBox airHeader = new HBox(10);
        airHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label airIcon = new Label("💨");
        airIcon.setStyle("-fx-font-size: 20px;");
        Label airTitle = new Label("AIR QUALITY INDEX");
        airTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        airHeader.getChildren().addAll(airIcon, airTitle);
        
        // AQI gauge
        HBox aqiGauge = new HBox();
        aqiGauge.setSpacing(2);
        
        String[] aqiLevels = {"Good", "Moderate", "Unhealthy"};
        String[] aqiColors = {"#4CAF50", "#FFC107", "#F44336"};
        int[] aqiValues = {42, 58, 72};
        
        for (int i = 0; i < aqiLevels.length; i++) {
            VBox gaugeSegment = new VBox(5);
            gaugeSegment.setAlignment(Pos.CENTER);
            
            Rectangle segment = new Rectangle(80, 20);
            segment.setFill(Color.web(aqiColors[i]));
            segment.setArcWidth(5);
            segment.setArcHeight(5);
            
            HBox segmentLabel = new HBox(5);
            segmentLabel.setAlignment(Pos.CENTER);
            Label level = new Label(aqiLevels[i]);
            level.setStyle("-fx-font-size: 10px; -fx-text-fill: white;");
            Label value = new Label(String.valueOf(aqiValues[i]));
            value.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: white;");
            
            segmentLabel.getChildren().addAll(level, value);
            gaugeSegment.getChildren().addAll(segment, segmentLabel);
            aqiGauge.getChildren().add(gaugeSegment);
        }
        
        airQualityLabel = new Label("CURRENT: 58 (MODERATE)");
        airQualityLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #FFC107;");
        
        airBox.getChildren().addAll(airHeader, aqiGauge, airQualityLabel);
        
        // Urban heat island effect
        VBox heatBox = new VBox(10);
        HBox heatHeader = new HBox(10);
        heatHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label heatIcon = new Label("🔥");
        heatIcon.setStyle("-fx-font-size: 20px;");
        Label heatTitle = new Label("HEAT ISLAND INTENSITY");
        heatTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        heatHeader.getChildren().addAll(heatIcon, heatTitle);
        
        ProgressBar heatProgress = new ProgressBar(0.45);
        heatProgress.setPrefWidth(300);
        heatProgress.setStyle(getProgressBarStyle("#FF5722"));
        
        Label heatDetail = new Label("45% above rural baseline");
        heatDetail.setStyle("-fx-font-size: 11px; -fx-text-fill: " + URBAN_TEXT_SECONDARY + ";");
        
        heatBox.getChildren().addAll(heatHeader, heatProgress, heatDetail);
        
        metricsContainer.getChildren().addAll(trafficBox, airBox, heatBox);
        card.getChildren().addAll(title, metricsContainer);
        
        return card;
    }
    
    private VBox createTrafficChartCard() {
        VBox card = createTechCard();
        
        Label title = new Label("REAL-TIME TRAFFIC PATTERNS");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + URBAN_ACCENT + ";");
        title.setPadding(new Insets(0, 0, 15, 0));
        
        // Create traffic chart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time of Day");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Traffic Density (%)");
        
        trafficChart = new AreaChart<>(xAxis, yAxis);
        trafficChart.setLegendVisible(false);
        trafficChart.setAnimated(true);
        trafficChart.setPrefHeight(300);
        
        // Style the chart
        trafficChart.setStyle(
            "-fx-chart-background-color: transparent;" +
            "-fx-legend-visible: false;"
        );
        
        trafficChart.getXAxis().setStyle("-fx-text-fill: " + URBAN_TEXT_SECONDARY + ";");
        trafficChart.getYAxis().setStyle("-fx-text-fill: " + URBAN_TEXT_SECONDARY + ";");
        
        // Add sample data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] times = {"6AM", "8AM", "10AM", "12PM", "2PM", "4PM", "6PM", "8PM"};
        int[] traffic = {40, 85, 65, 60, 55, 80, 75, 45};
        
        for (int i = 0; i < times.length; i++) {
            series.getData().add(new XYChart.Data<>(times[i], traffic[i]));
        }
        
        trafficChart.getData().add(series);
        
        // Style the area
        series.getNode().setStyle("-fx-stroke: " + URBAN_SECONDARY + "; -fx-stroke-width: 2px;");
        
        // Traffic hotspots
        VBox hotspotsBox = new VBox(10);
        hotspotsBox.setPadding(new Insets(15, 0, 0, 0));
        
        Label hotspotsTitle = new Label("CURRENT TRAFFIC HOTSPOTS");
        hotspotsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + URBAN_ACCENT + ";");
        
        String[][] hotspots = {
            {"🚗 Downtown Expressway", "Heavy", "85% capacity"},
            {"🚇 Central Station", "Moderate", "70% capacity"},
            {"🌉 River Bridge", "Light", "45% capacity"},
            {"🏢 Financial District", "Heavy", "90% capacity"}
        };
        
        for (String[] hotspot : hotspots) {
            HBox hotspotRow = new HBox(10);
            hotspotRow.setAlignment(Pos.CENTER_LEFT);
            
            Label name = new Label(hotspot[0]);
            name.setStyle("-fx-font-size: 12px; -fx-text-fill: " + URBAN_TEXT + ";");
            name.setPrefWidth(150);
            
            Label status = new Label(hotspot[1]);
            String statusColor = hotspot[1].equals("Heavy") ? "#F44336" : 
                                hotspot[1].equals("Moderate") ? "#FFC107" : "#4CAF50";
            status.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 2px 8px;" +
                "-fx-background-radius: 10px;" +
                "-fx-background-color: " + statusColor + ";" +
                "-fx-text-fill: white;"
            );
            
            Label detail = new Label(hotspot[2]);
            detail.setStyle("-fx-font-size: 11px; -fx-text-fill: " + URBAN_TEXT_SECONDARY + ";");
            
            hotspotRow.getChildren().addAll(name, status, detail);
            hotspotsBox.getChildren().add(hotspotRow);
        }
        
        card.getChildren().addAll(title, trafficChart, hotspotsTitle, hotspotsBox);
        
        return card;
    }
    
    private VBox createAirQualityCard() {
        VBox card = createTechCard();
        
        Label title = new Label("AIR POLLUTION ANALYSIS");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + URBAN_ACCENT + ";");
        title.setPadding(new Insets(0, 0, 15, 0));
        
        // Create pollution chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Concentration (µg/m³)");
        
        pollutionChart = new BarChart<>(xAxis, yAxis);
        pollutionChart.setLegendVisible(false);
        pollutionChart.setAnimated(true);
        pollutionChart.setPrefHeight(200);
        
        // Style the chart
        pollutionChart.setStyle(
            "-fx-chart-background-color: transparent;" +
            "-fx-legend-visible: false;"
        );
        
        pollutionChart.getXAxis().setStyle("-fx-text-fill: " + URBAN_TEXT_SECONDARY + ";");
        pollutionChart.getYAxis().setStyle("-fx-text-fill: " + URBAN_TEXT_SECONDARY + ";");
        
        // Add pollution data
        XYChart.Series<String, Number> pm25Series = new XYChart.Series<>();
        pm25Series.setName("PM2.5");
        
        XYChart.Series<String, Number> pm10Series = new XYChart.Series<>();
        pm10Series.setName("PM10");
        
        XYChart.Series<String, Number> no2Series = new XYChart.Series<>();
        no2Series.setName("NO₂");
        
        String[] pollutants = {"PM2.5", "PM10", "NO₂", "O₃", "SO₂"};
        int[] pm25Values = {12, 25, 8, 15, 6};
        int[] pm10Values = {25, 40, 15, 30, 12};
        int[] no2Values = {18, 32, 22, 28, 10};
        
        for (int i = 0; i < pollutants.length; i++) {
            pm25Series.getData().add(new XYChart.Data<>(pollutants[i], pm25Values[i]));
            pm10Series.getData().add(new XYChart.Data<>(pollutants[i], pm10Values[i]));
            no2Series.getData().add(new XYChart.Data<>(pollutants[i], no2Values[i]));
        }
        
        pollutionChart.getData().addAll(pm25Series, pm10Series, no2Series);
        
        // Color the bars
        for (XYChart.Data<String, Number> data : pm25Series.getData()) {
            data.getNode().setStyle("-fx-bar-fill: #4CAF50;");
        }
        for (XYChart.Data<String, Number> data : pm10Series.getData()) {
            data.getNode().setStyle("-fx-bar-fill: #FFC107;");
        }
        for (XYChart.Data<String, Number> data : no2Series.getData()) {
            data.getNode().setStyle("-fx-bar-fill: #F44336;");
        }
        
        // Pollution sources
        VBox sourcesBox = new VBox(10);
        sourcesBox.setPadding(new Insets(15, 0, 0, 0));
        
        Label sourcesTitle = new Label("MAJOR POLLUTION SOURCES");
        sourcesTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + URBAN_ACCENT + ";");
        
        String[][] sources = {
            {"🚗 Vehicle Emissions", "42%"},
            {"🏭 Industrial Activity", "28%"},
            {"🏢 Building Energy", "18%"},
            {"🚜 Construction Dust", "12%"}
        };
        
        for (String[] source : sources) {
            HBox sourceRow = new HBox(10);
            sourceRow.setAlignment(Pos.CENTER_LEFT);
            
            Label name = new Label(source[0]);
            name.setStyle("-fx-font-size: 12px; -fx-text-fill: " + URBAN_TEXT + ";");
            
            ProgressBar sourceBar = new ProgressBar(Double.parseDouble(source[1].replace("%", "")) / 100);
            sourceBar.setPrefWidth(150);
            sourceBar.setStyle(getProgressBarStyle(URBAN_SECONDARY));
            
            Label percentage = new Label(source[1]);
            percentage.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + URBAN_TEXT + ";");
            
            sourceRow.getChildren().addAll(name, sourceBar, percentage);
            sourcesBox.getChildren().add(sourceRow);
        }
        
        card.getChildren().addAll(title, pollutionChart, sourcesTitle, sourcesBox);
        
        return card;
    }
    
    private VBox createTransitCard() {
        VBox card = createTechCard();
        
        Label title = new Label("PUBLIC TRANSIT STATUS");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + URBAN_ACCENT + ";");
        title.setPadding(new Insets(0, 0, 15, 0));
        
        // Transit system overview
        VBox transitOverview = new VBox(15);
        
        // Metro status
        HBox metroRow = new HBox(10);
        metroRow.setAlignment(Pos.CENTER_LEFT);
        
        Label metroIcon = new Label("🚇");
        metroIcon.setStyle("-fx-font-size: 24px;");
        
        VBox metroInfo = new VBox(5);
        Label metroTitle = new Label("METRO SYSTEM");
        metroTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        transitProgress = new ProgressBar(0.72);
        transitProgress.setPrefWidth(200);
        transitProgress.setStyle(getProgressBarStyle("#4CAF50"));
        
        HBox metroDetails = new HBox();
        metroDetails.setAlignment(Pos.CENTER_LEFT);
        publicTransitLabel = new Label("72% ON TIME");
        publicTransitLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        Label metroCapacity = new Label("Avg. wait: 4.2 min");
        metroCapacity.setStyle("-fx-font-size: 11px; -fx-text-fill: " + URBAN_TEXT_SECONDARY + ";");
        
        metroDetails.getChildren().addAll(publicTransitLabel);
        HBox.setHgrow(publicTransitLabel, Priority.ALWAYS);
        metroDetails.getChildren().add(metroCapacity);
        
        metroInfo.getChildren().addAll(metroTitle, transitProgress, metroDetails);
        metroRow.getChildren().addAll(metroIcon, metroInfo);
        
        // Bus system
        HBox busRow = new HBox(10);
        busRow.setAlignment(Pos.CENTER_LEFT);
        
        Label busIcon = new Label("🚌");
        busIcon.setStyle("-fx-font-size: 24px;");
        
        VBox busInfo = new VBox(5);
        Label busTitle = new Label("BUS NETWORK");
        busTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        ProgressBar busProgress = new ProgressBar(0.65);
        busProgress.setPrefWidth(200);
        busProgress.setStyle(getProgressBarStyle("#2196F3"));
        
        Label busStatus = new Label("65% ON TIME • 128 active routes");
        busStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: " + URBAN_TEXT_SECONDARY + ";");
        
        busInfo.getChildren().addAll(busTitle, busProgress, busStatus);
        busRow.getChildren().addAll(busIcon, busInfo);
        
        // Bike share
        HBox bikeRow = new HBox(10);
        bikeRow.setAlignment(Pos.CENTER_LEFT);
        
        Label bikeIcon = new Label("🚲");
        bikeIcon.setStyle("-fx-font-size: 24px;");
        
        VBox bikeInfo = new VBox(5);
        Label bikeTitle = new Label("BIKE SHARE SYSTEM");
        bikeTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        HBox bikeStats = new HBox(20);
        VBox availableBox = createSmallStatBox("284", "Bikes Available", "#4CAF50");
        VBox stationsBox = createSmallStatBox("86%", "Stations Open", "#2196F3");
        VBox tripsBox = createSmallStatBox("3.2k", "Today's Trips", URBAN_ACCENT);
        
        bikeStats.getChildren().addAll(availableBox, stationsBox, tripsBox);
        bikeInfo.getChildren().addAll(bikeTitle, bikeStats);
        bikeRow.getChildren().addAll(bikeIcon, bikeInfo);
        
        // Transit alerts
        VBox alertsBox = new VBox(10);
        alertsBox.setPadding(new Insets(15, 0, 0, 0));
        
        Label alertsTitle = new Label("TRANSIT ALERTS");
        alertsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + URBAN_ACCENT + ";");
        
        TextArea transitAlerts = new TextArea();
        transitAlerts.setText("• Red Line: Normal service\n• Blue Line: 5 min delays\n• Green Line: Enhanced service\n• Bus Route 42: Detour active\n• Bike stations: Downtown high availability");
        transitAlerts.setEditable(false);
        transitAlerts.setWrapText(true);
        transitAlerts.setPrefHeight(80);
        transitAlerts.setStyle(
            "-fx-control-inner-background: " + URBAN_PANEL + ";" +
            "-fx-text-fill: " + URBAN_TEXT + ";" +
            "-fx-border-color: " + URBAN_SECONDARY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-font-size: 12px;"
        );
        
        alertsBox.getChildren().addAll(alertsTitle, transitAlerts);
        
        transitOverview.getChildren().addAll(metroRow, busRow, bikeRow, alertsBox);
        card.getChildren().addAll(title, transitOverview);
        
        return card;
    }
    
    private HBox createFooter() {
        HBox footer = new HBox(20);
        footer.setPadding(new Insets(15, 30, 15, 30));
        footer.setBackground(new Background(new BackgroundFill(
            Color.web(URBAN_PANEL), CornerRadii.EMPTY, Insets.EMPTY
        )));
        footer.setBorder(new Border(new BorderStroke(
            Color.web(URBAN_ACCENT), BorderStrokeStyle.SOLID, 
            CornerRadii.EMPTY, new BorderWidths(2, 0, 0, 0)
        )));
        footer.setAlignment(Pos.CENTER);
        
        // Data stream indicators
        HBox dataStreams = new HBox(30);
        dataStreams.setAlignment(Pos.CENTER_LEFT);
        
        String[][] streams = {
            {"📡", "248", "Sensors Active"},
            {"💾", "12.4k", "Data Points/Hour"},
            {"⚡", "98.2%", "Uptime"},
            {"🔗", "64", "Connected Systems"}
        };
        
        for (String[] stream : streams) {
            VBox streamBox = new VBox(5);
            streamBox.setAlignment(Pos.CENTER);
            
            Label icon = new Label(stream[0]);
            icon.setStyle("-fx-font-size: 20px;");
            
            Label value = new Label(stream[1]);
            value.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + URBAN_ACCENT + ";");
            
            Label label = new Label(stream[2]);
            label.setStyle("-fx-font-size: 11px; -fx-text-fill: " + URBAN_TEXT_SECONDARY + ";");
            
            streamBox.getChildren().addAll(icon, value, label);
            dataStreams.getChildren().add(streamBox);
        }
        
        // Action buttons - FIXED LINE: Added closing quote for color code
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button analyticsBtn = createTechButton("📊 ANALYTICS", URBAN_SECONDARY);
        Button forecastBtn = createTechButton("📈 FORECAST", URBAN_ACCENT);
        Button exportBtn = createTechButton("💾 EXPORT DATA", "#9C27B0");  // FIXED: Added closing quote
        Button refreshBtn = createTechButton("🔄 SYNC", "#4CAF50");
        
        buttonsBox.getChildren().addAll(analyticsBtn, forecastBtn, exportBtn, refreshBtn);
        
        footer.getChildren().addAll(dataStreams);
        HBox.setHgrow(dataStreams, Priority.ALWAYS);
        footer.getChildren().add(buttonsBox);
        
        // Add event handlers
        refreshBtn.setOnAction(e -> refreshData());
        analyticsBtn.setOnAction(e -> showAnalyticsMessage());
        forecastBtn.setOnAction(e -> showForecastMessage());
        exportBtn.setOnAction(e -> showExportMessage());
        
        return footer;
    }
    
    // Helper methods
    private VBox createTechCard() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: " + URBAN_PANEL + ";" +
            "-fx-background-radius: 8px;" +
            "-fx-border-radius: 8px;" +
            "-fx-border-color: " + URBAN_DARK + ";" +
            "-fx-border-width: 1px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);"
        );
        return card;
    }
    
    private VBox createMetricBox(String icon, String title, String value, String color) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + URBAN_TEXT_SECONDARY + ";");
        
        header.getChildren().addAll(iconLabel, titleLabel);
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        box.getChildren().addAll(header, valueLabel);
        return box;
    }
    
    private VBox createSmallStatBox(String value, String label, String color) {
        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER);
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + URBAN_TEXT_SECONDARY + ";");
        
        box.getChildren().addAll(valueLabel, labelLabel);
        return box;
    }
    
    private Button createTechButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8px 15px;" +
            "-fx-background-radius: 4px;" +
            "-fx-border-color: rgba(255,255,255,0.2);" +
            "-fx-border-width: 1px;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);"
        );
        
        button.setOnMouseEntered(e -> {
            button.setStyle(button.getStyle().replace(color, lightenColor(color, 20)));
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(button.getStyle().replace(lightenColor(color, 20), color));
        });
        
        return button;
    }
    
    private String getComboBoxStyle() {
        return "-fx-background-color: " + URBAN_DARK + ";" +
               "-fx-border-color: " + URBAN_ACCENT + ";" +
               "-fx-border-radius: 4px;" +
               "-fx-background-radius: 4px;" +
               "-fx-text-fill: " + URBAN_TEXT + ";" +
               "-fx-font-size: 14px;" +
               "-fx-font-weight: bold;" +
               "-fx-padding: 8px 15px;";
    }
    
    private String getToggleButtonStyle(boolean isSelected) {
        String baseStyle = "-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6px 15px; -fx-background-radius: 4px;";
        
        if (isSelected) {
            return baseStyle + 
                   "-fx-background-color: #4CAF50;" +
                   "-fx-text-fill: white;" +
                   "-fx-border-color: #45a049;" +
                   "-fx-border-width: 1px;";
        } else {
            return baseStyle + 
                   "-fx-background-color: " + URBAN_DARK + ";" +
                   "-fx-text-fill: " + URBAN_TEXT_SECONDARY + ";" +
                   "-fx-border-color: " + URBAN_NEUTRAL + ";" +
                   "-fx-border-width: 1px;";
        }
    }
    
    private String getProgressBarStyle(String color) {
        return "-fx-accent: " + color + ";" +
               "-fx-background-color: " + URBAN_DARK + ";" +
               "-fx-border-color: " + URBAN_DARK + ";" +
               "-fx-border-radius: 5px;" +
               "-fx-background-radius: 5px;" +
               "-fx-padding: 2px;";
    }
    
    private String lightenColor(String hexColor, int percent) {
        try {
            Color color = Color.web(hexColor);
            Color lighter = color.brighter();
            return String.format("#%02X%02X%02X",
                (int)(lighter.getRed() * 255),
                (int)(lighter.getGreen() * 255),
                (int)(lighter.getBlue() * 255));
        } catch (Exception e) {
            return hexColor;
        }
    }
    
    private void initializeData() {
        updateCityData();
        updateTime();
    }
    
    private void startLiveUpdates() {
        if (liveUpdateTimeline != null) {
            liveUpdateTimeline.stop();
        }
        
        liveUpdateTimeline = new Timeline(
            new KeyFrame(Duration.seconds(5), e -> {
                updateLiveData();
                updateTime();
            })
        );
        liveUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        liveUpdateTimeline.play();
    }
    
    private void stopLiveUpdates() {
        if (liveUpdateTimeline != null) {
            liveUpdateTimeline.stop();
        }
    }
    
    private void updateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        updateTimeLabel.setText("SYNC: " + now.format(formatter));
    }
    
    private void updateCityData() {
        String city = cityComboBox.getValue();
        
        if (city.contains("METROPOLIS")) {
            cityNameLabel.setText("METROPOLIS CENTRAL");
            temperatureLabel.setText("68°F");
            trafficLevelLabel.setText("MODERATE CONGESTION");
            trafficProgress.setProgress(0.65);
            airQualityLabel.setText("CURRENT: 58 (MODERATE)");
            publicTransitLabel.setText("72% ON TIME");
            transitProgress.setProgress(0.72);
        } else if (city.contains("BAYSIDE")) {
            cityNameLabel.setText("BAYSIDE URBAN ZONE");
            temperatureLabel.setText("64°F");
            trafficLevelLabel.setText("LIGHT TRAFFIC");
            trafficProgress.setProgress(0.45);
            airQualityLabel.setText("CURRENT: 42 (GOOD)");
            publicTransitLabel.setText("85% ON TIME");
            transitProgress.setProgress(0.85);
        }
        
        // Update charts with new data
        updateCharts();
    }
    
    private void updateLiveData() {
        if (!liveDataToggle.isSelected()) return;
        
        // Simulate live data updates
        double currentTemp = Double.parseDouble(temperatureLabel.getText().replace("°F", ""));
        double newTemp = currentTemp + (Math.random() * 2 - 1); // Small random change
        temperatureLabel.setText(String.format("%.0f°F", newTemp));
        
        // Update traffic with small random variations
        double currentTraffic = trafficProgress.getProgress();
        double newTraffic = Math.max(0.3, Math.min(0.9, currentTraffic + (Math.random() * 0.1 - 0.05)));
        trafficProgress.setProgress(newTraffic);
        
        // Update traffic level text
        if (newTraffic < 0.5) {
            trafficLevelLabel.setText("LIGHT TRAFFIC");
            trafficLevelLabel.setStyle("-fx-text-fill: #4CAF50;");
        } else if (newTraffic < 0.75) {
            trafficLevelLabel.setText("MODERATE CONGESTION");
            trafficLevelLabel.setStyle("-fx-text-fill: #FFC107;");
        } else {
            trafficLevelLabel.setText("HEAVY CONGESTION");
            trafficLevelLabel.setStyle("-fx-text-fill: #F44336;");
        }
        
        // Update transit
        double currentTransit = transitProgress.getProgress();
        double newTransit = Math.max(0.6, Math.min(0.95, currentTransit + (Math.random() * 0.05 - 0.025)));
        transitProgress.setProgress(newTransit);
        publicTransitLabel.setText(String.format("%.0f%% ON TIME", newTransit * 100));
        
        // Update air quality
        int aqi = 42 + (int)(Math.random() * 30);
        airQualityLabel.setText("CURRENT: " + aqi + " (" + getAQILevel(aqi) + ")");
        
        // Update charts
        updateCharts();
    }
    
    private String getAQILevel(int aqi) {
        if (aqi <= 50) return "GOOD";
        if (aqi <= 100) return "MODERATE";
        if (aqi <= 150) return "UNHEALTHY FOR SENSITIVE";
        if (aqi <= 200) return "UNHEALTHY";
        return "VERY UNHEALTHY";
    }
    
    private void updateCharts() {
        // Update temperature chart with slight variations
        if (tempChart.getData().size() > 0) {
            XYChart.Series<String, Number> series = tempChart.getData().get(0);
            for (XYChart.Data<String, Number> data : series.getData()) {
                double currentValue = data.getYValue().doubleValue();
                double newValue = currentValue + (Math.random() * 2 - 1);
                data.setYValue(newValue);
            }
        }
        
        // Update traffic chart
        if (trafficChart.getData().size() > 0) {
            XYChart.Series<String, Number> series = trafficChart.getData().get(0);
            for (XYChart.Data<String, Number> data : series.getData()) {
                double currentValue = data.getYValue().doubleValue();
                double newValue = Math.max(30, Math.min(95, currentValue + (Math.random() * 10 - 5)));
                data.setYValue(newValue);
            }
        }
    }
    
    private void refreshData() {
        updateCityData();
        updateTime();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Data Synchronized");
        alert.setHeaderText(null);
        alert.setContentText("All urban data streams have been synchronized with the latest sensor readings.");
        alert.show();
    }
    
    private void showAnalyticsMessage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Advanced Analytics");
        alert.setHeaderText("Urban Intelligence Analytics");
        alert.setContentText("Access detailed analytics including:\n\n" +
            "• Traffic pattern prediction\n" +
            "• Pollution source attribution\n" +
            "• Transit optimization models\n" +
            "• Urban heat island analysis\n" +
            "• Real-time anomaly detection\n\n" +
            "Connect to Urban Pulse Labs API for full access.");
        alert.showAndWait();
    }
    
    private void showForecastMessage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Urban Forecast");
        alert.setHeaderText("48-Hour Urban Forecast");
        alert.setContentText("🏙️ METROPOLIS CENTRAL FORECAST:\n\n" +
            "NEXT 24 HOURS:\n" +
            "• Peak traffic: 7-9AM, 4-6PM\n" +
            "• Air quality: Moderate\n" +
            "• Transit reliability: 78%\n" +
            "• Temperature range: 64-72°F\n\n" +
            "NEXT 48 HOURS:\n" +
            "• Expected rainfall: 40%\n" +
            "• Traffic impact: +15%\n" +
            "• Air quality improvement: Likely");
        alert.showAndWait();
    }
    
    private void showExportMessage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Data Export");
        alert.setHeaderText("Export Urban Data");
        alert.setContentText("Export options available:\n\n" +
            "• CSV: Raw sensor data\n" +
            "• JSON: API-ready format\n" +
            "• PDF: Analytics reports\n" +
            "• Real-time stream: WebSocket\n\n" +
            "Select format and time range in the export panel.");
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}