package org.example.generator;

import org.bson.types.ObjectId;
import org.example.entity.Member;
import org.example.entity.Project;
import org.example.entity.Task;

import java.util.HashSet;
import java.util.stream.Collectors;

public class ProjectGenerator extends Generator<Project> {
    @Override
    public Project generate() {
        return new Project()
                .withId(ObjectId.get())
                .withName(faker.app().name())
                .withDescription(faker.lorem().paragraph())
                .withTasks(new HashSet<>())
                .withMembers(new HashSet<>());
    }
}
