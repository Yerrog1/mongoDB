package org.example;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.example.entity.Member;
import org.example.entity.Project;
import org.example.entity.Task;
import org.example.generator.MemberGenerator;
import org.example.generator.ProjectGenerator;
import org.example.generator.TaskGenerator;

import javax.xml.crypto.Data;
import java.util.*;

public class Main {
    private static final MongoDatabase db = DatabaseSingleton.getInstance();
    private static final MemberGenerator memberGenerator = new MemberGenerator();
    private static final ProjectGenerator projectGenerator = new ProjectGenerator();
    private static final TaskGenerator taskGenerator = new TaskGenerator();

    public static void main(String[] args) {
        createCollections();
    }

    public static void createCollections() {
        db.drop();
        var memberCollection = db.getCollection(Member.COLLECTION_NAME, Member.class);
        var projectCollection = db.getCollection(Project.COLLECTION_NAME, Project.class);
        var taskCollection = db.getCollection(Task.COLLECTION_NAME, Task.class);

        var members = memberGenerator.generate(100);
        var projects = projectGenerator.generate(100);
        var tasks = new ArrayList<Task>();

        List<Member> finalMembers = members;


        List<Project> finalProjects = projects;
        members = members.stream().map(member -> {
            var a1 = (int) (Math.random() * 100);
            var a2 = (int) (Math.random() * 100);
            var a3 = (int) (Math.random() * 100);

            Set<ObjectId> projectSet = new HashSet<>();
            projectSet.add(finalProjects.get(a1).getId());
            projectSet.add(finalProjects.get(a2).getId());
            projectSet.add(finalProjects.get(a3).getId());

            finalProjects.get(a1).getMembers().add(member.getId());
            finalProjects.get(a2).getMembers().add(member.getId());
            finalProjects.get(a3).getMembers().add(member.getId());

            return member.withProjects(projectSet);
        }).toList();

        projects = finalProjects;
        projects = projects.stream().map(project -> {
            var projectTasks = taskGenerator.generate(5);
            var taskSet = new HashSet<>(
                    projectTasks.stream()
                            .map(task -> {
                                task = task.withProject(project.getId());
                                if (project.getMembers().size() != 0) {
                                    var random = (int) (Math.random() * project.getMembers().size());
                                    task = task.withOwner((ObjectId) project.getMembers().toArray()[random]);
                                } else {
                                    var random = (int) (Math.random() * finalMembers.size());
                                    project.getMembers().add(finalMembers.get(random).getId());
                                    finalMembers.get(random).getProjects().add(project.getId());
                                }
                                tasks.add(task);
                                return task.getId();
                            })
                            .toList()
            );

            return project.withTasks(taskSet);
        }).toList();

        List<Project> projects2 = projects;
        members = members.stream().map(member -> {
            if (member.getProjects().size() != 0) {
                var random = (int) (Math.random() * member.getProjects().size());
                Member projectMember = member;
                var project = projects2.stream().filter(p -> p.getId().equals((ObjectId) projectMember.getProjects().toArray()[random])).findFirst().get();
                if (project.getTasks().size() != 0) {
                    var random2 = (int) (Math.random() * project.getTasks().size());
                    var task = tasks.stream().filter(t -> t.getId().equals((ObjectId) project.getTasks().toArray()[random2])).findFirst().get();
                    member = member.withAssignedTask(task.getId());
                    task = task.withOwner(member.getId());
                }
            }
            return member;
        }).toList();
        List<Member> ownerMember = members;
        projects = projects.stream().map(project -> {
            if (project.getTasks().size() != 0) {
                var random = (int) (Math.random() * project.getMembers().size());
                Project selectedProject = project;
                var member = ownerMember.stream().filter(m -> m.getId().equals((ObjectId) selectedProject.getMembers().toArray()[random])).findFirst().get();
                project = project.withOwner(member.getId());
            }
            return project;
        }).toList();
        memberCollection.insertMany(members);
        projectCollection.insertMany(projects);
        taskCollection.insertMany(tasks);
    }

    public static void consultas() {
        MongoCollection<Member> memberCollection = db.getCollection(Member.COLLECTION_NAME, Member.class);
        MongoCollection<Project> projectCollection = db.getCollection(Project.COLLECTION_NAME, Project.class);
        MongoCollection<Task> taskCollection = db.getCollection(Task.COLLECTION_NAME, Task.class);
        List<Member> members;
        List<Project> projects;
        List<Task> tasks;
        //8 operaciones de consulta empleando filtros y proyecciones.

        //Buscar todos los miembros con el nombre "Alice":
        Bson filter = Filters.eq("name", "Alice");
        members = memberCollection.find(filter).into(new ArrayList<>());
        System.out.println("Miembros con el nombre Alice: " + members);

        //Buscar todos los proyectos propiedad de un miembro con un ID determinado:
        ObjectId memberId = new ObjectId("123456789012345678901234");
        filter = Filters.eq("owner", memberId);
        projects = projectCollection.find(filter).into(new ArrayList<>());
        System.out.println("Proyectos propiedad de un miembro con ID "+memberId+": " + projects);

        //Buscar todas las tareas asignadas a un miembro con un ID determinado:
        memberId = new ObjectId("123456789012345678901234");
        filter = Filters.eq("owner", memberId);
        List<Task> result = taskCollection.find(filter).into(new ArrayList<>());
        System.out.println("Tarea asignada a un miembro con ID "+memberId+": " + result);

        //Buscar todos los miembros que participan en un proyecto con un ID determinado:
        ObjectId projectId = new ObjectId("123456789012345678901234");
        filter = Filters.in("projects", projectId);
        members = memberCollection.find(filter).into(new ArrayList<>());
        System.out.println("Miembros que participan en un proyecto con ID "+projectId+": " + members);

        //Buscar todas las tareas asociadas a un proyecto con un ID determinado, con proyección para incluir solo el nombre de la tarea:
        projectId = new ObjectId("123456789012345678901234");
        filter = Filters.eq("project", projectId);
        Bson projection = Projections.include("name");
        tasks = taskCollection.find(filter).projection(projection).into(new ArrayList<>());
        System.out.println("Tareas asociadas a un proyecto con ID "+projectId+": " + tasks);

        //Buscar todos los proyectos que contienen al menos una tarea, con proyección para incluir solo el nombre del proyecto:
        filter = Filters.exists("tasks");
        projection = Projections.include("name");
        projects = projectCollection.find(filter).projection(projection).into(new ArrayList<>());
        System.out.println("Proyectos que contienen al menos una tarea: " + projects);

        //Buscar todos los miembros que tienen una tarea asignada:
        filter = Filters.exists("assignedTask");
        members = memberCollection.find(filter).into(new ArrayList<>());
        System.out.println("Miembros que tienen una tarea asignada: " + members);

        //Buscar todos los miembros con una biografía que contiene la palabra "programador":
        filter = Filters.regex("biography", "programador");
        members = memberCollection.find(filter).into(new ArrayList<>());
        System.out.println("Miembros con una biografía que contiene la palabra \"programador\": " + members);

        //6 operaciones de actualización.







        //4 operaciones de agregaciones pipeline de las cuales al menos 3 deben realizar operaciones de agrupamiento empleando funciones de agregado.





        //Exportar cada una de las colecciones a un fichero en formato .json.
    }
}