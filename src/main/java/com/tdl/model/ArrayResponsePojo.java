package com.tdl.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class ArrayResponsePojo {
	@JsonProperty("data")
	private ArrayList<User> data;
	@JsonProperty("message")
	private String message;
	
	public ArrayList<User> getData() {
		return data;
	}
	public void setData(ArrayList<User> data) {
		this.data = data;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	
	
	public ArrayResponsePojo(){
		super();
	}
	
	@JsonCreator
	public ArrayResponsePojo(@JsonProperty("data") ArrayList<User> data, @JsonProperty ("message") String message) {
		super();
		this.data = data;
		this.message = message;
	}
	
}
