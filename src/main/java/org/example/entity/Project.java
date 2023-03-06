package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.bson.types.ObjectId;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
public class Project {
    public final static String COLLECTION_NAME = "projects";
    private ObjectId id;
    private String name;
    private String description;
    //N:M
    private Set<ObjectId> members;
    //1:N
    private Set<ObjectId> tasks;
    //1:N
    private ObjectId owner;


}
