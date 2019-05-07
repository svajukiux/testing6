package com.tdl.model;

import java.util.ArrayList;
import java.util.List;

public class Courses {
	public Courses(List<Order> orders) {
		super();
		this.orders = orders;
	}

	List<Order> orders;
	
	public Courses() {
		orders = new ArrayList<>();
	}

	public List<Order> getOrders() {
		return orders;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}
	
	
}
