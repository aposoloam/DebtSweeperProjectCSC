//Project for CSC 1061, haven't commented anything because i'm lazy


import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DebtSweeper extends Application {

    private Map<String, String> credentials = new HashMap<>();
    private Map<String, Double> debts = new HashMap<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Debt Calculator");
        loadCredentialsFromFile();
        loadDebtsFromFile();
        Scene loginScene = createLoginScene(primaryStage);
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private Scene createLoginScene(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        Label usernameLabel = new Label("Username:");
        TextField usernameInput = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordInput = new PasswordField();
        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        grid.add(usernameLabel, 0, 0);
        grid.add(usernameInput, 1, 0);
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordInput, 1, 1);
        grid.add(loginButton, 1, 2);
        grid.add(registerButton, 1, 3);

        loginButton.setOnAction(e -> {
            if (authenticate(usernameInput.getText(), passwordInput.getText())) {
                primaryStage.setScene(createDebtCalculationScene(primaryStage));
            } else {
                showAlert(AlertType.ERROR, "Login Failed", "Invalid username or password.");
            }
        });

        registerButton.setOnAction(e -> {
            primaryStage.setScene(createRegistrationScene(primaryStage));
        });

        return new Scene(grid, 400, 300);
    }

    private Scene createRegistrationScene(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        Label usernameLabel = new Label("Username:");
        TextField usernameInput = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordInput = new PasswordField();
        Button registerButton = new Button("Register");
        Button backButton = new Button("Back");

        grid.add(usernameLabel, 0, 0);
        grid.add(usernameInput, 1, 0);
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordInput, 1, 1);
        grid.add(registerButton, 1, 2);
        grid.add(backButton, 1, 3);

        registerButton.setOnAction(e -> {
            String username = usernameInput.getText();
            String password = passwordInput.getText();
            if (!credentials.containsKey(username)) {
                credentials.put(username, password);
                saveCredentialsToFile(username, password);
                showAlert(AlertType.INFORMATION, "Registration Successful", "You may now login.");
                primaryStage.setScene(createLoginScene(primaryStage));
            } else {
                showAlert(AlertType.ERROR, "Registration Failed", "Username already exists.");
            }
        });

        backButton.setOnAction(e -> {
            primaryStage.setScene(createLoginScene(primaryStage));
        });

        return new Scene(grid, 400, 300);
    }

    private Scene createDebtCalculationScene(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        Label nameLabel = new Label("Name:");
        TextField nameInput = new TextField();
        Label debtLabel = new Label("Debt:");
        TextField debtInput = new TextField();
        Label interestRateLabel = new Label("Interest Rate (%):");
        TextField interestRateInput = new TextField();
        Button calculateButton = new Button("Calculate");
        Button viewDebtsButton = new Button("View Debts");
        Button logoutButton = new Button("Logout");

        grid.add(nameLabel, 0, 0);
        grid.add(nameInput, 1, 0);
        grid.add(debtLabel, 0, 1);
        grid.add(debtInput, 1, 1);
        grid.add(interestRateLabel, 0, 2);
        grid.add(interestRateInput, 1, 2);
        grid.add(calculateButton, 1, 3);
        grid.add(viewDebtsButton, 1, 4);
        grid.add(logoutButton, 1, 5);

        calculateButton.setOnAction(e -> {
            try {
                String name = nameInput.getText();
                double debtAmount = Double.parseDouble(debtInput.getText());
                double interestRate = Double.parseDouble(interestRateInput.getText());
                double futureDebt = calculateFutureDebt(debtAmount, interestRate);
                debts.put(name, futureDebt);
                saveDebtsToFile();
                showAlert(AlertType.INFORMATION, "Calculation Complete",
                        "Future debt for " + name + " is: $" + String.format("%.2f", futureDebt));
            } catch (NumberFormatException ex) {
                showAlert(AlertType.ERROR, "Calculation Error",
                        "Please ensure that debt and interest rate are valid numbers.");
            }
        });

        viewDebtsButton.setOnAction(e -> primaryStage.setScene(createViewDebtsScene(primaryStage)));

        logoutButton.setOnAction(e -> primaryStage.setScene(createLoginScene(primaryStage)));

        return new Scene(grid, 400, 300);
    }

    private Scene createViewDebtsScene(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        TextArea debtsArea = new TextArea();
        debtsArea.setEditable(false);
        StringBuilder debtsText = new StringBuilder();
        for (Map.Entry<String, Double> entry : debts.entrySet()) {
            debtsText.append(entry.getKey()).append(": $").append(String.format("%.2f", entry.getValue())).append("\n");
        }
        debtsArea.setText(debtsText.toString());
        grid.add(debtsArea, 0, 0);

        Button backButton = new Button("Back");
        grid.add(backButton, 0, 1);
        GridPane.setColumnSpan(debtsArea, 2);
        GridPane.setColumnSpan(backButton, 2);

        backButton.setOnAction(e -> primaryStage.setScene(createDebtCalculationScene(primaryStage)));

        return new Scene(grid, 400, 300);
    }

    private double calculateFutureDebt(double debtAmount, double interestRate) {
        return debtAmount * (1 + interestRate / 100);
    }

    private void loadCredentialsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("credentials.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                credentials.put(parts[0], parts[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveCredentialsToFile(String username, String password) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("credentials.txt", true))) {
            writer.write(username + ":" + password);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadDebtsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("debts.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                debts.put(parts[0], Double.parseDouble(parts[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDebtsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("debts.txt"))) {
            for (Map.Entry<String, Double> entry : debts.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean authenticate(String username, String password) {
        return credentials.containsKey(username) && credentials.get(username).equals(password);
    }

    public static void main(String[] args) {
        launch(args);
    }
}