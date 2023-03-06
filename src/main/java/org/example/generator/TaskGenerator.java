package org.example.generator;

import org.bson.types.ObjectId;
import org.example.entity.Task;
import org.example.generator.MemberGenerator;

public class TaskGenerator extends Generator<Task>{
    @Override
    public Task generate() {
        return new Task()
                .withId(ObjectId.get())
                .withName(faker.lorem().word())
                .withDescription(faker.lorem().sentence());
    }
}
