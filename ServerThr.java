package com.example.kalgr_projekt_todo;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;

public class ServerThr extends Thread {
    private Socket clientSocket;
    private BufferedReader clientReader;
    private PrintWriter clientWriter;
    private static int ID = 0;
    private String ownusername;
    private int id;
    private ArrayList<String> onlineusernames;
    private ArrayList<String> everyUser;
    private ArrayList<Group> allGroupes;
    private ArrayList<OneTask> allTasks;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ServerThr(Socket clientSocket, ArrayList<String> onlineusers, ArrayList<String> everyUser,ArrayList<Group> allGroupes,ArrayList<OneTask> allTasks) throws IOException {
        this.clientSocket = clientSocket;
        this.clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.clientWriter = new PrintWriter(clientSocket.getOutputStream());
        this.id = ServerThr.ID++;
        this.onlineusernames = onlineusers;
        this.everyUser = everyUser;
        ownusername="";
        this.allGroupes=allGroupes;
        this.allTasks=allTasks;
    }

    protected void sendLine(String line) {
        try {
            System.out.println(line);
            clientWriter.print(line + "\r\n");
            clientWriter.flush();
        } catch (Exception e) {
            System.out.println("Nem tudta elküldeni a szerver az üzenetet");
        }
    }

    /**
     * fájlba menti az új usert
     */
    public void registrateToFile(String user, String pass) {
        faljbaIrat("Users.txt", user);
        faljbaIrat("Users.txt", pass);
        System.out.println(id + " new user registrated: " + user);
        synchronized (everyUser){
            everyUser.add(user);
        }
    }

    /**
     * True-t ad vissza ha belépett / regisztrált
     * False-t ad vissza ha nem ismerjük ki ez
     *
     * beengedi a usert-welcome fgv-el
     */
    public boolean already_reg(String user, String pass, boolean needreg) throws IOException {
        FileReader fr;
        try {
            fr = new FileReader("Users.txt");
        } catch (FileNotFoundException e) {
            System.out.println("Nem sikerült megnyitni a Users.txt fájlt");
            return false;
        }
        synchronized (onlineusernames){
            for (String onlines: onlineusernames)
                if (onlines.equals(user))
                    return false; //Már belépett máshol
        }

        BufferedReader br = new BufferedReader(fr);
        while (br.ready()) {
            if (br.readLine().equals(user)){
                if (br.readLine().equals(pass)) {
                    //Yes, already registrated
                    br.close();
                    fr.close();
                    welcomeUser(user);
                    return true;
                }
            }else br.readLine();//léptetünk
        }
        br.close();
        fr.close();
        if (needreg){
            registrateToFile(user,pass);
            welcomeUser(user);
            return true;
        }
        return false;
    }

    /**
     * berakja a userta az onlineusers-ba és üdvözli a klienst(beengedi)
     */
    void welcomeUser(String username){
        System.out.println(id + username);
        ownusername=username;
        synchronized (onlineusernames){
            onlineusernames.add(username);
        }
        sendLine("Welcome");
        System.out.println(id + " Welcome user " + username);
    }

