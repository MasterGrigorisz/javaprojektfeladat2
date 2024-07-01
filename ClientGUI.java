package com.example.kalgr_projekt_todo;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ClientGUI extends Stage {
    private Socket clientSocket;
    private BufferedReader serverInput;
    private PrintWriter serverOutput;
    private String username;
    private AnchorPane ablak;
    private ArrayList<String> onlineusernames;
    private ArrayList<String> everyusernames;
    private ArrayList<Group> allGroupes; //adott felhasználó álltal látható összes group
    private ArrayList<OneTask> allTasks; //adott felhasználó alltal látható összes task
    private LocalDateTime updateDate; // legutolsó update érkezése a szerverről
    private Label usernamelabel;
    private Label lastuplabel;
    private Label lSE = new Label("A szerver nem elérhető");
    private ListView<String> TasksView;
    private ListView<String> GroupsView;
    private ListView<String> InvitesView;
    private ListView<String> selectedShower;// alsó nagy view, erre kerülnek ki a megnyitott dolgok
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final String groupblank = "Title:\nDescription:\nDeadline:\nStatus:\nGroups on Task:\nEditors:\nWorkers:\n";
    private String[] TaskKitoltes; // új task kitöltéséhez kell
    private String[] Groupkitoltes;// új group kitöltéséhez
    private ArrayList<String> TaskAtiras; // task átírásához kell
    private boolean Taskotkeszitunk = false;
    private boolean taskotBabralunk = false;
    private boolean Groupotkeszitunk = false;
    private boolean groupotbabralunk = false;
    private TextField newstuffbar1 = new TextField();// cím
    private TextField newstuffbar2 = new TextField();// leírás
    private DatePicker datePicker = new DatePicker();
    private ComboBox<OneTask.Status> statusbox;
    private ComboBox<Group.Permission> permissionbox;
    private ComboBox<String> groupbox;
    private ComboBox<String> editorsbox;
    private ComboBox<String> workersbox;
    private OneTask selectedout = null; // melyik task van kiválasztva
    private String selectedGroup;
    private String selectedTask;
    private String selectedInv;
    private int CHANGE = 0;// jogosultsági szintekhez van kiosztva

    public ClientGUI(Socket clientSocket, BufferedReader serverInput, PrintWriter serverOutput) {
        this.clientSocket = clientSocket;
        this.serverInput = serverInput;
        this.serverOutput = serverOutput;
        onlineusernames = new ArrayList<>();
        allGroupes = new ArrayList<>();
        allTasks = new ArrayList<>();
        everyusernames = new ArrayList<>();
    }

    public void start(Stage stage) throws IOException {
        ////////////////////////GUI
        stage.setTitle("TODO menü");
        ablak = new AnchorPane();

        // Textfield és gomb létrehozása a felső részhez
        TextField searchbar = new TextField();
        Button searchButton = new Button("Search");
        searchbar.setPrefSize(400d, 30d);
        searchButton.setPrefSize(80d, 30d);
        AnchorPane.setTopAnchor(searchbar, 10d);
        AnchorPane.setTopAnchor(searchButton, 10d);
        AnchorPane.setLeftAnchor(searchbar, 140d);
        AnchorPane.setLeftAnchor(searchButton, 550d);

        // ListView
        TasksView = new ListView<>();
        GroupsView = new ListView<>();
        InvitesView = new ListView<>();
        selectedShower = new ListView<>();
        Label Tasklabel = new Label("Tasks:");
        Label Grouplabel = new Label("Groups:");
        Label Openedlabel = new Label("Opened:");
        TasksView.setPrefSize(180d, 230d);
        GroupsView.setPrefSize(180d, 230d);
        InvitesView.setPrefSize(120d, 140d);
        selectedShower.setPrefSize(500d, 300d);
        AnchorPane.setTopAnchor(TasksView, 70d);
        AnchorPane.setTopAnchor(GroupsView, 70d);
        AnchorPane.setTopAnchor(InvitesView, 110d);
        AnchorPane.setTopAnchor(selectedShower, 310d);
        AnchorPane.setTopAnchor(Tasklabel, 50d);
        AnchorPane.setTopAnchor(Grouplabel, 50d);
        AnchorPane.setTopAnchor(Openedlabel, 290d);
        AnchorPane.setTopAnchor(lSE, 5d);
        AnchorPane.setLeftAnchor(TasksView, 330d);
        AnchorPane.setLeftAnchor(GroupsView, 140d);
        AnchorPane.setLeftAnchor(InvitesView, 520d);
        AnchorPane.setLeftAnchor(selectedShower, 10d);
        AnchorPane.setLeftAnchor(Tasklabel, 330d);
        AnchorPane.setLeftAnchor(Grouplabel, 140d);
        AnchorPane.setLeftAnchor(Openedlabel, 10d);
        AnchorPane.setLeftAnchor(lSE, 10d);

        // RadioButton-ok és a hozzájuk tartozó gomb létrehozása
        ToggleGroup toggleGroup = new ToggleGroup();
        RadioButton radioButtonYes = new RadioButton("Yes");
        RadioButton radioButtonNo = new RadioButton("No");
        radioButtonYes.setToggleGroup(toggleGroup);
        radioButtonNo.setToggleGroup(toggleGroup);
        Button answerButton = new Button("Answer");
        AnchorPane.setTopAnchor(radioButtonYes, 260d);
        AnchorPane.setTopAnchor(radioButtonNo, 280d);
        AnchorPane.setTopAnchor(answerButton, 265d);
        AnchorPane.setLeftAnchor(radioButtonYes, 530d);
        AnchorPane.setLeftAnchor(radioButtonNo, 530d);
        AnchorPane.setLeftAnchor(answerButton, 580d);

        // Többi label és gomb
        usernamelabel = new Label("Username");
        lastuplabel = new Label("Last up");
        Button updateButton = new Button("Update me!");
        Button GroupButton = new Button("Open Group");
        Button TaskButton = new Button("Open Task");
        Button NewGroupButton = new Button("Make new Group");
        Button NewTaskButton = new Button("Make new Task");
        usernamelabel.setPrefSize(100d, 30d);
        lastuplabel.setPrefSize(100d, 30d);
        updateButton.setPrefSize(100d, 30d);
        GroupButton.setPrefSize(100d, 30d);
        TaskButton.setPrefSize(100d, 30d);
        NewGroupButton.setPrefSize(100d, 30d);
        NewTaskButton.setPrefSize(100d, 30d);
        AnchorPane.setTopAnchor(usernamelabel, 10d);
        AnchorPane.setTopAnchor(lastuplabel, 45d);
        AnchorPane.setTopAnchor(updateButton, 90d);
        AnchorPane.setTopAnchor(GroupButton, 130d);
        AnchorPane.setTopAnchor(TaskButton, 170d);
        AnchorPane.setTopAnchor(NewGroupButton, 210d);
        AnchorPane.setTopAnchor(NewTaskButton, 250d);

        AnchorPane.setLeftAnchor(usernamelabel, 10d);
        AnchorPane.setLeftAnchor(lastuplabel, 10d);
        AnchorPane.setLeftAnchor(updateButton, 10d);
        AnchorPane.setLeftAnchor(GroupButton, 10d);
        AnchorPane.setLeftAnchor(TaskButton, 10d);
        AnchorPane.setLeftAnchor(NewGroupButton, 10d);
        AnchorPane.setLeftAnchor(NewTaskButton, 10d);

        // Opened + new stuff
        Label Helperlabel = new Label(
                "Steps of making:\n1. Select what to change\n2. Write the new one\n3. Confirm* than Make!");
        Button ConfirmButton = new Button("Confirm");
        Button CancelButton = new Button("Cancel");
        Button MakeButton = new Button("Make!");
        Button DeleteButton = new Button("Delete");
        newstuffbar1 = new TextField();
        newstuffbar2 = new TextField();
        datePicker = new DatePicker();
        statusbox = new ComboBox<>();
        permissionbox = new ComboBox<>();
        groupbox = new ComboBox<>();
        editorsbox = new ComboBox<>();
        workersbox = new ComboBox<>();
        statusbox.getItems().setAll(OneTask.Status.values());
        permissionbox.getItems().setAll(Group.Permission.MEMBER, Group.Permission.ADMIN, Group.Permission.DISMISSED);
        setAllInvisible();
        ConfirmButton.setDisable(true);
        CancelButton.setDisable(true);
        MakeButton.setDisable(true);
        DeleteButton.setDisable(true);
        answerButton.setDisable(true);

        ConfirmButton.setPrefSize(120d, 30d);
        CancelButton.setPrefSize(120d, 30d);
        MakeButton.setPrefSize(120d, 30d);
        DeleteButton.setPrefSize(120d, 30d);
        newstuffbar1.setPrefSize(120d, 30d);
        newstuffbar2.setPrefSize(120d, 30d);
        datePicker.setPrefSize(120d, 30d);
        statusbox.setPrefSize(120d, 30d);
        permissionbox.setPrefSize(120d, 30d);
        groupbox.setPrefSize(120d, 30d);
        editorsbox.setPrefSize(120d, 30d);
        workersbox.setPrefSize(120d, 30d);
        AnchorPane.setTopAnchor(Helperlabel, 305d);
        AnchorPane.setTopAnchor(MakeButton, 390d);
        AnchorPane.setTopAnchor(ConfirmButton, 430d);
        AnchorPane.setTopAnchor(CancelButton, 580d);
        AnchorPane.setTopAnchor(DeleteButton, 70d);
        AnchorPane.setTopAnchor(newstuffbar1, 470d);
        AnchorPane.setTopAnchor(newstuffbar2, 470d);
        AnchorPane.setTopAnchor(datePicker, 470d);
        AnchorPane.setTopAnchor(statusbox, 470d);
        AnchorPane.setTopAnchor(permissionbox, 510d);
        AnchorPane.setTopAnchor(groupbox, 470d);
        AnchorPane.setTopAnchor(editorsbox, 470d);
        AnchorPane.setTopAnchor(workersbox, 470d);

        AnchorPane.setLeftAnchor(ConfirmButton, 520d);
        AnchorPane.setLeftAnchor(CancelButton, 520d);
        AnchorPane.setLeftAnchor(Helperlabel, 515d);
        AnchorPane.setLeftAnchor(MakeButton, 520d);
        AnchorPane.setLeftAnchor(DeleteButton, 520d);
        AnchorPane.setLeftAnchor(newstuffbar1, 520d);
        AnchorPane.setLeftAnchor(newstuffbar2, 520d);
        AnchorPane.setLeftAnchor(datePicker, 520d);
        AnchorPane.setLeftAnchor(statusbox, 520d);
        AnchorPane.setLeftAnchor(permissionbox, 520d);
        AnchorPane.setLeftAnchor(groupbox, 520d);
        AnchorPane.setLeftAnchor(editorsbox, 520d);
        AnchorPane.setLeftAnchor(workersbox, 520d);

        updatme();// rögtön kérünk frissítést
        ablak.getChildren().addAll(searchbar, searchButton, TasksView, GroupsView, InvitesView, selectedShower,
                radioButtonYes, radioButtonNo, answerButton, usernamelabel, lastuplabel, updateButton, GroupButton,
                TaskButton, NewGroupButton, NewTaskButton,DeleteButton, Grouplabel, Tasklabel, Openedlabel, Helperlabel,
                ConfirmButton, CancelButton, MakeButton, newstuffbar1, newstuffbar2, datePicker, statusbox, groupbox,
                editorsbox, workersbox,permissionbox,lSE);
        stage.setScene(new Scene(ablak, 650, 620));
        stage.sizeToScene();
        stage.show();
        ////////////////////////GUI
        ////////////////////////BUTTONS
        //////////////SEARCH
        /**
         * a searchbarból kiveszi a nem üres keresett szót
         * a task és group view-n keres a címben, azokat listázza ki
         */
        searchButton.setOnAction(e -> {
            String googledszo = searchbar.getText();
            if (googledszo.isEmpty())
                return;

            ObservableList<String> items = FXCollections.observableArrayList();
            synchronized (allGroupes) {
                for (Group next : allGroupes)
                    if (next.getName().contains(googledszo))
                        items.add(next.getName());
            }
            GroupsView.setItems(items);

            items = FXCollections.observableArrayList();
            synchronized (allTasks) {
                for (OneTask next : allTasks)
                    if (next.getTitle().contains(googledszo))
                        items.add(next.getTitle());
            }
            TasksView.setItems(items);
        });
        //////////////SHOW GROUP
        /**
         * a kiválasztott group tartalmát kiírja a showview-ra
         * ha tulaj, lehet törölni
         */
        GroupButton.setOnAction(e -> {
            setAllInvisible();
            setAlltoZero();
            ConfirmButton.setDisable(true);
            MakeButton.setDisable(true);
            taskotBabralunk = false;
            groupotbabralunk=true;
            Groupotkeszitunk=false;
            Taskotkeszitunk = false;
            if (selectedGroup != null) {
                synchronized (allGroupes) {
                    for (Group next : allGroupes) {
                        if (next.getName().equals(selectedGroup)) {
                            ObservableList<String> items = FXCollections.observableArrayList();
                            String[] chunk = next.toView().split("\n");
                            items.addAll(Arrays.asList(chunk));
                            selectedShower.setItems(items);
                            if (next.getUsersPermission(username).equals(Group.Permission.OWNER))
                                DeleteButton.setDisable(false);
                        }
                    }
                }
            }
        });
        //////////////SHOW TASK
        /**
         * a kiválasztott task tartalmát kiírja showview-ra
         * ha tulaj, törlést megengedi
         * előkészíti a módosításhoz valókat:
         *      gombok, engedélyező boolok
         */
        TaskButton.setOnAction(e -> {
            ConfirmButton.setDisable(false);
            MakeButton.setDisable(true);
            CancelButton.setDisable(true);
            taskotBabralunk = true;
            groupotbabralunk=false;
            Groupotkeszitunk=false;
            Taskotkeszitunk = false;
            selectedShower.getItems().clear();
            setAlltoZero();
            setAllInvisible();
            TaskAtiras = new ArrayList<>();
            if (selectedTask != null) {
                TaskAtiras.add("Thisone:\n" + selectedTask);
                newthingshower_Taskbabralas();
                synchronized (selectedout) {
                    if (selectedout != null) {
                        MakeButton.setDisable(false);
                        ConfirmButton.setDisable(false);
                        if (selectedout.getAutrhorname().equals(username))
                            DeleteButton.setDisable(false);
                    }
                }
            }

        });
        //////////////NEW TASK
        /**
         * selectedshow-ra kirak egy üres teskot (cím, lerás, stb)
         * engedélyezi a confirmot, maket, cancel-t
         * change-et engedélyezi maxra
         */
        NewTaskButton.setOnAction(e -> {
            setAllInvisible();
            setAlltoZero();
            MakeButton.setDisable(true);
            ConfirmButton.setDisable(true);
            CancelButton.setDisable(false);
            taskotBabralunk = false;
            groupotbabralunk=false;
            Groupotkeszitunk=false;
            Taskotkeszitunk = true;
            TaskKitoltes = new String[7];
            Arrays.fill(TaskKitoltes, "");
            CHANGE = 3;

            newthingshower_Taskkitoltes();
        });
        //////////////NEW GROUP
        /**
         * groupkészítésnél a change mínuszban van, így külön kezelhető egy változóval a task és group
         * group kevés tényezőt tartalmaz, így betömhetem 4 elembe (groupkitöltés)
         * engedélyezi a gombokat, change-et berakja max engedélyezésbe
         */
        NewGroupButton.setOnAction(e -> {
            setAllInvisible();
            setAlltoZero();
            taskotBabralunk = false;
            groupotbabralunk=false;
            Groupotkeszitunk=true;
            Taskotkeszitunk = false;
            CHANGE=-3;
            MakeButton.setDisable(true);
            ConfirmButton.setDisable(true);
            CancelButton.setDisable(false);
            Groupkitoltes= new String[]{"Name:","", "Members + Permissions:",""};
            newthingshower_Groupkitoltes();
        });
        //////////////CANCEL
        /**
         * mindent resetel
         * elveszi a gombokat, kinullácca a kitölthető elemeket, sowert is törli
         */
        CancelButton.setOnAction(e -> {
            setAllInvisible();
            setAlltoZero();
            ConfirmButton.setDisable(true);
            CancelButton.setDisable(true);
            MakeButton.setDisable(true);
            selectedShower.getItems().clear();
            Taskotkeszitunk=false;
            taskotBabralunk=false;
            Groupotkeszitunk=false;
            groupotbabralunk=false;
        });
        //////////////CONFIRM
        /**
         * bármelyik módosítást amit akar elvégezni a törlésen kívül a felhasználó, ez fogje megcsinálni
         * 1. group vagy task szerkesztés?
         * 2. külön ellenőrzi a beviteli mezőket : 2db bar, datepicker, legördülő menük (status, felhasználók, csoportok)
         * 3. ha az egik visible, akkor azt fogjuk olvasni (visiblet a selevtedView kezeli)
         * 4. belrakjuk az változónkba amit piszkálnk az új adatot
         *
         * címnél lekéri a szerverről, hogy nincs-e már már ilyen
         * ha megvolt az átírás, megnézi, hogy a make gombot megadhatjuk-e
         * selectedview-t frisíti fgv-el
         */
        ConfirmButton.setOnAction(e -> {
            if (Taskotkeszitunk || taskotBabralunk) {
                if (newstuffbar1.isVisible() && !newstuffbar1.getText().isEmpty()) {
                    sendLine("Check this task:");
                    sendLine(newstuffbar1.getText());
                    if (expect("Good")){
                        if (Taskotkeszitunk)
                            TaskKitoltes[0] = newstuffbar1.getText();
                        if (taskotBabralunk)
                            TaskAtiras.add("Title:\n" + newstuffbar1.getText());
                    } else
                        newstuffbar1.setText("");

                }
                if (newstuffbar2.isVisible() && !newstuffbar2.getText().isEmpty()) {
                    if (Taskotkeszitunk)
                        TaskKitoltes[1] = newstuffbar2.getText();
                    if (taskotBabralunk) {
                        selectedout.setDescription(newstuffbar2.getText());
                        TaskAtiras.add("Description:\n" + newstuffbar2.getText());
                    }
                }
                if (datePicker.isVisible() && datePicker.getValue() != null) {
                    String ezt = String.valueOf(datePicker.getValue());
                    if (Taskotkeszitunk)
                        TaskKitoltes[2] = ezt + " 23:59:59";
                    if (taskotBabralunk)
                        TaskAtiras.add("Deadline:\n" + ezt + " 23:59:59");
                }
                if (statusbox.isVisible() && statusbox.getValue() != null) {
                    if (Taskotkeszitunk)
                        TaskKitoltes[3] = statusbox.getValue().name();
                    if (taskotBabralunk)
                        TaskAtiras.add("Status:\n" + statusbox.getValue().name());
                }
                if (groupbox.isVisible() && groupbox.getValue() != null) {
                    String chosen = groupbox.getValue();
                    if (Taskotkeszitunk)
                        if (!TaskKitoltes[4].contains(" " + chosen + " "))
                            TaskKitoltes[4] += " " + chosen + " ";
                    if (taskotBabralunk)
                        TaskAtiras.add("Group:\n"+chosen);

                }
                if (editorsbox.isVisible() && editorsbox.getValue() != null) {
                    String chosen = editorsbox.getValue();
                    if (Taskotkeszitunk)
                        if (!TaskKitoltes[5].contains(" " + chosen + " "))
                            TaskKitoltes[5] += " " + chosen + " ";
                    if (taskotBabralunk)
                        TaskAtiras.add("Editors:\n"+chosen);

                }
                if (workersbox.isVisible() && workersbox.getValue() != null) {
                    String chosen = workersbox.getValue();
                    if (Taskotkeszitunk)
                        if (!TaskKitoltes[6].contains(" " + chosen + " "))
                            TaskKitoltes[6] += " " + chosen + " ";
                    if (taskotBabralunk)
                        TaskAtiras.add("Workers:\n"+chosen);
                }
                boolean ures = false;
                if (TaskKitoltes != null)
                    for (String next : TaskKitoltes) {
                        if (next.equals(""))
                            ures = true;
                    }
                if (!ures && Taskotkeszitunk) {
                    MakeButton.setDisable(false);
                }
                if (Taskotkeszitunk)
                    newthingshower_Taskkitoltes();
                if (taskotBabralunk)
                    newthingshower_Taskbabralas();
            }
           if (Groupotkeszitunk || groupotbabralunk){
               if (newstuffbar1.isVisible() && !newstuffbar1.getText().isEmpty()){
                   sendLine("Check this group:");
                   sendLine(newstuffbar1.getText());
                   if (expect("Good"))
                        Groupkitoltes[1]=newstuffbar1.getText();
                   else
                       newstuffbar1.setText("");
               }
               if (permissionbox.isVisible() && permissionbox.getValue()!=null
                       && workersbox.isVisible() && workersbox.getValue() != null){
                   Groupkitoltes[3]+=workersbox.getValue()+" : "+permissionbox.getValue()+"\n";
               }
               newthingshower_Groupkitoltes();
               if (!Groupkitoltes[1].equals("") && !Groupkitoltes[3].equals("")){
                   MakeButton.setDisable(false);
               }
           }
        });
        //////////////MAKE
        /**
         * ez küldi el a létrehozandó obj-et a szerverre
         * 1.kezdőparancs, hogy mit szerene küldeni
         * 2.kiküldi a felépített stringet
         * 3.bevárja a megerősítér, hgy fogadva lett
         * elveszi a már kitöltött beviteli eszközöket, gombokat
         */
        MakeButton.setOnAction(e -> {
            //elküldjük a új, kész tartalmat a szervernek
            if (Taskotkeszitunk) {
                sendLine("New Task pls!");
                for (String next : TaskKitoltes) {
                    sendLine(next);
                }
                if (expect("Yupp, u good")) {
                    updatme();
                    updateGUI();
                    setAllInvisible();
                }
                else System.out.println("Nem lett megerősítve a szerverről");
            }
            if (taskotBabralunk) {
                sendLine("Rewrite Task pls!");
                //System.out.println("Rewrite:: " + TaskAtiras);
                for (String next : TaskAtiras) {
                    //System.out.println("Rewrite:: " + next);
                    sendLine(next);
                }
                sendLine("That's all");
                if (expect("Yupp, u good")) {
                    updatme();
                    updateGUI();
                    setAllInvisible();
                }
                else System.out.println("Nem lett megerősítve a szerverről");
            }
            if (Groupotkeszitunk){
                sendLine("New Group pls!");
                sendLine(Groupkitoltes[1]);
                sendLine(Groupkitoltes[3]);
                for (String next : Groupkitoltes) {
                    sendLine(next);
                }
                sendLine("That's all");
                if (expect("Yupp, u good")) {
                    updatme();
                    updateGUI();
                    setAllInvisible();
                    setAlltoZero();
                    Helperlabel.setText("Steps of making:\n1. Select what to change\n2. Write the new one\n3. Confirm* than Make!");
                }
                else System.out.println("Nem lett megerősítve a szerverről");
            }
            setAllInvisible();
            ConfirmButton.setDisable(true);
            CancelButton.setDisable(true);
            MakeButton.setDisable(true);
            selectedShower.getItems().clear();
            setAlltoZero();
        });
        //////////////ANSWER
        /**
         * a kivláasztott kérést elküldi a szervernek
         */
        answerButton.setOnAction(e -> {
            RadioButton selectedRadioButton = (RadioButton) toggleGroup.getSelectedToggle();
            String answer = selectedRadioButton.getText();
            if (answer.equals("No")){
                String selectedInv = InvitesView.getSelectionModel().getSelectedItem();
                InvitesView.getItems().remove(selectedInv);
            }else {
                sendLine("Wanna answer");
                sendLine(selectedInv);
                String selectedInv = InvitesView.getSelectionModel().getSelectedItem();
                InvitesView.getItems().remove(selectedInv);

            }
            answerButton.setDisable(true);
            updatme();
        });
        //////////////DELETE
        /**
         * ha a kivláasztott group/task tulaja a felhasználó akkor kirakja a megerősítés kérését a inviteview-ra, ha még nincs kint
         */
        DeleteButton.setOnAction(e -> {
            if(selectedGroup!=null && !InvitesView.getItems().contains("Delete this Group:\n"+selectedGroup))
                InvitesView.getItems().add("Delete this Group:\n"+selectedGroup);
            if(selectedTask!=null && !InvitesView.getItems().contains("Delete this Task:\n"+selectedTask))
                InvitesView.getItems().add("Delete this Task:\n"+selectedTask);
            selectedShower.getItems().clear();
        });
        //////////////UPDATE
        /**
         * TrIVi
         */
        updateButton.setOnAction(e -> {
            updatme();
        });
        ////////////////////////LEFTOVER
        /**
         * Legfontosabb elem
         * switch-case ben engedélyezi/letiltja a beviteli eszközöket
         */
        selectedShower.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> ov, String oldVal, String newVal) -> {
                    if (newVal != null) {
                        Helperlabel.setText("Steps of making:\n1. Select what to change\n2. Write the new one\n3. Confirm* than Make!");
                        ConfirmButton.setDisable(false);
                        switch (newVal) {
                            case "Title:" -> {
                                setAllInvisible();
                                if (CHANGE == 3) {
                                    newstuffbar1.setVisible(true);
                                }
                            }
                            case "Description:" -> {
                                setAllInvisible();
                                if (CHANGE >= 2) {
                                    newstuffbar2.setVisible(true);
                                }
                            }
                            case "Deadline:" -> {
                                setAllInvisible();
                                if (CHANGE >= 2) {
                                    datePicker.setVisible(true);
                                }
                            }
                            case "Status:" -> {
                                setAllInvisible();
                                if (CHANGE >= 1) {
                                    statusbox.setVisible(true);
                                }
                            }
                            case "Groups on Task:" -> {
                                setAllInvisible();
                                if (CHANGE >= 3) {
                                    groupbox.getItems().clear();
                                    ObservableList<String> items = FXCollections.observableArrayList();
                                    synchronized (allGroupes) {
                                        for (Group next : allGroupes)
                                            items.add(next.getName());
                                    }
                                    Helperlabel.setText("Steps of making:\n1. Select what to change\n2. Write the new one\n3. Confirm* than Make!\nDouble add -> Remove");
                                    groupbox.setItems(items);
                                    groupbox.setVisible(true);
                                }
                            }
                            case "Editors:" -> {
                                setAllInvisible();
                                if (CHANGE >= 3) {
                                    editorsbox.getItems().clear();
                                    ObservableList<String> items = FXCollections.observableArrayList();
                                    for (String next : everyusernames)
                                        if (!next.equals(username))
                                            items.add(next);
                                    Helperlabel.setText("Steps of making:\n1. Select what to change\n2. Write the new one\n3. Confirm* than Make!\nDouble add -> Remove");
                                    editorsbox.setItems(items);
                                    editorsbox.setVisible(true);
                                }
                            }
                            case "Workers:" -> {
                                setAllInvisible();
                                if (CHANGE >= 2) {
                                    workersbox.getItems().clear();
                                    ObservableList<String> items = FXCollections.observableArrayList();
                                    for (String next : everyusernames)
                                        if (!next.equals(username))
                                            items.add(next);
                                    Helperlabel.setText("Steps of making:\n1. Select what to change\n2. Write the new one\n3. Confirm* than Make!\nDouble add -> Remove");
                                    workersbox.setItems(items);
                                    workersbox.setVisible(true);
                                }
                            }
                            case "Name:"->{
                                setAllInvisible();
                                if (CHANGE<-2){
                                    newstuffbar1.setVisible(true);
                                    workersbox.setVisible(false);
                                    permissionbox.setVisible(false);
                                }
                            }
                            case "Members + Permissions:"->{
                                if (CHANGE<-2){
                                workersbox.getItems().clear();
                                ObservableList<String> items = FXCollections.observableArrayList();
                                for (String next : everyusernames)
                                    if (!next.equals(username))
                                        items.add(next);
                                workersbox.setItems(items);
                                newstuffbar1.setVisible(false);
                                workersbox.setVisible(true);
                                permissionbox.setVisible(true);
                                Helperlabel.setText("Steps of making:\n1. Select what to change\n2. Write the new one\n3. Confirm* than Make!\n(Last change values)");
                                }
                            }
                            default -> {
                                ConfirmButton.setDisable(true);
                                setAllInvisible();
                            }
                        }
                    }
                });
        GroupsView.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> ov, String oldVal, String newVal) -> {
                    selectedGroup=newVal;
                });
        TasksView.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> ov, String oldVal, String newVal) -> {
                    selectedTask=newVal;
                });
        InvitesView.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> ov, String oldVal, String newVal) -> {
                    selectedInv=newVal;
                    answerButton.setDisable(false);
                });
        ////////////////////////UPDATER
        /**
         * 60 másodperenként kér frissítést a szerverről
         */
        ScheduledService<Void> updater = new ScheduledService<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() {
                        updatme();
                        return null;
                    }
                };
            }
        };
        updater.setPeriod(Duration.seconds(60));
        updater.start();
    }

    /**
     * ez updateli az adatokat a szerverről
     * 1.köszönő üzenet
     * 2.nullázza az adatokat
     * kijon alapján group, task, users
     * 3.iklusban switch-case rendezi az adatokat: kulcsszavak vagy adat
     * 3/1 kulcsaszavak álítják a kijon-t
     * 3/2 adatokból switch-case indul
     * 4.belső switch-caseben kijn alapján tölti az adatokat
     */
    private void updatme() {
        sendLine("Update Me");
        String bejovouzenet = getmessage();
        if (bejovouzenet.startsWith("Here ya go:")) {
            username = bejovouzenet.substring("Here ya go:".length()).trim();
            int kijon = 0;
            synchronized (allGroupes) {
                allGroupes = new ArrayList<>();
            }
            synchronized (allTasks) {
                allTasks = new ArrayList<>();
            }
            onlineusernames = new ArrayList<>();
            everyusernames = new ArrayList<>();
            outerloop:
            while (true) {
                switch (bejovouzenet = getmessage()) {
                    case "Sending Tasks" -> kijon = 1;
                    case "Sending onlineusers" -> kijon = 2;
                    case "Sending everyusers" -> kijon = 3;
                    case "That's all folks" -> {
                        updateDate = LocalDateTime.parse(getmessage(), formatter);
                        updateGUI();
                        break outerloop;
                    }
                    default -> {
                        switch (kijon) {
                            case 0 -> {//group jön
                                //itt rövidebb kódot eredményez beolvasni "kézzel" mint stringbuilderbe rakni és a group/task saját függvényével felötölteni az adatot
                                int dbs = Integer.parseInt(getmessage());
                                Group next = new Group(bejovouzenet);
                                String[] memparts = getmessage().split(";");
                                String[] perparts = getmessage().split(";");
                                for (int i = 0; i < dbs; i++) {
                                    next.addMember(memparts[i], Group.Permission.valueOf(perparts[i]));
                                }
                                synchronized (allGroupes) {
                                    allGroupes.add(next);
                                }
                            }
                            case 1 -> {//task jön
                                // itt is rövidebb a kód ha kézzel rakom be és nem a saját írójával töltöm fel, mert a for ciklusok ígyis-úgyis kellenek a darabszámok miatt, akkor meg már rögtön a taskba töltöm fel és nem stringbuilderbe
                                OneTask next = new OneTask(bejovouzenet, getmessage(),
                                        LocalDateTime.parse(getmessage(), formatter),
                                        LocalDateTime.parse(getmessage(), formatter),
                                        OneTask.Status.valueOf(getmessage()), getmessage());
                                int Count = Integer.parseInt(getmessage());
                                String[] input = getmessage().split(";");
                                for (int i = 0; i < Count; i++) {
                                    next.addGroup(input[i]);
                                }
                                Count = Integer.parseInt(getmessage());
                                input = getmessage().split(";");
                                for (int i = 0; i < Count; i++) {
                                    next.addEditor(input[i]);
                                }
                                Count = Integer.parseInt(getmessage());
                                input = getmessage().split(";");
                                for (int i = 0; i < Count; i++) {
                                    next.addWorker(input[i]);
                                }
                                synchronized (allTasks) {
                                    allTasks.add(next);
                                }
                            }
                            case 2 -> {
                                synchronized (onlineusernames){
                                onlineusernames.add(bejovouzenet); //onlineusers jön //később hasznos lehet
                                }
                            }
                            case 3 -> {
                                synchronized (everyusernames) {
                                    everyusernames.add(bejovouzenet);//everyuser jön
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    /**
     * updateelés után kiírja a frissített adatkat a viewkba
     */
    private void updateGUI() {
        Platform.runLater(() -> {
            usernamelabel.setText("User: " + username);
            DateTimeFormatter formattershort = DateTimeFormatter.ofPattern("HH:mm:ss");
            lastuplabel.setText("Last update:\n" + updateDate.format(formattershort));
            ObservableList<String> items = FXCollections.observableArrayList();
            synchronized (allGroupes) {
                for (Group next : allGroupes)
                    items.add(next.getName());
            }
            GroupsView.setItems(items);
            items = FXCollections.observableArrayList();
            synchronized (allTasks) {
                for (OneTask next : allTasks)
                    items.add(next.getTitle());
            }
            TasksView.setItems(items);
        });
    }
    /**
     * selectedshower kiiratója a több féle newthingshower_
     */
    private void newthingshower_Groupkitoltes(){
        ObservableList<String> items = FXCollections.observableArrayList();
        for (String next: Groupkitoltes)
            items.add(next);
        selectedShower.getItems().clear();
        selectedShower.setItems(items);
    }
    private void newthingshower_Taskkitoltes() {
        ObservableList<String> items = FXCollections.observableArrayList();
        String[] chunk = groupblank.split("\n");
        for (int i = 0; i < chunk.length; i++) {
            items.add(chunk[i]);
            if (TaskKitoltes != null)
                if (!TaskKitoltes[i].equals(""))
                    items.add("\t" + TaskKitoltes[i]);
        }
        selectedShower.getItems().clear();
        selectedShower.setItems(items);
    }
    private void newthingshower_Taskbabralas() {
        selectedShower.getItems().clear();
        String selectedstring = TasksView.getSelectionModel().getSelectedItem();
        synchronized (allTasks) {
            for (OneTask next : allTasks)
                if (next.getTitle().equals(selectedstring))
                    selectedout = next;
        }
        ObservableList<String> items = FXCollections.observableArrayList();
        String[] chunk = selectedout.toView().split("\n");
        Collections.addAll(items, chunk);
        selectedShower.setItems(items);
        CHANGE = 1;//Worker csak 1 dolgot változtathat
        for (String names : selectedout.getEditors()) {
            if (names.equals(username)) {
                CHANGE = 2; //dolgot változtathat a Taskon
            }
        }
        if (selectedout.getAutrhorname().equals(username))
            CHANGE = 3; //Tulaj bármit megtehet
    }
    /**
     * szervernek kiküldi a akapott stringet
     */
    protected void sendLine(String line) {
        try {
            serverOutput.print(line + "\r\n");
            serverOutput.flush();
        } catch (Exception e) {
            System.out.println("Nem tudta elküldeni a client az üzenetet");
        }
    }
    /**
     * boolba visszadja, hogy a szerver az adott üzenetet küldte ki nekünk
     */
    protected boolean expect(String line) {
        if (clientSocket.isClosed()){
            lSE.setVisible(true);
            return false;
        }
        try {
            String ServerLine = serverInput.readLine();
            //System.out.println("Server says: " + ServerLine);
            return ServerLine.equals(line);
        } catch (Exception e) {
            System.out.println("Nem Érkezett meg rendesen a szervertől az üzenet");
        }
        return false;
    }
    /**
     * visszaadja a szervertől kapott üzenetet
     */
    protected String getmessage() {
        try {
            String ServerLine = serverInput.readLine();
            //System.out.println("Server says: " + ServerLine);
            lSE.setVisible(false);
            return ServerLine;
        } catch (Exception e) {
            lSE.setVisible(true);
            System.out.println("Nem Érkezett meg rendesen a szervertől az üzenet");
        }
        return "";
    }
    /**
     * minden beviteli mezőt eltüntet
     */
    private void setAllInvisible() {
        statusbox.setVisible(false);
        permissionbox.setVisible(false);
        newstuffbar1.setVisible(false);
        newstuffbar2.setVisible(false);
        datePicker.setVisible(false);
        groupbox.setVisible(false);
        editorsbox.setVisible(false);
        workersbox.setVisible(false);
    }
    /**
     * minden beviteli mező tartalmát törli
     */
    private void setAlltoZero() {
        Platform.runLater(() -> {
            newstuffbar1.setText("");
            newstuffbar2.setText("");
            datePicker.setValue(null);
            groupbox.getItems().clear();
            editorsbox.getItems().clear();
            workersbox.getItems().clear();
        });
    }
}