package org.example.entity;

import java.util.List;

public class Project {
    private final static String COLLECTION_NAME = "projects";
    private String id;
    private String name;
    private String description;
    //N:M
    private List<Member> members;
    //1:N
    private List<Task> tasks;
    //1:N
    private Member owner;

    public Project() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Project setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Project setDescription(String description) {
        this.description = description;
        return this;
    }

    public List<Member> getMembers() {
        return members;
    }

    public Project setMembers(List<Member> members) {
        this.members = members;
        return this;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public Project setTasks(List<Task> tasks) {
        this.tasks = tasks;
        return this;
    }

    public Member getOwner() {
        return owner;
    }

    public Project setOwner(Member owner) {
        this.owner = owner;
        return this;
    }
    public static String getCollectionName() {
        return COLLECTION_NAME;
    }
}
