package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.bson.types.ObjectId;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
public class Member {
    public final static String COLLECTION_NAME = "members";
    private ObjectId id;
    private String name;
    private String biography;
    private String email;
    // N:M
    private Set<ObjectId> projects;
    //1:1
    private ObjectId assignedTask;



}
