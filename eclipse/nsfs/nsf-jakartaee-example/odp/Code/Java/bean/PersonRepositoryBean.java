package bean;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import model.Person;
import model.PersonRepository;

@RequestScoped
@Named("PersonRepository")
public class PersonRepositoryBean {
	@Inject
	PersonRepository personRepository;
	
	public List<Person> getAll() {
		return personRepository.findAll().collect(Collectors.toList());
	}
}
