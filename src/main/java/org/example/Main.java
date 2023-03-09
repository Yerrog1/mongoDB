package org.example;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.example.entity.Member;
import org.example.entity.Project;
import org.example.entity.Task;
import org.example.generator.MemberGenerator;
import org.example.generator.ProjectGenerator;
import org.example.generator.TaskGenerator;

import javax.xml.crypto.Data;
import java.time.LocalDateTime;
import java.util.*;

import static com.mongodb.client.model.Accumulators.first;
import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.*;

public class Main {
    private static final MongoDatabase db = DatabaseSingleton.getInstance();
    private static final MemberGenerator memberGenerator = new MemberGenerator();
    private static final ProjectGenerator projectGenerator = new ProjectGenerator();
    private static final TaskGenerator taskGenerator = new TaskGenerator();
    private static String idPersona;
    private static String idProyecto;
    private static String idTarea;

    public static void main(String[] args) {
        //createCollections();
        consultas();
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
        members = members.stream().filter(member -> member.getAssignedTask() == null).map(member -> {
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
        ObjectId taskId = new ObjectId("6409906aa821b0309efcce05");
        //6 operaciones de consulta empleando filtros y proyecciones.
        System.out.println("========================================");
        //Buscar todos los miembros con el nombre "Teresia Veum":
        Bson filter = Filters.eq("name", "Teresia Veum");
        members = memberCollection.find(filter).into(new ArrayList<>());
        System.out.println("Miembros con el nombre Teresia Veum  : ");
        for (Member member : members) {
            System.out.println(member.getId() + " " + member.getName());
        }
        System.out.println("========================================");
        //Buscar todos los proyectos propiedad de un miembro con un ID determinado:
        ObjectId memberId = new ObjectId("6409906aa821b0309efccd3d");
        filter = Filters.eq("owner", memberId);
        projects = projectCollection.find(filter).into(new ArrayList<>());
        System.out.println("Proyectos propiedad de un miembro con ID " + memberId + ": ");
        for (Project project : projects) {
            System.out.println(project.getId() + " " + project.getName());
        }
        System.out.println("========================================");

        List<Task> result = taskCollection.find(filter).into(new ArrayList<>());

        //Buscar todos los miembros que participan en un proyecto con un ID determinado:
        ObjectId projectId = new ObjectId("6409906aa821b0309efccda1");
        filter = Filters.in("projects", projectId);
        members = memberCollection.find(filter).into(new ArrayList<>());
        System.out.println("Miembros que participan en un proyecto con ID " + projectId + ": ");
        for (Member member : members) {
            System.out.println(member.getId() + " " + member.getName());
        }
        System.out.println("========================================");
        //Buscar todas las tareas asociadas a un proyecto con un ID determinado:
        projectId = new ObjectId("6409906aa821b0309efccda1");
        filter = Filters.eq("project", projectId);
        Bson projection = Projections.include("name");
        tasks = taskCollection.find(filter).projection(projection).into(new ArrayList<>());
        System.out.println("Tareas asociadas a un proyecto con ID " + projectId + ": ");
        for (Task task : tasks) {
            System.out.println(task.getId() + " " + task.getName());
        }
        System.out.println("========================================");
        //Buscar todos los proyectos que contienen al menos una tarea:
        filter = Filters.exists("tasks");
        projection = Projections.include("name");
        projects = projectCollection.find(filter).projection(projection).into(new ArrayList<>());
        System.out.println("Proyectos que contienen al menos una tarea: ");
        for (Project project : projects) {
            System.out.println(project.getId() + " " + project.getName());
        }
        System.out.println("========================================");
        //Buscar todos los miembros que tienen una tarea asignada:
        filter = Filters.exists("assignedTask");
        members = memberCollection.find(filter).into(new ArrayList<>());
        System.out.println("Miembros que tienen una tarea asignada: ");
        for (Member member : members) {
            System.out.println(member.getId() + " " + member.getName());
        }
        System.out.println("========================================");
        //Buscar todos los miembros con una biografía que contiene la palabra "magni":
        filter = Filters.regex("biography", "magni");
        members = memberCollection.find(filter).into(new ArrayList<>());
        System.out.println("Miembros con una biografía que contiene la palabra \"magni\": ");
        for (Member member : members) {
            System.out.println(member.getId() + " " + member.getName());
        }
        System.out.println("========================================");
        System.out.println("========================================");
        //6 operaciones de actualización.
        //Actualizar el nombre de un proyecto:
        System.out.println("Actualizar el nombre de un proyecto:");
        projectCollection.updateOne(
                Filters.eq("_id", projectId),
                Updates.set("name", "Nuevo nombre del proyecto actualizado " + LocalDateTime.now().toString())
        );
        //Agregar un miembro a un proyecto existente:
        System.out.println("Agregar un miembro a un proyecto existente:");
        projectCollection.updateOne(
                Filters.eq("_id", projectId),
                Updates.addToSet("members", memberId)
        );

        memberCollection.updateOne(
                Filters.eq("_id", memberId),
                Updates.addToSet("projects", projectId)
        );
        //Eliminar una tarea de un proyecto y desasignarla de su propietario:
        System.out.println("Eliminar una tarea de un proyecto y desasignarla de su propietario:");
        taskCollection.updateOne(
                Filters.eq("_id", taskId),
                Updates.set("project", null)
        );


        projectCollection.updateOne(
                Filters.eq("_id", projectId),
                Updates.pull("tasks", taskId)
        );
        filter = Filters.eq("_id", taskId);
        result = taskCollection.find(filter).into(new ArrayList<>());
        Object ownerId = result.get(0).getOwner();
        memberCollection.updateOne(
                Filters.eq("_id", ownerId),
                Updates.set("assignedTask", null)
        );
        //Agregar una tarea a un proyecto:
        System.out.println("Agregar una tarea a un proyecto:");
        Task newTask = new Task(new ObjectId(), "Nombre de la tarea", "Descripción de la tarea creada " + LocalDateTime.now().toString(), null, projectId);

        taskCollection.insertOne(newTask);

        projectCollection.updateOne(
                Filters.eq("_id", projectId),
                Updates.addToSet("tasks", newTask.getId())
        );


        //Agregar un nuevo miembro:
        System.out.println("Agregar un nuevo miembro:");
        Member newMember = new Member(new ObjectId(), "Nuevo miembro", "Biografía del nuevo miembro creado " + LocalDateTime.now().toString(), "correo@nuevomiembro.com", new HashSet<>(), null);

        memberCollection.insertOne(newMember);

        List<Project> memberProjects = projectCollection.find(
                Filters.in("_id", newMember.getProjects())
        ).into(new ArrayList<>());

        //Actualizar la biografía de un miembro:
        System.out.println("Actualizar la biografía de un miembro:");
        memberCollection.updateOne(
                Filters.eq("_id", memberId),
                Updates.set("biography", "Nueva biografía del miembro actualizada el " + LocalDateTime.now().toString())
        );
        //3 operaciones de agregaciones pipeline de las cuales al menos 3 deben realizar operaciones de agrupamiento empleando funciones de agregado.
        System.out.println("========================================");
        System.out.println("========================================");
        //Contar el número de miembros que hay en cada proyecto:
        System.out.println("Obtiene los nombres de los proyectos del primer miembro:");
        db.getCollection("members")
                .aggregate(List.of(
                        limit(1), // Solo el primer miembro
                        lookup("projects", "projects", "_id", "mp"), // consulta col `projects`,campo local `projects` se corresponde con el `_id` remoto, lo pilla como `mp`
                        unwind("$mp"), // "Saca" el MP cada proyecto a un documento
                        project(BsonDocument.parse("{mp: {name: 1}}")) // Saco solo el nombre del memberProject (el nombre del proyecto)
                )).forEach(p -> {
                    var mp = p.get("mp"); // Pillo el "mp"
                    System.out.println(mp); // imprimo
                });
        System.out.println("========================================");
        //


        //Exportar cada una de las colecciones a un fichero en formato .json.
    }
}