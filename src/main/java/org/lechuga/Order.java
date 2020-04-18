package org.lechuga;

import java.util.Arrays;
import java.util.List;

public class Order {

	private static final String ASC = " ASC";
	private static final String DESC = " DESC";

	private final String propName;
	private final String order;

	private Order(String propName, String order) {
		super();
		this.propName = propName;
		this.order = order;
	}

	public static List<Order> by(Order... orders) {
		return Arrays.asList(orders);
	}

	public static List<Order> by(List<Order> orders) {
		return orders;
	}

	public static Order asc(String propName) {
		return new Order(propName, ASC);
	}

	public static Order desc(String propName) {
		return new Order(propName, DESC);
	}

	public String getPropName() {
		return propName;
	}

	public String getOrder() {
		return order;
	}
}