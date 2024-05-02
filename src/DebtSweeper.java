/* Peyton Cleaver
 * 05/01/2024
 * CSC 1061, Patrick McDougle
 * A GUI Program to Help Users Understand Debt
 * Check README.md for more information
 */

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DebtSweeper extends Application {

    private Map<String, String> credentials = new HashMap<>();
    private Map<String, DebtInfo> debts = new HashMap<>();

    static class DebtInfo {
        double originalAmount;
        List<Double> payments = new ArrayList<>();

        DebtInfo(double originalAmount) {
            this.originalAmount = originalAmount;
        }

        double getCurrentDebt() {
            return originalAmount - payments.stream().mapToDouble(Double::doubleValue).sum();
        }
    }

    private double userIncome = 0;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("DebtSweeper: the Debt Calculator");
        primaryStage.getIcons().add(new Image("file:icon.png"));
        loadCredentialsFromFile();
        loadDebtsFromFile();
        Scene loginScene = createLoginScene(primaryStage);
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private Scene createLoginScene(Stage primaryStage) {
        primaryStage.setTitle("DebtSweeper: the Debt Calculator");
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
        primaryStage.setTitle("Register");
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
        primaryStage.setTitle("Enter a New Debt");
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
                if (!debts.containsKey(name)) {
                    double debtAmount = Double.parseDouble(debtInput.getText());
                    double interestRate = Double.parseDouble(interestRateInput.getText());
                    DebtInfo newDebt = new DebtInfo(debtAmount * (1 + interestRate / 100));
                    debts.put(name, newDebt);
                    saveDebtsToFile();
                    showAlert(AlertType.INFORMATION, "Calculation Complete",
                            "Future debt for " + name + " is: $" + String.format("%.2f", newDebt.originalAmount));
                } else {
                    showAlert(AlertType.ERROR, "Input Failed", "Debt name already exists.");
                }

            } catch (NumberFormatException ex) {
                showAlert(AlertType.ERROR, "Calculation Error",
                        "Please ensure that debt and interest rate are valid numbers.");
            }
        });

        viewDebtsButton.setOnAction(e -> primaryStage.setScene(createViewDebtsScene(primaryStage)));

        logoutButton.setOnAction(e -> primaryStage.setScene(createLoginScene(primaryStage)));

        return new Scene(grid, 400, 300);
    }

    private Scene createIncomeScene(Stage primaryStage) {
        primaryStage.setTitle("View Your Income");
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        Label incomeLabel = new Label("Enter your monthly income:");
        TextField incomeField = new TextField();
        incomeField.setPromptText("Enter amount in dollars");

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            try {
                double income = Double.parseDouble(incomeField.getText());
                userIncome = income; // save the income
                showAlert(AlertType.INFORMATION, "Submission Successful", "Income submitted: $" + income);
                primaryStage.setScene(createViewDebtsScene(primaryStage)); // go back to view debts
            } catch (NumberFormatException ex) {
                showAlert(AlertType.ERROR, "Error", "Please enter a valid number.");
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> primaryStage.setScene(createViewDebtsScene(primaryStage)));

        grid.add(incomeLabel, 0, 0);
        grid.add(incomeField, 1, 0);
        grid.add(submitButton, 1, 1);
        grid.add(backButton, 0, 1);

        return new Scene(grid, 400, 300);
    }

    private Scene createViewDebtsScene(Stage primaryStage) {
        primaryStage.setTitle("View Your Debts");
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        Label incomeDisplayLabel = new Label("Your monthly income: $" + String.format("%.2f", userIncome));
        grid.add(incomeDisplayLabel, 0, 0);

        TextArea debtsArea = new TextArea();
        debtsArea.setEditable(false);
        StringBuilder debtsText = new StringBuilder();
        for (Map.Entry<String, DebtInfo> entry : debts.entrySet()) {
            String payoffTime = calculatePayoffTime(entry.getValue().getCurrentDebt(), userIncome);
            debtsText.append(entry.getKey()).append(": $")
                    .append(String.format("%.2f", entry.getValue().getCurrentDebt()))
                    .append(" - Payoff in " + payoffTime).append("\n");
        }
        debtsArea.setText(debtsText.toString());
        grid.add(debtsArea, 0, 1);

        Button incomeButton = new Button("Update Income");
        incomeButton.setOnAction(e -> primaryStage.setScene(createIncomeScene(primaryStage)));
        grid.add(incomeButton, 0, 2);

        Button paymentButton = new Button("Input a Payment");
        paymentButton.setOnAction(e -> primaryStage.setScene(createPaymentScene(primaryStage)));
        grid.add(paymentButton, 0, 3);

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> primaryStage.setScene(createDebtCalculationScene(primaryStage)));
        grid.add(backButton, 0, 4);
        
        return new Scene(grid, 400, 300);
    }
