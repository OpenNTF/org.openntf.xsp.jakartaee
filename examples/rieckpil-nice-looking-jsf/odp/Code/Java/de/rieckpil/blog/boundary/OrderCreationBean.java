package de.rieckpil.blog.boundary;

import java.util.Date;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import de.rieckpil.blog.control.CustomerService;
import de.rieckpil.blog.control.OrderService;
import de.rieckpil.blog.entity.Customer;
import de.rieckpil.blog.entity.Order;

@Named
@RequestScoped
public class OrderCreationBean {

	@NotEmpty
	private String taxId;

	@NotNull
	private Customer selectedCustomer;

	@NotEmpty
	private String selectedProduct;
	private boolean expressDelivery;

	@NotNull
	@Positive
	private Integer amount;

	@NotNull
	private Date orderDate;

	private List<Customer> availableCustomers;

	@Inject
	private CustomerService customerService;

	@Inject
	private OrderService orderService;

	@PostConstruct
	public void init() {
		availableCustomers = customerService.getCustomers();
	}

	public String createOder() {
		Order order = new Order(taxId, selectedCustomer.getCustomerId(), selectedProduct, amount, orderDate,
				expressDelivery);
		orderService.addOrder(order);

		FacesContext context = FacesContext.getCurrentInstance();
		context.addMessage(null, new FacesMessage("Successful", "Sucessfully created order:  " + order.getOrderId()));
		context.getExternalContext().getFlash().setKeepMessages(true);

		return "/createOrder.xhtml?faces-redirect=true";
	}

	public List<Customer> getAvailableCustomers() {
		return availableCustomers;
	}

	public String getTaxId() {
		return taxId;
	}

	public void setTaxId(String taxId) {
		this.taxId = taxId;
	}

	public Customer getSelectedCustomer() {
		return selectedCustomer;
	}

	public void setSelectedCustomer(Customer selectedCustomer) {
		this.selectedCustomer = selectedCustomer;
	}

	public String getSelectedProduct() {
		return selectedProduct;
	}

	public void setSelectedProduct(String selectedProduct) {
		this.selectedProduct = selectedProduct;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	public boolean isExpressDelivery() {
		return expressDelivery;
	}

	public void setExpressDelivery(boolean expressDelivery) {
		this.expressDelivery = expressDelivery;
	}

}
