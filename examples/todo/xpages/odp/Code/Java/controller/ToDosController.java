package controller;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;

import bean.ToDosBean;
import model.ToDo;

public class ToDosController implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public List<ToDo> list(ToDo.State status) {
		ToDosBean bean = (ToDosBean)ExtLibUtil.resolveVariable("ToDos");
		if(status == null) {
			return bean.getAll();
		} else {
			if(status == ToDo.State.Complete) {
				return bean.getComplete();
			} else {
				return bean.getIncomplete();
			}
		}
	}

	public boolean toggleState() {
		ToDo todo = (ToDo)ExtLibUtil.resolveVariable("todo");
		todo.setStatus(todo.getStatus() == ToDo.State.Complete ? ToDo.State.Incomplete : ToDo.State.Complete);

		ToDosBean bean = (ToDosBean)ExtLibUtil.resolveVariable("ToDos");
		bean.saveToDo(todo);
		
		return true;
	}
	
	public boolean deleteToDo() {
		ToDo todo = (ToDo)ExtLibUtil.resolveVariable("todo");

		ToDosBean bean = (ToDosBean)ExtLibUtil.resolveVariable("ToDos");
		bean.deleteToDo(todo);
		
		return true;
	}
	
	public ToDo getNewToDo() {
		return (ToDo)ExtLibUtil.getViewScope().computeIfAbsent("newToDo", key -> new ToDo());
	}
	
	public String saveNewToDo() {
		ToDo todo = getNewToDo();
		if(StringUtil.isEmpty(todo.getTitle())) {
			return "xsp-failure";
		}
		todo.setCreated(OffsetDateTime.now());
		todo.setStatus(ToDo.State.Incomplete);

		ToDosBean bean = (ToDosBean)ExtLibUtil.resolveVariable("ToDos");
		bean.saveToDo(todo);
		
		ExtLibUtil.getViewScope().put("newToDo", new ToDo());
		return "xsp-success";
	}
	
	public String saveExistingToDo() {
		ToDo todo = (ToDo)ExtLibUtil.resolveVariable("todo");
		if(todo == null) {
			return "xsp-failure";
		}

		ToDosBean bean = (ToDosBean)ExtLibUtil.resolveVariable("ToDos");
		bean.saveToDo(todo);
		
		return "xsp-success";
	}
}
