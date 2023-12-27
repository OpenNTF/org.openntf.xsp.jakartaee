package de.rieckpil.blog.control;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

import de.rieckpil.blog.entity.Customer;

@FacesConverter(value = "customerConverter", managed = true)
public class CustomerConverter implements Converter<Customer> {

	@Inject
	private CustomerService customerService;

	@Override
	public String getAsString(FacesContext context, UIComponent component, Customer customer) {

		if (customer == null) {
			return "";
		}

		return customer.getCustomerId();
	}

	@Override
	public Customer getAsObject(FacesContext context, UIComponent component, String value) {
		if (value == null || value.isEmpty()) {
			return null;
		}

		return customerService.findByCustomerId(value);
	}

}