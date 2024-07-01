package com.example.kalgr_projekt_todo;

import java.util.ArrayList;
import java.util.List;


public class Group {
    public enum Permission {
    OWNER, ADMIN, MEMBER, DISMISSED
    }

    private String name;
    private List<String> membersName;
    private List<Permission> permissions;

    public Group(String name) {
        this.name = name;
        this.membersName = new ArrayList<>();
        this.permissions = new ArrayList<>();
    }
    public Group() {
        this.membersName = new ArrayList<>();
        this.permissions = new ArrayList<>();
    }
    public List<String> getMembersName() {
        return membersName;
    }
    public String getName() {
        return name;
    }
    public Permission getUsersPermission(String username){
        int index = membersName.indexOf(username);
        if (index == -1) {
            System.out.println("User not found in group");
            return null;
        }
        return permissions.get(index);
    }

    /**
     * egyszerre adja hozzá a usernevet ls a permissionját
     * ha benne van, átírja a permissionját
     */
    public void addMember(String newmembername, Permission permission) {
        int bennevane=-1;
        for(int i=0;i<membersName.size();i++)
            if(membersName.get(i).equals(newmembername))
                bennevane=i;
        if(bennevane!=-1)
            permissions.add(bennevane,permission);
        else {
            membersName.add(newmembername);
            permissions.add(permission);
        }
    }

    /**
     * a group adatai stringbe rendezi, hogy szerveren át tuja üldeni
     */
    public String groupToString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name+"\n");
        sb.append(membersName.size()+"\n");
        for (String member : membersName) {
            sb.append(member+";");
        }
        sb.append("\n");
        for (Permission permission : permissions) {
            sb.append(permission.name()+";");
        }
        return sb.toString();
    }

    /**
     * a felnti formázott stringet képes magébatölteni
     */
    public void read(String data) {
        String[] chunk = data.split("\n");
        name=chunk[0];
        int numItems = Integer.parseInt(chunk[1]);
        String[] memparts = chunk[2].split(";");
        String[] perparts = chunk[3].split(";");
        if (numItems!= memparts.length || numItems != perparts.length)
            throw new IllegalArgumentException("Number of items in lines 1, 2 and 3 do not match");
        for (int i = 0; i < numItems; i++) {
            this.addMember(memparts[i],Permission.valueOf(perparts[i]));
        }
    }

    /**
     * rövidebb verziója a kiírásnak, a selectedview-ra
     */
    public String toView(){
        StringBuilder sb = new StringBuilder();
        sb.append(name+"\n");
        for (int i=0;i<membersName.size();i++){
            sb.append("\t"+membersName.get(i)+" : "+permissions.get(i)+"\n");
        }
       return sb.toString();
    }
}
