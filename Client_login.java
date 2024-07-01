package com.example.kalgr_projekt_todo;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client_login extends Application {
    private Socket clientSocket;
    private BufferedReader serverInput;
    private PrintWriter serverOutput;

    /**
     * ha sikeres  belépés/regiszráció, új ablakban nyitja meg a rendes progit
     */
    public void start(Stage stage) {
        Label lSE = new Label("A szerver nem elérhető");
        lSE.setVisible(false);
        try {
            clientSocket = new Socket("localhost", ServerStarter.PORT_NUMBER);
            serverInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            serverOutput = new PrintWriter(clientSocket.getOutputStream());
        } catch (IOException e) {
            lSE.setVisible(true);
        }

        stage.setTitle("Welcome Stranger");
        AnchorPane root = new AnchorPane();
        Label lF = new Label("Felhasználónév:");
        Label lJ = new Label("Jelszó:");
        Label lE = new Label("Helytelen belépés");
        lE.setVisible(false);
        TextField tfF = new TextField();
        TextField tfJ = new TextField();
        Button loginer = new Button("Log in");
        Button reg = new Button("Registrate");
        root.getChildren().addAll(lF, lJ, tfF, tfJ, loginer, lE, lSE, reg);
        AnchorPane.setTopAnchor(lF, 10d);
        AnchorPane.setTopAnchor(lJ, 50d);
        AnchorPane.setTopAnchor(tfF, 10d);
        AnchorPane.setTopAnchor(tfJ, 50d);
        AnchorPane.setTopAnchor(loginer, 90d);
        AnchorPane.setTopAnchor(reg, 90d);
        AnchorPane.setTopAnchor(lE, 120d);
        AnchorPane.setTopAnchor(lSE, 120d);

        AnchorPane.setLeftAnchor(lF, 10d);
        AnchorPane.setLeftAnchor(lJ, 10d);
        AnchorPane.setLeftAnchor(tfF, 100d);
        AnchorPane.setLeftAnchor(tfJ, 100d);
        AnchorPane.setLeftAnchor(loginer, 100d);
        AnchorPane.setLeftAnchor(reg, 10d);
        AnchorPane.setLeftAnchor(lE, 170d);
        AnchorPane.setLeftAnchor(lSE, 100d);
        tfF.setText("b");
        tfJ.setText("b");
        //
        /**
         * enter, space és visszaper nem lehet a felhasználónév-jelszban
         * ha teljesül, küldi a login kérelmet
         * ha nemjó, kiírja az elutasítást, törli a beviteli ezőket
         */
        loginer.setOnAction(e -> new Thread(new Task<Boolean>() {
            @Override
            public Boolean call() {

                if (!tfF.getText().equals("") && !tfJ.getText().equals("")) {
                    try {
                        if (tfF.getText().contains(" ") || tfF.getText().contains("\\") || tfF.getText().contains(":")) {
                            tfF.setText("");
                            tfJ.setText("");
                            lE.setVisible(true);
                        } else {
                            sendLine(tfF.getText());
                            sendLine(tfJ.getText());
                        }
                    } catch (IOException ex) {
                        System.out.println("Nem tudta elküldeni az üzenetet");
                    }
                    try {
                        return expect("Welcome");
                    } catch (IOException ex) {
                        System.out.println("Nem érkezett meg megfelelően a szerver válasza");
                    }
                }
                return false;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                if (this.getValue()) {
                    lE.setText("Belépve");
                    try {
                        new ClientGUI(clientSocket, serverInput, serverOutput).start(stage);
                    } catch (IOException ex) {
                        System.out.println("Nem indult el a GUI");
                    }
                } else {
                    tfF.setText("");
                    tfJ.setText("");
                    lE.setVisible(true);
                }
            }
        }).start());
        /**
         * ugyan az mint a login, csak regisztrálást kér
         */
        reg.setOnAction(e -> new Thread(new Task<Boolean>() {
            @Override
            public Boolean call() {
                if (!tfF.getText().equals("") && !tfJ.getText().equals("")) {
                    try {
                        if (tfF.getText().contains(" ") || tfF.getText().contains("\\") || tfF.getText().contains(":")) {
                            tfF.setText("");
                            tfJ.setText("");
                            lE.setVisible(true);
                        } else {
                            sendLine("Registrate please");
                            sendLine(tfF.getText());
                            sendLine(tfJ.getText());
                        }
                    } catch (IOException ex) {
                        System.out.println("Nem tudta elküldeni az üzenetet");
                    }
                    try {
                        return expect("Welcome");
                    } catch (IOException ex) {
                        System.out.println("Nem érkezett meg megfelelően a szerver válasza");
                    }
                }
                return false;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                if (this.getValue()) {
                    lE.setText("Belépve");
                    try {
                        new ClientGUI(clientSocket, serverInput, serverOutput).start(stage);
                    } catch (IOException ex) {
                        System.out.println("Nem indult el a GUI");
                    }
                } else {
                    tfF.setText("");
                    tfJ.setText("");
                    lE.setVisible(true);
                }
            }
        }).start());
        Scene scene = new Scene(root, 300, 150);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    protected void sendLine(String line) throws IOException {
        serverOutput.print(line + "\r\n");
        serverOutput.flush();
        System.out.println("Client says: " + line);
    }

    protected boolean expect(String line) throws IOException {
        String ServerLine = serverInput.readLine();
        System.out.println("Server says: " + ServerLine);
        return ServerLine.equals(line);
    }
}