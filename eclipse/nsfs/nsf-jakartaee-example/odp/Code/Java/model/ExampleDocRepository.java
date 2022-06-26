package model;

import java.util.stream.Stream;

import org.openntf.xsp.nosql.mapping.extension.DominoRepository;
import org.openntf.xsp.nosql.mapping.extension.ViewEntries;

public interface ExampleDocRepository extends DominoRepository<ExampleDoc, String> {
	String VIEW_DOCS = "Example Docs";
	
	Stream<ExampleDoc> findAll();
	
	@ViewEntries(VIEW_DOCS)
	Stream<ExampleDoc> getViewEntries();
	
	@ViewEntries(value=VIEW_DOCS, documentsOnly=true)
	Stream<ExampleDoc> getViewEntriesDocsOnly();

	@ViewEntries(value=VIEW_DOCS, maxLevel=0)
	Stream<ExampleDoc> getViewCategories();
}
