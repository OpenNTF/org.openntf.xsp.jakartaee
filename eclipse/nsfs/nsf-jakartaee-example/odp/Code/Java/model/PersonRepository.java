package model;

import java.util.stream.Stream;

import jakarta.nosql.mapping.Repository;

public interface PersonRepository extends Repository<Person, String> {
	Stream<Person> findAll();
	Stream<Person> findByLastName(String lastName);
}
