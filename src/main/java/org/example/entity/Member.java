package org.example.entity;

import java.util.List;

public class Member {
    private final static String COLLECTION_NAME = "members";
    private String id;
    private String name;
    private String biography;
    private String email;
    // N:M
    private List<Project> projects;
    //1:1
    private Task assignedTask;

    public Member() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Member setName(String name) {
        this.name = name;
        return this;
    }

    public String getBiography() {
        return biography;
    }

    public Member setBiography(String biography) {
        this.biography = biography;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Member setEmail(String email) {
        this.email = email;
        return this;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public Member setProjects(List<Project> projects) {
        this.projects = projects;
        return this;
    }

    public Task getAssignedTask() {
        return assignedTask;
    }

    public Member setAssignedTask(Task assignedTask) {
        this.assignedTask = assignedTask;
        return this;
    }
    public static String getCollectionName() {
        return COLLECTION_NAME;
    }
}
