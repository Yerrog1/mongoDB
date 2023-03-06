package org.example;

import com.mongodb.client.MongoDatabase;
import org.example.entity.Member;
import org.example.entity.Project;
import org.example.entity.Task;

import javax.xml.crypto.Data;

public class Main {
    private static Member member = new Member();
    private static Project project = new Project();
    private static Task task = new Task();
    private static final MongoDatabase db = DatabaseSingleton.getInstance();
    public static void main(String[] args) {

    }
    public static void createCollections(){
        db.getCollection(Member.getCollectionName(),Member.class);

        db.getCollection(Project.getCollectionName(),Project.class);
        db.getCollection(Task.getCollectionName(),Task.class);

    }
}