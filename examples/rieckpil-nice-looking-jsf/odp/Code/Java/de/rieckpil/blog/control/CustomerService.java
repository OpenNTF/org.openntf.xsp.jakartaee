package de.rieckpil.blog.control;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import jakarta.annotation.PostConstruct;

import de.rieckpil.blog.entity.Customer;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CustomerService {

	private List<Customer> customers;

	@PostConstruct
	public void init() {
		this.customers = createRandomCustomers();
	}

	public List<Customer> getCustomers() {
		return customers;
	}

	public void deleteCustomer(Customer customer) {
		this.customers.remove(customer);

		if (this.customers.isEmpty()) {
			this.customers = createRandomCustomers();
		}
	}

	public Customer findByCustomerId(String customerId) {
		return this.customers.stream().filter(c -> c.getCustomerId().equals(customerId)).findFirst().orElse(null);
	}

	private List<Customer> createRandomCustomers() {
		List<Customer> result = new ArrayList<>();

		byte[] id = new byte[10];
		SecureRandom rnd = new SecureRandom();
		for (int i = 0; i < 100; i++) {
			int charIndex = rnd.nextInt(26);
			char initial = (char)('A' + charIndex);
			
			String firstName = initial + "oo" + System.currentTimeMillis(); //$NON-NLS-1$
			String lastName = initial + "ooson" + System.currentTimeMillis(); //$NON-NLS-1$
			rnd.nextBytes(id);
			result.add(new Customer(firstName, lastName, Base64.getEncoder().encodeToString(id),
					ThreadLocalRandom.current().nextLong(1_000_000)));
		}

		return result;
	}
}
