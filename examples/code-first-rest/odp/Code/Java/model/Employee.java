package model;

import java.util.stream.Stream;

import org.openntf.xsp.nosql.mapping.extension.DominoRepository;

import jakarta.nosql.mapping.Column;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.Id;
import jakarta.nosql.mapping.Sorts;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

@Entity
public class Employee {
	public interface Repository extends DominoRepository<Employee, String> {
		Stream<Employee> findAll(Sorts sorts);
	}
	
	private @Id String id;
	private @Column @NotEmpty String name;
	private @Column @NotEmpty String title;
	private @Column @NotEmpty String department;
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
