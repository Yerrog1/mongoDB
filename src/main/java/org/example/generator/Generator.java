package org.example.generator;

import net.datafaker.Faker;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Generator<T> {
    protected Faker faker;

    public Generator() {
        this.faker = new Faker();
    }
    public List<T> generate(int quantity){
        return Stream.generate(this::generate).limit(quantity).collect(Collectors.toCollection(ArrayList::new));
    }

    public abstract T generate();




}
