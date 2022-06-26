package model;

import java.util.stream.Stream;

import org.openntf.xsp.nosql.mapping.extension.DominoRepository;

public interface ExampleDocRepository extends DominoRepository<ExampleDoc, String> {
	Stream<ExampleDoc> findAll();
}
