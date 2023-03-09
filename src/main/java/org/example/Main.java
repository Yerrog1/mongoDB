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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    private static String nombrePersona;
    private static String idPersona;
    private static String idProyecto;
    private static String idTarea;

    public static void main(String[] args) {
        createCollections();
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
        nombrePersona = members.get(0).getName();
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
        idPersona = members.get(5).getId().toString();
        idProyecto = projects.get(54).getId().toString();
        idTarea = tasks.get(69).getId().toString();
    }

    public static void consultas() {
        MongoCollection<Member> memberCollection = db.getCollection(Member.COLLECTION_NAME, Member.class);
        MongoCollection<Project> projectCollection = db.getCollection(Project.COLLECTION_NAME, Project.class);
        MongoCollection<Task> taskCollection = db.getCollection(Task.COLLECTION_NAME, Task.class);
        List<Member> members;
        List<Project> projects;
        List<Task> tasks;
        Bson filter;
        ObjectId taskId = new ObjectId("6409906aa821b0309efcce05");
        //6 operaciones de consulta empleando filtros y proyecciones.
        System.out.println("====================================================================================================");
        consulta1(memberCollection);
        System.out.println("====================================================================================================");
        consulta2(projectCollection);
        System.out.println("====================================================================================================");
        consulta3(memberCollection);
        System.out.println("====================================================================================================");
        consulta4(taskCollection);
        System.out.println("====================================================================================================");
        consulta5(projectCollection);
        System.out.println("====================================================================================================");
        consulta6(memberCollection);
        System.out.println("====================================================================================================");
        consulta7(memberCollection);
        System.out.println("====================================================================================================");
        System.out.println("====================================================================================================");
        //6 operaciones de actualización.
        actualizacion1(projectCollection);
        actualizacion2(projectCollection, memberCollection);
        actualizacion3(memberCollection, projectCollection, taskCollection, taskId);
        actualizacion4(projectCollection, taskCollection);
        actualizacion5(memberCollection, projectCollection);
        actualizacion6(memberCollection);
        //3 operaciones de agregación (mediante canalización de agregación), dos de las cuales deben incluir funciones de agregación.
        System.out.println("====================================================================================================");
        agregacion1();
        System.out.println("====================================================================================================");
        agregacion2();
        System.out.println("====================================================================================================");
        agregacion3();
        //Exportar cada una de las colecciones a un fichero en formato .json.
        System.out.println("====================================================================================================");
        System.out.println("====================================================================================================");
        System.out.println("Exportar cada una de las colecciones a un fichero en formato .json.");
        System.out.println("====================================================================================================");
        System.out.println("====================================================================================================");
        System.out.println("Exportando miembros...");
        exportCollection("members");
        System.out.println("Exportando proyectos...");
        exportCollection("projects");
        System.out.println("Exportando tareas...");
        exportCollection("tasks");
    }

    private static void agregacion3() {
        //Obtener el número de tareas que hay en cada proyecto:
        System.out.println("Obtener el número de tareas que hay en cada proyecto:");
        db.getCollection("projects")
                .aggregate(List.of(
                        lookup("tasks", "tasks", "_id", "tasks"), // consulta col `tasks`,campo local `tasks` se corresponde con el `_id` remoto, lo pilla como `tasks`
                        unwind("$tasks"), // "Saca" el task cada tarea a un documento
                        group("$name", Accumulators.sum("tasks", 1)) // Agrupo por nombre de proyecto y cuento las tareas
                )).forEach(p -> {
                    var name = p.get("_id"); // Pillo el "_id"
                    var taskss = p.get("tasks"); // Pillo el "tasks"
                    System.out.println(name + " " + taskss); // imprimo
                });
    }

    private static void agregacion2() {
        //Obtener el número de miembros que hay en cada proyecto:
        System.out.println("Obtener el número de miembros que hay en cada proyecto:");
        db.getCollection("projects")
                .aggregate(List.of(
                        lookup("members", "members", "_id", "members"), // consulta col `members`,campo local `members` se corresponde con el `_id` remoto, lo pilla como `members`
                        unwind("$members"), // "Saca" el member cada miembro a un documento
                        group("$name", Accumulators.sum("members", 1)) // Agrupo por nombre de proyecto y cuento los miembros
                )).forEach(p -> {
                    var name = p.get("_id"); // Pillo el "_id"
                    var memberss = p.get("members"); // Pillo el "members"
                    System.out.println(name + " " + memberss); // imprimo
                });
    }

    private static void agregacion1() {
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
    }

    private static void actualizacion6(MongoCollection<Member> memberCollection) {
        //Actualizar la biografía de un miembro:
        ObjectId memberId = new ObjectId(idPersona);
        System.out.println("Actualizar la biografía de un miembro:");
        memberCollection.updateOne(
                Filters.eq("_id", memberId),
                Updates.set("biography", "Nueva biografía del miembro actualizada el " + LocalDateTime.now().toString())
        );
    }

    private static void actualizacion5(MongoCollection<Member> memberCollection, MongoCollection<Project> projectCollection) {
        //Agregar un nuevo miembro:
        System.out.println("Agregar un nuevo miembro:");
        Member newMember = new Member(new ObjectId(), "Nuevo miembro", "Biografía del nuevo miembro creado " + LocalDateTime.now().toString(), "correo@nuevomiembro.com", new HashSet<>(), null);

        memberCollection.insertOne(newMember);

        List<Project> memberProjects = projectCollection.find(
                Filters.in("_id", newMember.getProjects())
        ).into(new ArrayList<>());
    }

    private static void actualizacion4(MongoCollection<Project> projectCollection, MongoCollection<Task> taskCollection) {
        //Agregar una tarea a un proyecto:
        ObjectId id = new ObjectId(idProyecto);
        System.out.println("Agregar una tarea a un proyecto:");
        Task newTask = new Task(new ObjectId(), "Nombre de la tarea", "Descripción de la tarea creada "
                + LocalDateTime.now().toString(), null, id);

        taskCollection.insertOne(newTask);

        projectCollection.updateOne(
                Filters.eq("_id", id),
                Updates.addToSet("tasks", newTask.getId())
        );
    }

    private static void actualizacion3(MongoCollection<Member> memberCollection, MongoCollection<Project> projectCollection, MongoCollection<Task> taskCollection, ObjectId taskId) {
        Bson filter;
        //Eliminar una tarea de un proyecto y desasignarla de su propietario:
        ObjectId projectId = new ObjectId(idProyecto);
        ObjectId taskId1 = new ObjectId(idTarea);
        System.out.println("Eliminar una tarea de un proyecto y desasignarla de su propietario:");
        taskCollection.updateOne(
                Filters.eq("_id", taskId),
                Updates.set("project", null)
        );


        projectCollection.updateOne(
                Filters.eq("_id", projectId),
                Updates.pull("tasks", taskId1)
        );
        filter = Filters.eq("_id", taskId1);
        ArrayList<Task> result = taskCollection.find(filter).into(new ArrayList<>());
        Object ownerId = result.get(0).getOwner();
        memberCollection.updateOne(
                Filters.eq("_id", ownerId),
                Updates.set("assignedTask", null)
        );
    }

    private static void actualizacion2(MongoCollection<Project> projectCollection, MongoCollection<Member> memberCollection) {
        //Agregar un miembro a un proyecto existente:
        ObjectId projectId = new ObjectId(idProyecto);
        ObjectId memberId = new ObjectId(idPersona);
        System.out.println("Agregar un miembro a un proyecto existente:");
        projectCollection.updateOne(
                Filters.eq("_id", projectId),
                Updates.addToSet("members", memberId)
        );
        memberCollection.updateOne(
                Filters.eq("_id", memberId),
                Updates.addToSet("projects", projectId)
        );
    }

    private static void actualizacion1(MongoCollection<Project> projectCollection) {
        //Actualizar el nombre de un proyecto:
        ObjectId projectId = new ObjectId(idProyecto);
        System.out.println("Actualizar el nombre de un proyecto:");
        projectCollection.updateOne(
                Filters.eq("_id", projectId),
                Updates.set("name", "Nuevo nombre del proyecto actualizado " + LocalDateTime.now().toString())
        );
    }

    private static void consulta7(MongoCollection<Member> memberCollection) {
        List<Member> members;
        Bson filter;
        //Buscar todos los miembros con una biografía que contiene la palabra "magni":
        filter = Filters.regex("biography", "magni");
        members = memberCollection.find(filter).into(new ArrayList<>());
        System.out.println("Miembros con una biografía que contiene la palabra \"magni\": ");
        for (Member member : members) {
            System.out.println(member.getId() + " " + member.getName());
        }
    }

    private static void consulta6(MongoCollection<Member> memberCollection) {
        List<Member> members;
        Bson filter;
        //Buscar todos los miembros que tienen una tarea asignada:
        filter = Filters.exists("assignedTask");
        members = memberCollection.find(filter).into(new ArrayList<>());
        System.out.println("Miembros que tienen una tarea asignada: ");
        for (Member member : members) {
            System.out.println(member.getId() + " " + member.getName());
        }
    }

    private static void consulta5(MongoCollection<Project> projectCollection) {
        //Buscar todos los proyectos que contienen al menos una tarea:
        Bson filter = Filters.exists("tasks");
        Bson projection = Projections.include("name");
        List<Project> projects = projectCollection.find(filter).projection(projection).into(new ArrayList<>());
        System.out.println("Proyectos que contienen al menos una tarea: ");
        for (Project project : projects) {
            System.out.println(project.getId() + " " + project.getName());
        }
    }


    private static void consulta1(MongoCollection<Member> memberCollection) {
        //Buscar todos los miembros con el nombre de la primera persona:
        Bson filter = Filters.eq("name", nombrePersona);
        List<Member> members = memberCollection.find(filter).into(new ArrayList<>());
        System.out.println("Miembros con el nombre " + nombrePersona + ": ");
        for (Member member : members) {
            System.out.println(member.getId() + " " + member.getName());
        }
    }

    private static void consulta2(MongoCollection<Project> projectCollection) {
        //Buscar todos los proyectos propiedad de un miembro con un ID determinado:
        ObjectId memberId = new ObjectId(idPersona);
        Bson filter = Filters.eq("owner", memberId);
        List<Project> projects = projectCollection.find(filter).into(new ArrayList<>());
        System.out.println("Proyectos propiedad de un miembro con ID " + idPersona + ": ");
        for (Project project : projects) {
            System.out.println(project.getId() + " " + project.getName());
        }
    }

    private static void consulta3(MongoCollection<Member> memberCollection) {
        //Buscar todos los miembros que participan en un proyecto con un ID determinado:
        ObjectId projectId = new ObjectId(idProyecto);
        Bson filter = Filters.in("projects", projectId);
        List<Member> members = memberCollection.find(filter).into(new ArrayList<>());
        System.out.println("Miembros que participan en un proyecto con ID " + idProyecto + ": ");
        for (Member member : members) {
            System.out.println(member.getId() + " " + member.getName());
        }
    }

    private static void consulta4(MongoCollection<Task> taskCollection) {
        //Buscar todas las tareas asociadas a un proyecto con un ID determinado:
        ObjectId projectId = new ObjectId(idProyecto);
        Bson filter = Filters.eq("project", projectId);
        Bson projection = Projections.include("name");
        List<Task> tasks = taskCollection.find(filter).projection(projection).into(new ArrayList<>());
        System.out.println("Tareas asociadas a un proyecto con ID " + idProyecto + ": ");
        for (Task task : tasks) {
            System.out.println(task.getId() + " " + task.getName());
        }
    }

    private static void exportCollection(String collection) {

        var col = db.getCollection(collection);
        var cursor = col.find().iterator();
        var json = new ArrayList<String>();
        while (cursor.hasNext()) {
            json.add(cursor.next().toJson());
        }
        try {
            Files.write(Paths.get(collection + ".json"), json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}