package rest;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.data.Sort;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import model.Employee;

@Path("employees")
public class EmployeeResource {
	
	@Inject
	private Employee.Repository employees;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(description="Retrieves a list of all employee entities in the data store")
	public List<Employee> get() {
		return employees.findAll(Sort.asc("name")).collect(Collectors.toList());
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Employee create(@Valid Employee employee) {
		employee.setId(null);
		return employees.save(employee);
	}

	@Path("{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Employee getEmployee(@PathParam("id") String id) {
		return employees.findById(id)
			.orElseThrow(() -> new NotFoundException(MessageFormat.format("Could not find employee for ID {0}", id)));
	}
	
	@Path("{id}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Employee update(@PathParam("id") String id, @Valid Employee employee) {
		employee.setId(id);
		return employees.save(employee);
	}
	
	@Path("{id}")
	@DELETE
	public void delete(@PathParam("id") String id) {
		employees.deleteById(id);
	}
}
