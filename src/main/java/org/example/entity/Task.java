package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
public class Task {
    public final static String COLLECTION_NAME = "tasks";
    private ObjectId id;
    private String name;
    private String description;
    private ObjectId owner;
    private ObjectId project;


}
