package de.rieckpil.blog.boundary;

import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import de.rieckpil.blog.control.CustomerService;
import de.rieckpil.blog.control.OrderService;
import de.rieckpil.blog.entity.Customer;
import de.rieckpil.blog.entity.Order;

@Named("indexBean")
@RequestScoped
public class IndexBean {
	private Class<?> axisClass;
	@SuppressWarnings("rawtypes")
	private Class axisTypeClass;
	private Class<?> barChartModelClass;
	private Class<?> chartSeriesClass;

	@Inject
	private CustomerService customerService;

	@Inject
	private OrderService orderService;

	private Object customerBarModel;

	private String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; //$NON-NLS-1$

	private List<Order> orders;

	@PostConstruct
	public void init() {
		try {
			ClassLoader cl = (ClassLoader)((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext()).getAttribute("org.openntf.xsp.jsf.nsf.NSFJsfServlet_classLoader");
			
			axisClass = Class.forName("org.primefaces.model.chart.Axis", true, cl); //$NON-NLS-1$
			axisTypeClass = Class.forName("org.primefaces.model.chart.AxisType", true, cl); //$NON-NLS-1$
			barChartModelClass = Class.forName("org.primefaces.model.chart.BarChartModel", true, cl); //$NON-NLS-1$
			chartSeriesClass = Class.forName("org.primefaces.model.chart.ChartSeries", true, cl); //$NON-NLS-1$
		} catch(ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		this.orders = orderService.getOrders();
		createCustomerRevenueBarChart();
	}

	private void createCustomerRevenueBarChart() {

		try {
			Object model = barChartModelClass.getConstructor().newInstance();
	
			Object revenue = chartSeriesClass.getConstructor().newInstance();
			chartSeriesClass.getMethod("setLabel", String.class).invoke(revenue, "Revenue"); //$NON-NLS-1$ //$NON-NLS-2$
	
			List<Customer> customers = customerService.getCustomers();
	
			for (int i = 0; i < alphabet.length(); i++) {
				String letter = alphabet.substring(i, i + 1);
	
				long summedRevenueByLetter = customers.stream()
						.filter(c -> c.getFirstName().substring(0, 1).equalsIgnoreCase(letter))
						.mapToLong(Customer::getBilledRevenue).sum();
	
				chartSeriesClass.getMethod("set", Object.class, Number.class).invoke(revenue, letter, summedRevenueByLetter); //$NON-NLS-1$
			}
	
			barChartModelClass.getMethod("addSeries", chartSeriesClass).invoke(model, revenue); //$NON-NLS-1$
			barChartModelClass.getMethod("setTitle", String.class).invoke(model, "Customer revenue (grouped by first name starting-letter)"); //$NON-NLS-1$ //$NON-NLS-2$
			barChartModelClass.getMethod("setLegendPosition", String.class).invoke(model, "ne"); //$NON-NLS-1$ //$NON-NLS-2$
			barChartModelClass.getMethod("setSeriesColors", String.class).invoke(model, "007ad9"); //$NON-NLS-1$ //$NON-NLS-2$
	
			@SuppressWarnings("unchecked")
			Object xAxisEnum = Enum.valueOf(axisTypeClass, "X"); //$NON-NLS-1$
			Object xAxis = barChartModelClass.getMethod("getAxis", axisTypeClass).invoke(model, xAxisEnum); //$NON-NLS-1$
			axisClass.getMethod("setLabel", String.class).invoke(xAxis, "First name starting-letter"); //$NON-NLS-1$ //$NON-NLS-2$

			@SuppressWarnings("unchecked")
			Object yAxisEnum = Enum.valueOf(axisTypeClass, "Y"); //$NON-NLS-1$
			Object yAxis = barChartModelClass.getMethod("getAxis", axisTypeClass).invoke(model, yAxisEnum); //$NON-NLS-1$
			axisClass.getMethod("setLabel", String.class).invoke(yAxis, "Revenue"); //$NON-NLS-1$ //$NON-NLS-2$
			
			customerBarModel = model;
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Object getCustomerBarModel() {
		return customerBarModel;
	}

	public List<Order> getOrders() {
		return orders;
	}

}
