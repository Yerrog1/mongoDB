package org.example.entity;

public class Task {
    private final static String COLLECTION_NAME = "tasks";
    private String id;
    private String name;
    private String description;
    private Member owner;
    private Project project;

    public Task() {
    }


    public String getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public Task setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Task setDescription(String description) {
        this.description = description;
        return this;
    }

    public Member getOwner() {
        return owner;
    }

    public Task setOwner(Member owner) {
        this.owner = owner;
        return this;
    }

    public Project getProject() {
        return project;
    }

    public Task setProject(Project project) {
        this.project = project;
        return this;
    }

    public static String getCollectionName() {
        return COLLECTION_NAME;
    }
}
