package com.tdl.service;

import java.io.IOException;
import java.net.ConnectException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tdl.model.ArrayResponsePojo;
import com.tdl.model.ToDoNote;
import com.tdl.model.ToDoNoteDTO;
import com.tdl.model.User;

@Component
public class ToDoNoteServiceImpl implements ToDoNoteService{
	
	private static List<ToDoNoteDTO> todos = new ArrayList<>();
	
	static {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		//Calendar c = Calendar.getInstance();
		//Date d= new Date();
		//c.add(Calendar.DATE, 3);
		ToDoNoteDTO workout1 = new ToDoNoteDTO(1, "Monday workout",  new Date(), "Leg Day", 1, false);
		ToDoNoteDTO workout2 = new ToDoNoteDTO(2, "Just workout",  new Date()   , "Full Body Day", 2, false);
		ToDoNoteDTO workout3 = new ToDoNoteDTO(3, "An workout",  new Date()  , "Sleep Day", 2, false);
		
		try {
			 workout1 = new ToDoNoteDTO(1, "Monday workout",  (Date)dateFormat.parse("2019-03-22")   , "Leg Day", 1, false);
			 workout2 = new ToDoNoteDTO(2, "Just workout",  (Date)dateFormat.parse("2019-03-20")   , "Full Body Day", 2, false);
			 workout3 = new ToDoNoteDTO(3, "An workout",  (Date)dateFormat.parse("2019-03-19")  , "Sleep Day", 2, false);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RestTemplate restTemplate = new RestTemplate();
		
		
		final String uri = "http://friend:5000/"; //2nd service database fill
		
		try {
			restTemplate.getForEntity(uri, String.class); 
			//return;
			
		}
		catch (HttpClientErrorException ex) {
			ex.printStackTrace();
		     
		}
		
		catch(RestClientException ex2) { // nepavyko prisijungti pacioj pradzioj
			System.out.println(ex2.getCause());
			if(ex2.getCause() instanceof ConnectException) {
				//TODO somehow handle it and add the else if same in below connection
				//return new ResponseEntity<String>("\"Coudl not connect\"",HttpStatus.CONFLICT);
			}
			
		}
		
		final String uriGet = "http://friend:5000/users";
		try {
			ResponseEntity<String> startingUsers =restTemplate.getForEntity(uriGet, String.class); 
			ObjectMapper mapper = new ObjectMapper();
			ArrayResponsePojo response = mapper.readValue(startingUsers.getBody(),ArrayResponsePojo.class);
			
			ArrayList<User> users = response.getData(); // at start there are 3 users
			workout1.addUserEmail(users.get(0).getEmail());
			workout2.addUserEmail(users.get(1).getEmail());
			//workout3.addUser(users.get(2)); // vis del to yra tik 2
		}
		
		catch (HttpClientErrorException | IOException ex) {
			ex.printStackTrace();
		     
		}
		
		catch(RestClientException ex2) { // nepavyko prisijungti pacioj pradzioj
			if(ex2.getCause() instanceof ConnectException) {
				System.out.println(ex2.getCause());
				//return new ResponseEntity<String>("\"Coudl not connect\"",HttpStatus.CONFLICT);
			}
		}
		
		
		
		todos.add(workout1);
		todos.add(workout2);
		todos.add(workout3);
	}
	
	@Override
	public List<ToDoNoteDTO> getAllToDoNoteDTO() {
		return todos;
	}

	@Override
	public ToDoNoteDTO getToDoNoteDTOById(Integer id) {
		for(ToDoNoteDTO toDoNote : todos) {
			if(toDoNote.getId() == id) {
				return toDoNote;
			}
		}
		return null;
	}

	@Override
	public ToDoNoteDTO addToDoNoteDTO(ToDoNoteDTO toDoNote) {
		
		
		ToDoNoteDTO tempNote = todos.get(todos.size()-1);
		int id = tempNote.getId()+1;
		toDoNote.setId(id);
		todos.add(toDoNote);
		return toDoNote;
		
	}

	@Override
	public void updateToDoNoteDTO(ToDoNoteDTO toDoNote) { // cia nera useriu updatinimo tho
		for(ToDoNoteDTO oldToDoNote : todos) {
			if(oldToDoNote.getId() == toDoNote.getId()) {
				oldToDoNote.setName(toDoNote.getName());
				oldToDoNote.setDateToComplete(toDoNote.getDateToComplete());
				oldToDoNote.setDescription(toDoNote.getDescription());
				oldToDoNote.setPriority(toDoNote.getPriority());
				oldToDoNote.setCompleted(toDoNote.isCompleted());
				oldToDoNote.setEmails(toDoNote.getUserEmails());
			}
		}
	}

	@Override
	public void deleteToDoNoteDTO(Integer id) {
		for(Iterator<ToDoNoteDTO> it= todos.iterator(); it.hasNext();) {
			ToDoNoteDTO toDoNote = it.next();
			if(toDoNote.getId() == id) {
				it.remove();
				break;
			}
		}
		
	}
	public List<String> getAllNotesUsers(ToDoNoteDTO toDoNote){ // pakeisti kad imtu id
		return toDoNote.getUserEmails();
	
	}
	
	
	
	public boolean isUserPresent(int noteId, String email) {
		ToDoNoteDTO noteDTO = this.getToDoNoteDTOById(noteId);
		ArrayList<String> userEmails = noteDTO.getUserEmails();
		for(int i=0; i<userEmails.size(); i++) {
			String tempEmail = userEmails.get(i);
			if(tempEmail.equals(email)) {
				return true;
			}
		}
		return false;
	}
	
	
	public void removeUser(int noteId, String email) {
		ArrayList <String> userEmails = this.getToDoNoteDTOById(noteId).getUserEmails();
		for(Iterator<String> it= userEmails.iterator(); it.hasNext();) {
			String userEmail = it.next();
			if(userEmail.equals(email)) {
				it.remove();
				break;
				
			}
		}
		
		
	}

}
