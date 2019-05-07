package com.tdl.model;

import java.util.ArrayList;
import java.util.Date;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ToDoNoteOnlyEmail {
	private Integer id;
	@NotNull(message = "Name may not be null")
	private String name;
	@JsonFormat(pattern="yyyy-MM-dd")
	private Date dateToComplete;
	private String description;
	private Integer priority;
	private Boolean completed;
	private ArrayList<String> userEmails; // users who commited to do the Todos
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getDateToComplete() {
		return dateToComplete;
	}
	public void setDateToComplete(Date dateToComplete) {
		this.dateToComplete = dateToComplete;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getPriority() {
		return priority;
	}
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	public Boolean getCompleted() {
		return completed;
	}
	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}
	public ArrayList<String> getUserEmails() {
		return userEmails;
	}
	public void setUserEmails(ArrayList<String> userEmails) {
		this.userEmails = userEmails;
	}
	public ToDoNoteOnlyEmail(Integer id, @NotNull(message = "Name may not be null") String name, Date dateToComplete,
			String description, Integer priority, Boolean completed, ArrayList<String> userEmails) {
		super();
		this.id = id;
		this.name = name;
		this.dateToComplete = dateToComplete;
		this.description = description;
		this.priority = priority;
		this.completed = completed;
		this.userEmails = userEmails;
	}
	public ToDoNoteOnlyEmail() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
