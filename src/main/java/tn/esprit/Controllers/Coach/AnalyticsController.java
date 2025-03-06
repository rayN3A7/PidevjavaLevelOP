package tn.esprit.Controllers.Coach;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.Controllers.forum.AdminSidebarController;
import tn.esprit.Models.Role;
import tn.esprit.Models.Session_game;
import tn.esprit.Services.ServiceSession;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class AnalyticsController implements Initializable {

    @FXML private LineChart<String, Number> revenueChart;
    @FXML private PieChart paymentMethodChart;
    @FXML private BarChart<String, Number> sessionChart;
    @FXML private GridPane kpiGrid;
    @FXML private DatePicker startDate;
    @FXML private DatePicker endDate;
    @FXML private ComboBox<String> filterCoach;
    @FXML private ComboBox<String> filterSessionType;
    @FXML private VBox dataContainer;
    @FXML private AdminSidebarController sidebarController;
    @FXML private BorderPane mainLayout;

    private ServiceSession sessionService;
    private UtilisateurService utilisateurService;
    private Map<Integer, String> coachNamesMap;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (revenueChart == null || paymentMethodChart == null || sessionChart == null ||
                kpiGrid == null || startDate == null || endDate == null ||
                filterCoach == null || filterSessionType == null || dataContainer == null) {
            System.err.println("Erreur : Certains éléments FXML n'ont pas été injectés correctement.");
        }
        if (SessionManager.getInstance().getRole() == Role.ADMIN) {
            loadAdminSidebar();
        }
        sessionService = new ServiceSession();
        utilisateurService = new UtilisateurService();
        loadCoachNames();

        initializeFilters();
        initializeCharts();
        updateDashboard();
    }

    private void loadCoachNames() {
        coachNamesMap = new HashMap<>();
        List<Session_game> allSessions = sessionService.getAll();
        Set<Integer> uniqueCoachIds = allSessions.stream()
                .map(Session_game::getCoach_id)
                .collect(Collectors.toSet());

        for (Integer coachId : uniqueCoachIds) {
            try {
                String coachName = utilisateurService.getOne(coachId).getNom();
                coachNamesMap.put(coachId, coachName);
            } catch (Exception e) {
                System.err.println("Error loading coach name for ID " + coachId + ": " + e.getMessage());
                coachNamesMap.put(coachId, "Coach " + coachId);
            }
        }
    }

    private void loadAdminSidebar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/sidebarAdmin.fxml"));
            VBox adminSidebar = loader.load();
            sidebarController = loader.getController();
            mainLayout.setLeft(adminSidebar);
        } catch (IOException e) {
            System.err.println("Error loading admin sidebar: " + e.getMessage());
        }
    }

    private void initializeFilters() {
        startDate.setValue(LocalDate.now().minusMonths(1));
        endDate.setValue(LocalDate.now());

        Set<Integer> uniqueCoachIds = sessionService.getAll().stream()
                .map(Session_game::getCoach_id)
                .collect(Collectors.toSet());

        ObservableList<String> coachList = FXCollections.observableArrayList();
        coachList.add("All");
        uniqueCoachIds.stream()
                .map(id -> coachNamesMap.getOrDefault(id, "Coach " + id))
                .sorted()
                .forEach(coachList::add);

        filterCoach.setItems(coachList);
        filterCoach.setValue("All");

        filterSessionType.setItems(FXCollections.observableArrayList("All", "Regular", "Promotional"));
        filterSessionType.setValue("All");

        startDate.valueProperty().addListener((obs, oldVal, newVal) -> updateDashboard());
        endDate.valueProperty().addListener((obs, oldVal, newVal) -> updateDashboard());
        filterCoach.valueProperty().addListener((obs, oldVal, newVal) -> updateDashboard());
        filterSessionType.valueProperty().addListener((obs, oldVal, newVal) -> updateDashboard());
    }

    private void initializeCharts() {
        revenueChart.setTitle("Revenue Over Time");
        paymentMethodChart.setTitle("Session Price Distribution");
        sessionChart.setTitle("Sessions by Game");
    }

    private void updateDashboard() {
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();
        String selectedCoach = filterCoach.getValue();
        String sessionType = filterSessionType.getValue();

        List<Session_game> sessions = getFilteredSessions(start, end, selectedCoach, sessionType);

        updateKPIs(sessions);
        updateCharts(sessions);
        updateDataDisplay(sessions);
    }

    private List<Session_game> getFilteredSessions(LocalDate start, LocalDate end, String coachName, String sessionType) {
        List<Session_game> sessions = sessionService.getAll();
        Date startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant());

        return sessions.stream()
                .filter(s -> !s.getdate_creation().before(startDate) && !s.getdate_creation().after(endDate))
                .filter(s -> "All".equals(coachName) || coachNamesMap.getOrDefault(s.getCoach_id(), "").equals(coachName))
                .filter(s -> "All".equals(sessionType) ||
                        ("Promotional".equals(sessionType) && s.getprix() < 60) ||
                        ("Regular".equals(sessionType) && s.getprix() >= 60))
                .collect(Collectors.toList());
    }

    private void updateKPIs(List<Session_game> sessions) {
        long totalSessions = sessions.size();
        double totalRevenue = sessions.stream().mapToDouble(Session_game::getprix).sum();
        long promoSessions = sessions.stream().filter(s -> s.getprix() < 60).count();

        Label totalSessionsLabel = new Label("Total Sessions: " + totalSessions);
        totalSessionsLabel.getStyleClass().add("kpi-label");

        Label totalRevenueLabel = new Label(String.format("Total Revenue: %.2f €", totalRevenue));
        totalRevenueLabel.getStyleClass().add("kpi-label");

        Label promoSessionsLabel = new Label("Promotional Sessions: " + promoSessions);
        promoSessionsLabel.getStyleClass().add("kpi-label");

        kpiGrid.getChildren().clear();
        kpiGrid.add(totalSessionsLabel, 0, 0);
        kpiGrid.add(totalRevenueLabel, 1, 0);
        kpiGrid.add(promoSessionsLabel, 2, 0);
    }

    private void updateCharts(List<Session_game> sessions) {
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Daily Revenue");
        Map<String, Double> dailyRevenue = sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> new SimpleDateFormat("yyyy-MM-dd").format(s.getdate_creation()),
                        Collectors.summingDouble(Session_game::getprix)));
        dailyRevenue.forEach((date, revenue) ->
                revenueSeries.getData().add(new XYChart.Data<>(date, revenue)));
        revenueChart.getData().clear();
        revenueChart.getData().add(revenueSeries);

        ObservableList<PieChart.Data> priceData = FXCollections.observableArrayList(
                new PieChart.Data("Regular (≥60€)", sessions.stream().filter(s -> s.getprix() >= 60).count()),
                new PieChart.Data("Promotional (<60€)", sessions.stream().filter(s -> s.getprix() < 60).count())
        );
        paymentMethodChart.setData(priceData);

        XYChart.Series<String, Number> gameSeries = new XYChart.Series<>();
        gameSeries.setName("Sessions per Game");
        Map<String, Long> gameStats = sessions.stream()
                .collect(Collectors.groupingBy(Session_game::getGame, Collectors.counting()));
        gameStats.forEach((game, count) ->
                gameSeries.getData().add(new XYChart.Data<>(game, count)));
        sessionChart.getData().clear();
        sessionChart.getData().add(gameSeries);
    }

    private void updateDataDisplay(List<Session_game> sessions) {
        dataContainer.getChildren().clear();

        HBox header = new HBox(20);
        header.getStyleClass().add("data-header");
        Label coachHeader = new Label("Coach Name");
        coachHeader.setPrefWidth(200);
        Label gameHeader = new Label("Game");
        gameHeader.setPrefWidth(200);
        Label priceHeader = new Label("Price");
        priceHeader.setPrefWidth(100);
        header.getChildren().addAll(coachHeader, gameHeader, priceHeader);
        dataContainer.getChildren().add(header);

        for (Session_game session : sessions) {
            HBox row = new HBox(20);
            row.getStyleClass().add("data-row");

            String coachName = coachNamesMap.getOrDefault(session.getCoach_id(), "Coach " + session.getCoach_id());
            Label coachLabel = new Label(coachName);
            coachLabel.setPrefWidth(200);
            Label gameLabel = new Label(session.getGame());
            gameLabel.setPrefWidth(200);
            Label priceLabel = new Label(String.format("%.2f €", session.getprix()));
            priceLabel.setPrefWidth(100);

            row.getChildren().addAll(coachLabel, gameLabel, priceLabel);
            dataContainer.getChildren().add(row);
        }
    }
}