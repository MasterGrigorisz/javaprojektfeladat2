package com.example.kalgr_projekt_todo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OneTask {
    public enum Status {
        DONE, INPROGRESS, NOTSTARTED
    }
    private String title;
    private String description;
    private LocalDateTime creationDate;
    private LocalDateTime deadline;
    private Status status;
    private String authorname;
    private List<String> groups;
    private List<String> editors;
    private List<String> workers;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public OneTask(String title, String description, LocalDateTime creationDate, LocalDateTime deadline,Status status, String authorname) {
        this.title = title;
        this.description = description;
        this.creationDate = creationDate;
        this.deadline = deadline;
        this.status = status;
        this.authorname = authorname;
        this.groups = new ArrayList<>();
        this.editors = new ArrayList<>();
        this.workers = new ArrayList<>();
    }
    public OneTask() {
        this.groups = new ArrayList<>();
        this.editors = new ArrayList<>();
        this.workers = new ArrayList<>();
    }
    public OneTask(String title){
        this.title = title;
    }
    public String getTitle() {
        return title;
    }
    public void addGroup(String group) {
        groups.add(group);
    }
    public void addEditor(String editor) {
        editors.add(editor);
    }
    public void addWorker(String worker) {
        workers.add(worker);
    }
    public String getAutrhorname() {return authorname;}
    public List<String> getGroups() {
        return groups;
    }
    public List<String> getEditors() {
        return editors;
    }
    public List<String> getWorkers() {
        return workers;
    }
    /**
     * ha van ilyen group akkor kiveszi
     */
    public void removeGroup(String groupname) {
        int index = groups.indexOf(groupname);
        if (index == -1) {
            System.out.println("Group not found in the list");
        }else
            groups.remove(groupname);
    }
    public void removeEditor(String name){
        int index = editors.indexOf(name);
        if (index == -1) {
            System.out.println("Editor not found in the list");
        }else
            editors.remove(name);
    }
    public void removeWorker(String name){
        int index = workers.indexOf(name);
        if (index == -1) {
            System.out.println("Worker not found in the list");
        }else
            workers.remove(name);
    }

    /**
     * Stringbe rendezi a teljes Taskot és visszaadja (printre vagy küldeni)
     */
    public String OneTaskToString() {
        StringBuilder sb = new StringBuilder();
        sb.append(title+"\n");
        sb.append(description+"\n");
        sb.append(creationDate.format(formatter)+"\n");
        sb.append(deadline.format(formatter)+"\n");
        sb.append(status.name()+"\n");
        sb.append(authorname+"\n");
        sb.append(groups.size()+"\n");
        for (String group : groups) {
            sb.append(group+";");
        }
        sb.append("\n");
        sb.append(editors.size()+"\n");
        for (String editor : editors) {
            sb.append(editor+";");
        }
        sb.append("\n");
        sb.append(workers.size()+"\n");
        for (String worker : workers) {
            sb.append(worker+";");
        }
        return sb.toString();
    }

    /**
     * a fenti formájú stringet képes betölteni magába
     */
    public void read(String data) {
        String[] lines = data.split("\n");
        title = lines[0];
        description = lines[1];
        creationDate = LocalDateTime.parse(lines[2],formatter);
        deadline = LocalDateTime.parse(lines[3],formatter);
        status = Status.valueOf(lines[4]);
        authorname=lines[5];

        int groupCount = Integer.parseInt(lines[6]);
        groups= new ArrayList<>();
        String[] groupData = lines[7].split(";");
        for (int i = 0; i < groupCount; i++) {
            groups.add(groupData[i]);
        }

        int editorCount = Integer.parseInt(lines[8]);
        editors= new ArrayList<>();
        String[] editorData = lines[9].split(";");
        for (int i = 0; i < editorCount; i++) {
            editors.add(editorData[i]);
        }

        int workerCount = Integer.parseInt(lines[10]);
        workers= new ArrayList<>();
        String[] workerData = lines[11].split(";");
        for (int i = 0; i < workerCount; i++) {
            workers.add(workerData[i]);
        }
    }

    /**
     * rövidített verziója a kiírásnak
     * selectedview-ba való kiíráshoz kell
     */
    public String toView(){
        StringBuilder sb = new StringBuilder();
        sb.append("Title:\n\t"+title+"\n");
        sb.append("Description:\n\t"+description+"\n");
        sb.append("CreationDate:\n\t"+creationDate.format(formatter)+"\n");
        sb.append("Deadline:\n\t"+deadline.format(formatter)+"\n");
        sb.append("Status:\n\t"+status+"\n");
        sb.append("Author:\n\t"+authorname+"\n");
        sb.append("Groups on Task:\n");
        for (int i=0;i<groups.size();i++)
            sb.append("\t"+groups.get(i)+"\n");
        sb.append("Editors:\n");
        for (int i=0;i<editors.size();i++)
            sb.append("\t"+editors.get(i)+"\n");
        sb.append("Workers:\n");
        for (int i=0;i<workers.size();i++)
            sb.append("\t"+workers.get(i)+"\n");
        return sb.toString();
    }
}