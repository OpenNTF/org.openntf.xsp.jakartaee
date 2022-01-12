package model;

import java.util.stream.Stream;

import jakarta.nosql.mapping.Repository;
import jakarta.nosql.mapping.Sorts;

public interface PersonRepository extends Repository<Person, String> {
	Stream<Person> findAll();
	Stream<Person> findAll(Sorts sorts);
	Stream<Person> findByLastName(String lastName);
}
