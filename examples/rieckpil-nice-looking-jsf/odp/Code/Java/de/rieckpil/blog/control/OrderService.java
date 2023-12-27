package de.rieckpil.blog.control;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import de.rieckpil.blog.entity.Order;

@ApplicationScoped
public class OrderService {

	private List<Order> orders;

	@PostConstruct
	public void init() {
		this.orders = createDefaultOrder();
	}

	private List<Order> createDefaultOrder() {
		List<Order> result = new ArrayList<>();
		result.add(new Order("999-123456789", "DUMMY-DUMMY", "Laptop", 4, new Date(), false));
		return result;
	}

	public List<Order> getOrders() {
		return orders;
	}

	public void addOrder(Order order) {
		this.orders.add(order);
	}

}
