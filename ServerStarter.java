package com.example.kalgr_projekt_todo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerStarter implements Runnable {
    public static final int PORT_NUMBER = 42069;
    private ServerSocket serverSocket;
    private ArrayList<String> onlineusernames;
    private ArrayList<String> everyUser;
    private ArrayList<Group> allGroupes;
    private ArrayList<OneTask> allTasks;

    public ServerStarter() throws IOException {
        serverSocket = new ServerSocket(PORT_NUMBER);
        onlineusernames = new ArrayList<>();
        allGroupes=new ArrayList<>();
        allTasks=new ArrayList<>();
        everyUser=new ArrayList<>();
    }

    /**
     * sima fájlolvasó
     */
    private String fajlbololvas(String txt) {
        StringBuilder data= new StringBuilder();
        try {
            FileReader fr;
            fr = new FileReader(txt);
            BufferedReader br = new BufferedReader(fr);
            while (br.ready()) {
                data.append(br.readLine()).append("\n");
            }
            br.close();
            fr.close();
        } catch (FileNotFoundException e) {
            System.out.println("Nem sikerült megnyitni a "+ txt +" fájlt");
        } catch (IOException e) {
            System.out.println("IO hiba olvasásnál");
        }
        return data.toString();
    }

    /**
     * group adatszerkezetet nyitja meg
     */
    private void Groupfajlnyitogato(String txtdata){
        String[] chunk = txtdata.split("\n"); //elsocsoport.txt\nmasodikcsoport.txt\n...
        for (String piece : chunk){
            Group next = new Group(piece.substring(piece.length()-4)); //leveszem róla a ".txt" -t
            next.read(fajlbololvas(piece));
            synchronized (allGroupes) {
                allGroupes.add(next);
            }
        }
    }

    /**
     * task adatszerezetet nyitja meg
     */
    private void Taskfajlnyitogato(String txtdata){
        String[] chunk = txtdata.split("\n"); //elsocsoport.txt\nmasodikcsoport.txt\n...
        for (String piece : chunk){
            OneTask next = new OneTask(piece.substring(piece.length()-4)); //leveszem róla a ".txt" -t
            next.read(fajlbololvas(piece));
            synchronized (allTasks) {
                allTasks.add(next);
            }
        }
    }

    /**
     * csak ez a fgv ír fájlba
     * tt- fájl neve
     * data - beírandó adat
     */
    private void faljbaIrat(String txt, String data) {
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
    /**
     * elvégzi a teljes mentést
     */
    private void HardSave(){
        StringBuilder groupnamestofile= new StringBuilder();
        StringBuilder tasknamestofile= new StringBuilder();
        synchronized (allGroupes){
        for (Group piece:allGroupes){
                groupnamestofile.append(piece.getName());
                faljbaIrat(piece.getName()+".txt",piece.groupToString());
            }
        }
        faljbaIrat("Groupes.txt", groupnamestofile.toString());
        synchronized (allTasks) {
            for (OneTask piece : allTasks) {
                tasknamestofile.append(piece.getTitle());
                faljbaIrat(piece.getTitle() + ".txt", piece.OneTaskToString());
            }
        }
        faljbaIrat("Tasks.txt", tasknamestofile.toString());
    }

    /**
     * beolvassa az összes felhasználót fájlból
     */
    private void Everyusernames(){
        FileReader fr;
        try {
            fr = new FileReader("Users.txt");
            BufferedReader br = new BufferedReader(fr);
            while (br.ready()) {
                everyUser.add(br.readLine());
                br.readLine();
                /*System.out.println(everyUser);
                for (String e : everyUser)
                    System.out.println(e);*/
            }
            br.close();
            fr.close();
        } catch (FileNotFoundException e) {
            System.out.println("Nem sikerült megnyitni a Users.txt fájlt");
        } catch (IOException e) {
            System.out.println("Nem működik a br.ready() a Everyusernames()-ben");
        }


    }

    /**
     * beolvassa az összes fájl, feltöltei az adatokat
     * amint jön egy bejövő kliens, új serverthread-et indít, ami kiszolgálja a klienst
     */
    @Override
    public void run() {
        Groupfajlnyitogato(fajlbololvas("Groupes.txt"));
        Taskfajlnyitogato(fajlbololvas("Tasks.txt"));
        Everyusernames();
        try {
            while (true) {
                HardSave();
                Socket clientSocket = serverSocket.accept();
                try {
                    new ServerThr(clientSocket,onlineusernames,everyUser,allGroupes,allTasks).start();
                    System.out.println("SzervaerThread started");
                } catch (IOException e) {
                    System.err.println("Failed to communicate with client!");
                }

            }
        } catch (IOException e) {
            System.out.println("Accept failed!");
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Server socket did sudoku");
        }
        HardSave();
        System.out.println("BIG Server sudoku");
    }
    public static void main(String[] args) {
        try {
            new Thread(new ServerStarter()).start();
            System.out.println("A Szerver elindult");
        } catch (IOException e) {
            System.out.println("A Szerver nem indult el");
        }
    }
}
