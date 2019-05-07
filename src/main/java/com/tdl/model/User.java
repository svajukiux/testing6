package com.tdl.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
	 @JsonProperty("email")
	 private String email;
	 @JsonProperty("firstName")
	 private String firstName;
	 @JsonProperty("lastName")
	 private String lastName;
	 
	 @JsonCreator
	 public User(@JsonProperty("email")String email,@JsonProperty("firstName") String firstName, @JsonProperty("lastName") String lastName) {
		//super();
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
	}
	 
	public String getEmail() {
	     return email;
	 }
	 public void setEmail(String email) {
	     this.email = email;
	 }
	 public String getFirstName() {
	     return firstName;
	 }
	 
	 public void setFirstName(String firstName) {
        this.firstName = firstName;
	 }
	 public String getLastName() {
        return lastName;
	 }
     public void setLastName(String lastName) {
	     this.lastName = lastName;
	 }
     public User(){
    	 super();
 		}
     
     @Override
     public boolean equals(Object o){
    	 if(o==this) {
    		 return true;
    	 }
    	 if(!(o instanceof User)) {
    		 return false;
    	 }
    	 User u = (User)o;
    	 return u.email.equals(email) && u.firstName.equals(firstName) && u.lastName.equals(lastName);
     
     }
     
     @Override
     public int hashCode() {
    	 return Objects.hash(this.email,this.firstName,this.lastName);
     }

}