private Scene createPaymentScene(Stage primaryStage) {
    GridPane grid = new GridPane();
    grid.setPadding(new Insets(20));
    grid.setVgap(10);
    grid.setHgap(10);
    grid.setAlignment(Pos.CENTER);

    ComboBox<String> debtNames = new ComboBox<>();
    debtNames.setPromptText("Select a debt");
    debts.keySet().forEach(debtNames.getItems()::add);
    grid.add(debtNames, 0, 0);

    TextField paymentAmountField = new TextField();
    paymentAmountField.setPromptText("Payment Amount");
    grid.add(paymentAmountField, 0, 1);

    Button submitPaymentButton = new Button("Submit Payment");
    submitPaymentButton.setOnAction(e -> {
        if (debtNames.getValue() != null && !paymentAmountField.getText().isEmpty()) {
            try {
                double paymentAmount = Double.parseDouble(paymentAmountField.getText());
                applyPayment(debtNames.getValue(), paymentAmount);
                primaryStage.setScene(createViewDebtsScene(primaryStage)); // Go back or refresh
            } catch (NumberFormatException ex) {
                showAlert(AlertType.ERROR, "Payment Error", "Please enter a valid number.");
            }
        } else {
            showAlert(AlertType.ERROR, "Payment Error", "Please select a debt and enter a payment amount.");
        }
    });
    grid.add(submitPaymentButton, 0, 2);

    Button backButton = new Button("Back");
    backButton.setOnAction(e -> primaryStage.setScene(createViewDebtsScene(primaryStage)));
    grid.add(backButton, 0, 3);

    return new Scene(grid, 300, 200);
}
    public void applyPayment(String debtName, double paymentAmount) {
        if (debts.containsKey(debtName)) {
            DebtInfo debtInfo = debts.get(debtName);
            debtInfo.payments.add(paymentAmount);
            saveDebtsToFile();

            double remainingDebt = debtInfo.getCurrentDebt();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Payment Applied");
            alert.setHeaderText(null);
            alert.setContentText("Payment of $" + paymentAmount + " has been applied to " + debtName
                    + ". Remaining debt: $" + remainingDebt);
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No debt found with the name: " + debtName);
            alert.showAndWait();
        }
    }

    private String calculatePayoffTime(double debt, double monthlyPayment) {
        if (monthlyPayment <= 0) {
            return "Invalid payment amount";
        }
        int months = (int) Math.ceil(debt / monthlyPayment);
        return months + " month(s)";
    }

    public void displayFinancialTip() {
        double totalDebt = debts.values().stream().mapToDouble(d -> d.getCurrentDebt()).sum();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Financial Tip");
        alert.setHeaderText(null);
        if (totalDebt > 5000) {
            alert.setContentText("Consider consolidating your debts to reduce interest rates.");
        } else if (totalDebt > 0) {
            alert.setContentText("Keep up the good work, and try to pay off smaller debts first!");
        } else {
            alert.setContentText("Congratulations on having no debt! Keep managing your finances wisely.");
        }
        alert.showAndWait();
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
                double amount = Double.parseDouble(parts[1]);
                DebtInfo debtInfo = new DebtInfo(amount);
                if (parts.length > 2) { // Existing payments
                    String[] payments = parts[2].split(",");
                    for (String payment : payments) {
                        debtInfo.payments.add(Double.parseDouble(payment));
                    }
                }
                debts.put(parts[0], debtInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDebtsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("debts.txt"))) {
            for (Map.Entry<String, DebtInfo> entry : debts.entrySet()) {
                DebtInfo info = entry.getValue();
                String payments = info.payments.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                writer.write(entry.getKey() + ":" + info.originalAmount + ":" + payments);
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