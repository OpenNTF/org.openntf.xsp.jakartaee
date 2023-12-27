package bean;

import java.util.List;
import java.util.stream.Collectors;

import com.ibm.commons.util.StringUtil;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.nosql.mapping.Sorts;
import model.ToDo;

@ApplicationScoped
@Named("ToDos")
public class ToDosBean {
	@Inject
	private ToDo.Repository repository;
	
	public List<ToDo> getAll() {
		return repository.findAll(Sorts.sorts().asc("created")).collect(Collectors.toList());
	}
	
	public List<ToDo> getComplete() {
		return repository.findByStatus(ToDo.State.Complete, Sorts.sorts().asc("created")).collect(Collectors.toList());
	}
	
	public List<ToDo> getIncomplete() {
		return repository.findByStatus(ToDo.State.Incomplete, Sorts.sorts().asc("created")).collect(Collectors.toList());
	}
	
	public ToDo getToDo(String documentId) {
		return repository.findById(documentId).get();
	}
	
	public ToDo saveToDo(ToDo todo) {
		return repository.save(todo);
	}
	
	public void deleteToDo(ToDo todo) {
		String id = todo.getDocumentId();
		if(StringUtil.isEmpty(id)) {
			return;
		}
		repository.deleteById(id);
	}
}
