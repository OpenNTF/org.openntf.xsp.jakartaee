package controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ibm.commons.util.StringUtil;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.data.Sort;
import model.ToDo;

@RequestScoped
@Named
public class ToDosController {
	
	@Inject
	private ToDo.Repository repository;
	
	@Inject
	@Named("viewScope")
	private Map<String, Object> viewScope;
	
	public List<ToDo> list(ToDo.State status) {
		if(status == null) {
			return repository.findAll(Sort.asc("created")).collect(Collectors.toList());
		} else {
			return repository.findByStatus(status, Sort.asc("created")).collect(Collectors.toList());
		}
	}
	
	public ToDo getEditingToDo() {
		String documentId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("documentId");
		return (ToDo)viewScope.computeIfAbsent("todo", key -> repository.findById(documentId).get());
	}
	
	public boolean deleteToDo(String documentId) {
		repository.deleteById(documentId);
		return true;
	}
	
	public ToDo getNewToDo() {
		return (ToDo)viewScope.computeIfAbsent("newToDo", key -> new ToDo());
	}
	
	public boolean saveNewToDo(AjaxBehaviorEvent event) {
		ToDo todo = getNewToDo();
		if(StringUtil.isEmpty(todo.getTitle())) {
			return true;
		}
		todo.setCreated(OffsetDateTime.now());
		todo.setStatus(ToDo.State.Incomplete);

		repository.save(todo);
		
		viewScope.put("newToDo", new ToDo());
		return true;
	}
	
	public boolean saveEditingToDo(AjaxBehaviorEvent event) {
		ToDo todo = getEditingToDo();
		if(todo == null) {
			return false;
		}
		
		repository.save(todo);
		
		return true;
	}
	
	public boolean toggleState(ToDo todo) {
		todo.setStatus(todo.getStatus() == ToDo.State.Complete ? ToDo.State.Incomplete : ToDo.State.Complete);
		repository.save(todo);
		return true;
	}
	
	public ToDo.State[] getStates() {
		return ToDo.State.values();
	}
}
