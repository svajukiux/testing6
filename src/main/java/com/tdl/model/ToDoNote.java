package com.tdl.model;

import java.util.ArrayList;
import java.util.Date;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.hateoas.core.Relation;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonFormat;
@Relation(collectionRelation = "notes") 
@Validated
public class ToDoNote {
	private Integer id;
	@NotNull(message = "Name may not be null")
	private String name;
	@JsonFormat(pattern="yyyy-MM-dd")
	private Date dateToComplete;
	private String description;
	private Integer priority;
	private Boolean completed;
	private ArrayList<User> users; // users who commited to do the Todos
	
	
	
	public ArrayList<User> getUsers() {
		return users;
	}

	public void setUsers(ArrayList<User> users) {
		this.users = users;
	}

	public Boolean getCompleted() {
		return completed;
	}

	public ToDoNote() {
		super();
	}

	public ToDoNote(Integer id, String name, Date dateToComplete, String description,Integer priority, Boolean completed) {
		super();
		this.id = id;
		this.name = name;
		this.dateToComplete = dateToComplete;
		this.description = description;
		this.priority = priority;
		this.completed = completed;
		this.users = new ArrayList<>();
	}

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

	public Boolean isCompleted() {
		return completed;
	}

	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}
	
	public void addUser(User user) {
		this.users.add(user);
	}
	
	public boolean checkIfUserExists(User user) {
		if(users.contains(user)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	
	public User getUser(String email) {
		for(int i=0; i<users.size(); i++) {
			if(users.get(i).getEmail().equals(email)) {
			return users.get(i);
			}
		}
		return null;
	}
	
	public void updateUser(User user,String email) {
		for(int i=0; i< users.size(); i++) {
			if(users.get(i).getEmail().equals(email)) {
				if(user.getEmail()!=null) {
					users.get(i).setEmail(user.getEmail());
				}
				if(user.getFirstName()!=null) {
					users.get(i).setFirstName(user.getFirstName());
				}
				if(user.getLastName()!=null) {
					users.get(i).setLastName(user.getLastName());
				}
			}
		}
	}
	
	
	//public User getUser(String email) {
	//	return users
	//}
	
}
