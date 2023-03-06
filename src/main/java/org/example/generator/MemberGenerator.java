package org.example.generator;

import org.bson.types.ObjectId;
import org.example.entity.Member;
import org.example.entity.Project;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class MemberGenerator extends Generator<Member> {
    @Override
    public Member generate() {
        return new Member()
                .withId(ObjectId.get())
                .withName(faker.name().fullName())
                .withBiography(faker.lorem().paragraph())
                .withEmail(faker.internet().emailAddress())
                .withProjects(new HashSet<>());
    }
}
