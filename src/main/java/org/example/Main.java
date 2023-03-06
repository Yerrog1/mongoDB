package org.example;

import com.mongodb.client.MongoDatabase;
import org.bson.types.ObjectId;
import org.example.entity.Member;
import org.example.entity.Project;
import org.example.entity.Task;
import org.example.generator.MemberGenerator;
import org.example.generator.ProjectGenerator;
import org.example.generator.TaskGenerator;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Main {
    private static final MongoDatabase db = DatabaseSingleton.getInstance();
    private static final MemberGenerator memberGenerator = new MemberGenerator();
    private static final ProjectGenerator projectGenerator = new ProjectGenerator();
    private static final TaskGenerator taskGenerator = new TaskGenerator();

    public static void main(String[] args) {
        createCollections();
    }

    public static void createCollections(){
        db.drop();
        var mc = db.getCollection(Member.COLLECTION_NAME,Member.class);
        var pc = db.getCollection(Project.COLLECTION_NAME,Project.class);
        var tc = db.getCollection(Task.COLLECTION_NAME,Task.class);

        var m = memberGenerator.generate(100);
        var p = projectGenerator.generate(100);
        var t = new ArrayList<Task>();

        p = p.stream().map(project -> {
            var tasksDeEsteProjecto = taskGenerator.generate(5);

            var taskSet = new HashSet<>(
                    tasksDeEsteProjecto.stream()
                            .map(task -> {
                                task = task.withProject(project.getId());
                                t.add(task);
                                return task.getId();
                            })
                            .toList()
            );

            return project.withTasks(taskSet);
        }).toList();

        java.util.List<Project> finalP = p;
        m = m.stream().map(member -> {
            var a1 = (int) (Math.random() * 100);
            var a2 = (int) (Math.random() * 100);
            var a3 = (int) (Math.random() * 100);

            Set<ObjectId> projectSet = new HashSet<>();
            projectSet.add(finalP.get(a1).getId());
            projectSet.add(finalP.get(a2).getId());
            projectSet.add(finalP.get(a3).getId());

            finalP.get(a1).getMembers().add(member.getId());
            finalP.get(a2).getMembers().add(member.getId());
            finalP.get(a3).getMembers().add(member.getId());

            return member.withProjects(projectSet);
        }).toList();

        mc.insertMany(m);
        pc.insertMany(finalP);
        tc.insertMany(t);
    }
}