    /**
     * regésztráiókor a fájlba írja a datat
     */
    public void faljbaIrat(String txt, String data) {
        try {
            File file = new File(txt);
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(txt, true));
            writer.write(data + System.lineSeparator());
            writer.close();
        } catch (IOException e) {
            System.out.println("Hiba a fájl írása közben");
        }
    }
    protected String getmessage() {
        try {
            if (!clientReader.ready())
                return "";
            String ServerLine = clientReader.readLine();
            System.out.println("Server gets: " + ServerLine); //////////////////////////////////////
            return ServerLine;
        } catch (Exception e) {
            System.out.println("Nem Érkezett meg rendesen a szervertől az üzenet");
        }
        return "";
    }

    /**
     * a teljes futása 2 while ciklus:
     * kilépés után kiveszi a felhaználót az onlineusersből
     */
    public void run() {
        try {
            boolean belepett=false;
            /**
             * nem engdei be amíg nem lett azonsoítva a felhasználó
             */
            while (!belepett) {
                if (!clientSocket.isClosed()) {
                    String input1 = clientReader.readLine();
                    if (input1.equals("Registrate please")) {
                        input1 = clientReader.readLine();
                        String input2 = clientReader.readLine();
                        belepett = already_reg(input1, input2, true);
                    } else {
                        String input2 = clientReader.readLine();
                        belepett = already_reg(input1, input2, false);
                    }
                    if (ownusername.equals(""))
                        sendLine("Dunno");
                }else {
                    System.out.println(id + " Server Thread sudoku: socket let us down");
                    return;
                }
            }
            //Azonosított felhasználó
            /**
             * A teljes while ciklus kezeli a klienst: switch-case ben feldolgozza a bejövő üzenetet, a szerint vlaszol, menti az adatokat, stb
             */
            while (!clientSocket.isClosed()){
                //Felhasznéló kiszolgálása
                String bejovouzenet = clientReader.readLine();
                switch (bejovouzenet){
                    case "Update Me" ->{
                        sendLine("Here ya go:"+ownusername);
                        synchronized (allGroupes) {
                            for (Group next : allGroupes) {
                                for (String usersingroup : next.getMembersName()) {
                                    if (usersingroup.equals(ownusername)) {
                                        sendLine(next.groupToString());
                                    }
                                }
                            }
                        }
                        sendLine("Sending Tasks");
                        synchronized (allTasks) {
                            for (OneTask next : allTasks) {
                                boolean ebbenbennevan = false;
                                if (next.getAutrhorname().equals(ownusername))
                                    ebbenbennevan = true;
                                for (String userintask : next.getEditors()) {
                                    if (userintask.equals(ownusername)) {
                                        ebbenbennevan = true;
                                    }
                                }
                                for (String userintask : next.getWorkers()) {
                                    if (userintask.equals(ownusername)) {
                                        ebbenbennevan = true;
                                    }
                                }
                                if (ebbenbennevan)
                                    sendLine(next.OneTaskToString());
                            }
                        }
                        sendLine("Sending onlineusers");
                        synchronized (onlineusernames){
                            for(String next: onlineusernames)
                                sendLine(next);
                            }
                        sendLine("Sending everyusers");
                        synchronized(everyUser) {
                            for (String next : everyUser)
                                sendLine(next);
                        }
                        //folytatás?
                        sendLine("That's all folks");
                        LocalDateTime now = LocalDateTime.now();
                        sendLine(now.format(formatter));
                    }
                    case "New Task pls!"->{
                        var bejovotask= new String[7];
                        for (int i=0;i<7;i++)
                            bejovotask[i]=clientReader.readLine();
                        var newOneTask= new OneTask(bejovotask[0],bejovotask[1],
                                LocalDateTime.now(), LocalDateTime.parse(bejovotask[2],formatter),
                                OneTask.Status.valueOf(bejovotask[3]),
                                ownusername);
                        String[] chunk = bejovotask[4].split("  ");
                        for (String next : chunk){
                            next=next.replace(" ", "");
                            newOneTask.addGroup(next);
                        }
                        chunk = bejovotask[5].split("  ");
                        for (String next : chunk){
                            next=next.replace(" ", "");
                            newOneTask.addEditor(next);
                        }
                        chunk = bejovotask[6].split("  ");
                        for (String next : chunk){
                            next=next.replace(" ", "");
                            newOneTask.addWorker(next);
                        }
                        sendLine("Yupp, u good");
                        synchronized (allTasks){
                            allTasks.add(newOneTask);
                        }

                    }
                    case "Rewrite Task pls!"->{
                        OneTask selected=null;
                            while (true){
                                bejovouzenet=clientReader.readLine();
                                if (bejovouzenet.equals("That's all"))
                                    break;
                                switch (bejovouzenet){
                                    case "Thisone:"->{
                                        String title=clientReader.readLine();
                                        synchronized (allTasks){
                                            for (OneTask next:allTasks)
                                                if (next.getTitle().equals(title))
                                                    selected=next;
                                        }
                                    }
                                    case "Description:"->{
                                        if (selected!=null){
                                            String desc=clientReader.readLine();
                                            selected.setDescription(desc);
                                        }
                                    }
                                    case "Deadline:"->{
                                        if (selected!=null){
                                            String dedl=clientReader.readLine();
                                            selected.setDeadline(LocalDateTime.parse(dedl));
                                        }
                                    }
                                    case "Status:"->{
                                        if (selected!=null){
                                            String stat=clientReader.readLine();
                                            selected.setStatus(OneTask.Status.valueOf(stat));
                                        }
                                    }
                                    case "Group:","Editors:","Workers:"->{
                                        if (selected!=null){
                                            String thisone=clientReader.readLine();
                                            boolean todelete=false;
                                            switch (bejovouzenet){
                                                case "Group:"->{
                                                        for (String next : selected.getGroups())
                                                            if (next.equals(thisone)) {
                                                                todelete = true;
                                                                break;
                                                            }
                                                        if(todelete)
                                                            selected.removeGroup(thisone);
                                                        else
                                                            selected.addGroup(thisone);
                                                }
                                                case "Editors:"->{
                                                        for (String next : selected.getEditors())
                                                            if (next.equals(thisone)) {
                                                                todelete = true;
                                                                break;
                                                            }
                                                        if(todelete)
                                                            selected.removeEditor(thisone);
                                                        else
                                                            selected.addEditor(thisone);
                                                }
                                                case "Workers:"->{
                                                        for (String next : selected.getWorkers())
                                                            if (next.equals(thisone)) {
                                                                todelete = true;
                                                                break;
                                                            }
                                                        if(todelete)
                                                            selected.removeWorker(thisone);
                                                        else
                                                            selected.addWorker(thisone);
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                            sendLine("Yupp, u good");
                    }
                    case "Wanna answer"->{
                        String whattodo=clientReader.readLine();
                        switch (whattodo){
                            case "Delete this Group:"->{
                                String delGroup=clientReader.readLine();
                                synchronized (allGroupes){
                                    Iterator<Group> iterator = allGroupes.iterator();
                                    while (iterator.hasNext()){
                                        Group next=iterator.next();
                                        if(next.getName().equals(delGroup) &&
                                                next.getUsersPermission(ownusername).equals(Group.Permission.OWNER))
                                            iterator.remove();
                                    }
                                }
                            }
                            case "Delete this Task:"->{
                                String delTask=clientReader.readLine();
                                synchronized (allTasks){
                                    Iterator<OneTask> iterator = allTasks.iterator();
                                    while (iterator.hasNext()) {
                                        OneTask next = iterator.next();
                                        if (next.getTitle().equals(delTask) && next.getAutrhorname().equals(ownusername))
                                            iterator.remove();
                                    }
                                }
                            }
                            //next?
                            default -> System.out.println("bejövő üzenet nem felismerhető: " + bejovouzenet);
                        }

                    }
                    case "New Group pls!"->{
                        Group newgroup = new Group(clientReader.readLine());
                        newgroup.addMember(ownusername, Group.Permission.OWNER);
                        bejovouzenet=clientReader.readLine();
                        while (!bejovouzenet.equals("That's all")){
                            String[] chunk=bejovouzenet.split(" : ");
                            if (chunk.length==2)
                                newgroup.addMember(chunk[0], Group.Permission.valueOf(chunk[1]));
                            bejovouzenet=clientReader.readLine();
                        }
                        sendLine("Yupp, u good");
                        synchronized (allGroupes){
                            allGroupes.add(newgroup);
                        }
                    }
                    case "Check this group:"->{
                        boolean itsokay=true;
                        String checkit=clientReader.readLine();
                        synchronized (allGroupes){
                            for (Group next: allGroupes)
                                if(next.getName().equals(checkit))
                                    itsokay=false;
                        }
                        if(itsokay)
                            sendLine("Good");
                        else
                            sendLine("YeahNah");
                    }
                    case "Check this task:"->{
                        boolean itsokay=true;
                        String checkit=clientReader.readLine();
                        synchronized (allTasks){
                            for (OneTask next: allTasks)
                                if(next.getTitle().equals(checkit))
                                    itsokay=false;
                        }
                        if(itsokay)
                            sendLine("Good");
                        else
                            sendLine("YeahNah");
                    }
                    default -> System.out.println("bejövő üzenet nem felismerhető: " + bejovouzenet);
                }
            }
            synchronized (onlineusernames){
                onlineusernames.remove(ownusername);
            }
        } catch (IOException e) {
            if (ownusername!=null)
                synchronized (onlineusernames){
                    onlineusernames.remove(ownusername);
                }
            System.out.println(id + " Cliensocket committed sudoku");
            return;
        }


        System.out.println(id + " Server Thread sudoku");
    }

}
