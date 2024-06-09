package model;

import java.util.stream.Stream;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.openntf.xsp.jakarta.nosql.mapping.extension.DominoRepository;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;
import jakarta.data.Sort;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "Represents an individual employee within the system")
@Entity
public class Employee {
	public interface Repository extends DominoRepository<Employee, String> {
		Stream<Employee> findAll(Sort<?> sorts);
	}
	
	private @Id String id;
	@Schema(description="The employee's full name", example="Foo Fooson")
	private @Column @NotEmpty String name;
	@Schema(description="The employee's job title", example="CTO")
	private @Column @NotEmpty String title;
	@Schema(description="The name of the employee's current department within the company", example="IT")
	private @Column @NotEmpty String department;
	@Schema(description="The employee's current age", example="80")
	private @Column @Min(1) int age;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	
	
}
