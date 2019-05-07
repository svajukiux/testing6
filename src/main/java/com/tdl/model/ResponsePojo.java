package com.tdl.model;

public class ResponsePojo {
	User data;
	String message;
	public ResponsePojo() {
		super();
		// TODO Auto-generated constructor stub
	}
	public User getData() {
		return data;
	}
	public void setData(User data) {
		this.data = data;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public ResponsePojo(User data, String message) {
		super();
		this.data = data;
		this.message = message;
	}
	
}
