package com.tdl.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;


import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpEntity;
//import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
//import com.svajukiux.controllers.CustomerController;
//import com.svajukiux.controllers.GameController;
import com.tdl.exception.InvalidFieldException;
import com.tdl.exception.ToDoNoteNotFoundException;
import com.tdl.model.ArrayResponsePojo;
import com.tdl.model.Order;
import com.tdl.model.ResponsePojo;
import com.tdl.model.ToDoNote;
import com.tdl.model.ToDoNoteDTO;
import com.tdl.model.User;
import com.tdl.service.ToDoNoteServiceImpl;

import org.modelmapper.ModelMapper;



@RestController
public class ToDoNoteController {
	
	private ModelMapper modelMapper = new ModelMapper();
	private List<ToDoNote> notes;
	
	private SimpleClientHttpRequestFactory getClientHttpRequestFactory()
	{
	    SimpleClientHttpRequestFactory clientHttpRequestFactory
	                      = new SimpleClientHttpRequestFactory();
	    //Connect timeout
	    clientHttpRequestFactory.setConnectTimeout(2000);
	     
	    //Read timeout
	    clientHttpRequestFactory.setReadTimeout(2000);
	    return clientHttpRequestFactory;
	}
	
	/*
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) 
	{
	    return restTemplateBuilder
	       .setConnectTimeout(2000)
	       .setReadTimeout(2000)
	       .build();
	}
	*/
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	@Autowired
	private ToDoNoteServiceImpl toDoNoteService;
	
	// unchecked types
	@GetMapping("/todos") // booo cia reikes List<Resources<ToDoNoteDto>
	public ResponseEntity<?> getAllToDoNote(@RequestParam(value = "embed",required =false)String embed) throws ParseException, JsonParseException, JsonMappingException, IOException{
		List<ToDoNote> notes = new ArrayList<ToDoNote>();
		List<ToDoNoteDTO> notesDTO = new ArrayList<ToDoNoteDTO>();
		notesDTO = toDoNoteService.getAllToDoNoteDTO(); // turim notes su emailais jei ne embed=users galima toki ir grazinti
		//List<ToDoNote> allNotes = toDoNoteService.getAllToDoNote();
		
		// buildas yra kur docker failas mazdaug
		
		
			
		if(embed!=null && embed.equals("users")) {
			//ArrayList<String> emails = new ArrayList<String>();
			RestTemplate restTemplate = new RestTemplate();
			for(int i=0; i< notesDTO.size();i++) {
				ArrayList<String> emails = notesDTO.get(i).getUserEmails();
				if(!emails.isEmpty()) { // jei ne empty email ArrayList
					ArrayList<User> users = new ArrayList<User>();
					for(int j=0; j<emails.size(); j++) {
						final String uri = "http://friend:5000/users/"+emails.get(j);
						ResponseEntity<String> result =null;
						int statusCode=0;
						ObjectMapper mapper = new ObjectMapper();
						//ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);
						try { // If user exists we will just add it to our ToDoNote
							 result = restTemplate.getForEntity(uri, String.class);
							 ResponsePojo pojo = mapper.readValue(result.getBody(), ResponsePojo.class);
							 User userResponse = new User(pojo.getData().getEmail(),pojo.getData().getFirstName(),pojo.getData().getLastName());
							 users.add(userResponse);
							
						}
						catch (HttpClientErrorException ex) {
							return ResponseEntity.status(ex.getRawStatusCode()).headers(ex.getResponseHeaders())
					                .body(ex.getResponseBodyAsString());     
						}
						catch(RestClientException ex2) {
							if(ex2.getCause() instanceof ConnectException) {
								System.out.println(ex2.getCause());
								return new ResponseEntity<String>("\"Could not connect to user web service\"",HttpStatus.SERVICE_UNAVAILABLE);
							}
							else if(ex2.getCause() instanceof UnknownHostException) {
								return new ResponseEntity<String>("\"Unable to connect to user web service\"", HttpStatus.SERVICE_UNAVAILABLE);
								}
							else {
								return new ResponseEntity<String>(ex2.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
							}
						}
						
						// get is kito web serviso pagal emailus
					}
					ToDoNote toDoNote = convertToEntity(notesDTO.get(i),true);
					toDoNote.setUsers(users);
					notes.add(toDoNote);
					// konvertuoti dto i note ir pridet prie jo userius
				}
				else {
					ToDoNote toDoNote = convertToEntity(notesDTO.get(i),true);
					notes.add(toDoNote);
				}
			
			}
			return new ResponseEntity<List<ToDoNote>>(notes,HttpStatus.OK);
			
			//return new Resources<>(noteResources);
		}
		
		else {
			
			
			return new ResponseEntity<List<ToDoNoteDTO>>(notesDTO,HttpStatus.OK);
			//return null;
		}
		
	}
	
	
	
	@GetMapping("/todos/{toDoNoteId}/users")
	public ResponseEntity<?> getNotesUsers(@PathVariable int toDoNoteId) throws JsonParseException, JsonMappingException, IOException {
		ToDoNoteDTO note = toDoNoteService.getToDoNoteDTOById(toDoNoteId);
		RestTemplate restTemplate = new RestTemplate();
		List<User> users = new ArrayList<User>();
		//final String uriTest = "http://193.219.91.103:1858/";

	    //RestTemplate restTemplateTest = new RestTemplate();
	    //String result2 = restTemplateTest.getForObject(uriTest, String.class);

	  //  System.out.println(result2);
	    
		if(note==null) {
			throw new ToDoNoteNotFoundException("Note with id "+ toDoNoteId + " not found");
			
		}
		ArrayList<String> emails = note.getUserEmails();
		if(!emails.isEmpty()) {
			for(int i=0; i<emails.size(); i++) {
				final String uri = "http://friend:5000/users/"+emails.get(i);
				ResponseEntity<String> result =null;
				int statusCode=0;
				ObjectMapper mapper = new ObjectMapper();
				//ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);
				try { // If user exists we will just add it to our ToDoNote
					 result = restTemplate.getForEntity(uri, String.class);
					 ResponsePojo pojo = mapper.readValue(result.getBody(), ResponsePojo.class);
					 User userResponse = new User(pojo.getData().getEmail(),pojo.getData().getFirstName(),pojo.getData().getLastName());
					 users.add(userResponse);
					
				}
				catch (HttpClientErrorException ex) {
					return ResponseEntity.status(ex.getRawStatusCode()).headers(ex.getResponseHeaders())
			                .body(ex.getResponseBodyAsString());     
				}
				catch(RestClientException ex2) {
					if(ex2.getCause() instanceof ConnectException) {
						System.out.println(ex2.getCause());
						return new ResponseEntity<String>("\"Could not connect to user web service\"",HttpStatus.SERVICE_UNAVAILABLE);
					}
					else if(ex2.getCause() instanceof UnknownHostException) {
						return new ResponseEntity<String>("\"Unable to connect to user web service\"", HttpStatus.SERVICE_UNAVAILABLE);
						}
					else {
						return new ResponseEntity<String>(ex2.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}

			}
		}
		return new ResponseEntity<List<User>>(users,HttpStatus.OK);
		
		
		
	}
	
	/*
	@GetMapping("/todos/priority/{number}")
	public List<ToDoNote> getAllPriorityNotes(@PathVariable int number){
		ArrayList<ToDoNote> priorityNotes = new ArrayList<ToDoNote>();
		List<ToDoNote> allNotes = toDoNoteService.getAllToDoNote();
		
		
		for(Iterator<ToDoNote> it= allNotes.iterator(); it.hasNext();) {
			ToDoNote toDoNote = it.next();
			if(toDoNote.getPriority()==number) {
				priorityNotes.add(toDoNote);
			}
		}
		
		return priorityNotes;
	}
	*/
	
	
	@GetMapping("/todos/{toDoNoteId}")
	public ResponseEntity<?> getToDoNoteById(@PathVariable int toDoNoteId,@RequestParam(value = "embed",required =false)String embed) throws ParseException, JsonParseException, JsonMappingException, IOException {
		ToDoNoteDTO noteDTO = toDoNoteService.getToDoNoteDTOById(toDoNoteId);
		ToDoNote toDoNote = null;
		RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
		if(noteDTO==null) {
			throw new ToDoNoteNotFoundException("Note with id "+ toDoNoteId + " not found");
			
		}
		
		if(embed!=null && embed.equals("users")) {
			ArrayList<String> emails = noteDTO.getUserEmails();
			ArrayList<User> users = new ArrayList<User>();
			
			if(!emails.isEmpty()) {
				for(int i=0; i<emails.size(); i++) {
					System.out.println(emails.get(i));
					final String uri = "http://friend:5000/users/"+emails.get(i);
					ResponseEntity<String> result =null;
					int statusCode=0;
					ObjectMapper mapper = new ObjectMapper();
					//ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);
					try { // If user exists we will just add it to our ToDoNote
						System.out.println("pre: "+result);
						 result = restTemplate.getForEntity(uri, String.class);
						 System.out.println(result);
						 ResponsePojo pojo = mapper.readValue(result.getBody(), ResponsePojo.class);
						 User userResponse = new User(pojo.getData().getEmail(),pojo.getData().getFirstName(),pojo.getData().getLastName());
						 users.add(userResponse);
						
					}
					catch (HttpClientErrorException ex) {
						return ResponseEntity.status(ex.getRawStatusCode()).headers(ex.getResponseHeaders())
				                .body(ex.getResponseBodyAsString());     
					}
					catch(RestClientException ex2) {
						if(ex2.getCause() instanceof ConnectException) {
							System.out.println(ex2.getCause());
							return new ResponseEntity<String>("\"Could not connect to user service\"",HttpStatus.SERVICE_UNAVAILABLE);
						}
						else if(ex2.getCause() instanceof UnknownHostException) {
							return new ResponseEntity<String>("\"Unable to connect to user web service\"", HttpStatus.SERVICE_UNAVAILABLE);
							}
						else {
							return new ResponseEntity<String>(ex2.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
						}
					}
					
					// get is kito web serviso pagal emailus
				}
				toDoNote = convertToEntity(noteDTO,true);
				toDoNote.setUsers(users);
				// konvertuoti dto i note ir pridet prie jo userius
			}
			else {
				toDoNote = convertToEntity(noteDTO,true);
			}
			return  new ResponseEntity<ToDoNote>(toDoNote,HttpStatus.OK);
		}
		
		else {
			//ToDoNoteDTO noteDto = convertToDto(note);
			//Resource<ToDoNoteDTO> resource = new Resource<ToDoNoteDTO>(noteDto);
			//resource.add(linkToSelf);
			//resource.add(linkToFull);
			//resource.add(linkToAll);
			return new ResponseEntity<ToDoNoteDTO>(noteDTO,HttpStatus.OK);
		}
		
		
		
	}
	
	
	
	@GetMapping("/users")
	public ResponseEntity<?> getUsersFromOtherService() throws JsonParseException, JsonMappingException, IOException {
		final String uri = "http://friend:5000/users";//"http://friend:5000/users";
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> result =null;
		//int statusCode=0;
		ObjectMapper mapper = new ObjectMapper();
		
		try { // If user exists we will just add it to our ToDoNote
			 result = restTemplate.getForEntity(uri, String.class);
			 ArrayResponsePojo pojo = mapper.readValue(result.getBody(), ArrayResponsePojo.class);
			 //User userToRespond = new User(pojo.getData().getEmail(),pojo.getData().getFirstName(),pojo.getData().getLastName());
			 //ToDoNote toDoNote = toDoNoteService.getToDoNoteById(toDoNoteId);
			 List<User> users = new ArrayList<User>(pojo.getData());
			 //users 
			 return new ResponseEntity<List<User>>(users,HttpStatus.CREATED);
			 	

			//System.out.println("result" + result);
			
			
		}
		catch (HttpClientErrorException ex) {
			System.out.println("value"+ ex.getStatusCode().value());
			return ResponseEntity.status(ex.getRawStatusCode()).headers(ex.getResponseHeaders())
	                .body(ex.getResponseBodyAsString());
		}
		
		catch(RestClientException ex2) {
			if(ex2.getCause() instanceof ConnectException) {
				System.out.println(ex2.getCause());
				return new ResponseEntity<String>("\"Could not connect to user web service\"",HttpStatus.SERVICE_UNAVAILABLE);
			}
			
			else if(ex2.getCause() instanceof UnknownHostException) {
					return new ResponseEntity<String>("\"Unable to connect to user web service\"", HttpStatus.SERVICE_UNAVAILABLE);
			}
		
			else {
				return new ResponseEntity<String>(ex2.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		
		
	}
	
	
	
	
	
	@GetMapping("/todos/{toDoNoteId}/users/{email}")
	public ResponseEntity<?> getUserByEmail(@PathVariable int toDoNoteId,@PathVariable String email) throws JsonParseException, JsonMappingException, IOException {
		ToDoNoteDTO noteDTO = toDoNoteService.getToDoNoteDTOById(toDoNoteId);
		RestTemplate restTemplate = new RestTemplate();
		if(noteDTO==null) {
			throw new ToDoNoteNotFoundException("Note with id "+ toDoNoteId + " not found");
			
		}
		
		if(noteDTO.getUser(email)==null) {
			throw new ToDoNoteNotFoundException("User with "+ email + " not found");
		}
		
		else {
		
			final String uri = "http://friend:5000/users/"+email;
			ResponseEntity<String> result =null;
			int statusCode=0;
			ObjectMapper mapper = new ObjectMapper();
			//ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);
			try { 
				 result = restTemplate.getForEntity(uri, String.class);
				 ResponsePojo pojo = mapper.readValue(result.getBody(), ResponsePojo.class);
				 User userResponse = new User(pojo.getData().getEmail(),pojo.getData().getFirstName(),pojo.getData().getLastName());
				 return new ResponseEntity<User>(userResponse,HttpStatus.OK);
				 //users.add(userResponse);
				
			}
			catch (HttpClientErrorException ex) {
				return ResponseEntity.status(ex.getRawStatusCode()).headers(ex.getResponseHeaders())
		                .body(ex.getResponseBodyAsString());     
			}
			catch(RestClientException ex2) {
				if(ex2.getCause() instanceof ConnectException) {
					System.out.println(ex2.getCause());
					return new ResponseEntity<String>("\"Could not connect to user web service\"",HttpStatus.SERVICE_UNAVAILABLE);
				}
				else if(ex2.getCause() instanceof UnknownHostException) {
					return new ResponseEntity<String>("\"Unable to connect to user web service\"", HttpStatus.SERVICE_UNAVAILABLE);
					}
				else {
					return new ResponseEntity<String>(ex2.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
		}
		
		
		
	}
	
	
	@PostMapping("/todos/{toDoNoteId}/users") // postinam nauja useri arba jau egzistuojanti kitame web servise
	public ResponseEntity<?> addUserToNote(@RequestBody User user,@PathVariable int toDoNoteId,UriComponentsBuilder builder) throws JsonParseException, JsonMappingException, ConnectException, IOException{
		ToDoNoteDTO noteDTO = toDoNoteService.getToDoNoteDTOById(toDoNoteId);
		if(noteDTO==null) {
			throw new ToDoNoteNotFoundException("Note with id "+ toDoNoteId + " not found");
			
		}
		String email = user.getEmail();
		final String uri = "http://friend:5000/users/"+email;
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> result =null;
		int statusCode=0;
		ObjectMapper mapper = new ObjectMapper();
		//ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);
		
		//return null;
		
		try { // If user exists we will just add it to our ToDoNote
			 result = restTemplate.getForEntity(uri, String.class);
			 ResponsePojo pojo = mapper.readValue(result.getBody(), ResponsePojo.class);
			 User userToRespond = new User(pojo.getData().getEmail(),pojo.getData().getFirstName(),pojo.getData().getLastName());
			 if(noteDTO.checkIfUserExists(user)==true) {
					return new ResponseEntity<String>("\"User with this email already exists in this note\"",HttpStatus.CONFLICT);
			 }
			 boolean match = checkIfUsersMatch(user,userToRespond);
			 if(match==false) { // putinam/updatinam kitam web service
				 HttpEntity<User> userEntity = new HttpEntity<User>(user);
				 result= restTemplate.exchange(uri, HttpMethod.PUT,userEntity,String.class);
			 }
			// ToDoNote toDoNote = toDoNoteService.getToDoNoteById(toDoNoteId);
			 	else {
			 		toDoNoteService.getToDoNoteDTOById(toDoNoteId).addUserEmail(userToRespond.getEmail());
			 		HttpHeaders headers = new HttpHeaders();
					headers.setLocation(builder.path("/todos/{id}/users/{email}").buildAndExpand(noteDTO.getId(),user.getEmail()).toUri());
			 		return new ResponseEntity<User>(user,headers,HttpStatus.CREATED);
			 	}

			//System.out.println("result" + result);
			//testing lag
			
		}
		
		catch (HttpClientErrorException ex) {
			System.out.println("value"+ ex.getStatusCode().value());
			statusCode=ex.getStatusCode().value();     
		}
		catch(RestClientException ex2) {
			if(ex2.getCause() instanceof ConnectException) {
				System.out.println(ex2.getCause());
				return new ResponseEntity<String>("\"Could not connect to user service\"",HttpStatus.SERVICE_UNAVAILABLE);
			}
			else if(ex2.getCause() instanceof UnknownHostException) {
				return new ResponseEntity<String>("\"Unable to connect to user web service\"", HttpStatus.SERVICE_UNAVAILABLE);
				}
			else {
				return new ResponseEntity<String>(ex2.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		
	
		
		
		try { // If user does not exist by the given email we can POST
			if(statusCode==404) {
				if(user.getEmail()==null || user.getFirstName()==null || user.getLastName()==null) {
					 return new ResponseEntity<String>("\"Required fields are missing( required fields are email,firstName,LastName)\"",HttpStatus.BAD_REQUEST);
				}
				final String uriPost = "http://friend:5000/users";
				result= restTemplate.postForEntity(uriPost, user, String.class);
				System.out.println("result" + result);
				
				ResponsePojo pojo = mapper.readValue(result.getBody(), ResponsePojo.class);
				User userToRespond = new User(pojo.getData().getEmail(),pojo.getData().getFirstName(),pojo.getData().getLastName());
				toDoNoteService.getToDoNoteDTOById(toDoNoteId).addUserEmail(userToRespond.getEmail());
				HttpHeaders headers = new HttpHeaders();
				headers.setLocation(builder.path("/todos/{id}/users/{email}").buildAndExpand(noteDTO.getId(),user.getEmail()).toUri()); // lygtais butu galima duoti ir user to respond
				return new ResponseEntity<User>(userToRespond,headers,HttpStatus.CREATED);
			}
		}
			catch (HttpClientErrorException ex) {
				System.out.println("value"+ ex.getStatusCode().value());
				return ResponseEntity.status(ex.getRawStatusCode()).headers(ex.getResponseHeaders())
		                .body(ex.getResponseBodyAsString());
			}
	// istryniau daug tu throws not sure if thats that good
		
		System.out.println("Rip");
		return null;
		
	}
	
	
	
	
	
	// remove user only from note
		@DeleteMapping("/todos/{toDoNoteId}/users/{email}")
		public ResponseEntity<User> deleteUser(@PathVariable int toDoNoteId,@PathVariable String email){
			ToDoNoteDTO todoDTO = toDoNoteService.getToDoNoteDTOById(toDoNoteId);
			
			if(todoDTO == null) {
				throw new ToDoNoteNotFoundException("Note with id "+ toDoNoteId + " not found. Cannot delete.");
			}
			
			boolean isPresent = toDoNoteService.isUserPresent(toDoNoteId, email);
			if(isPresent==false) {
				throw new ToDoNoteNotFoundException("User with email "+ email + " not found for this note"); // reiktu naujo exception kur UserNotFound
			}
			
			//ToDoNoteDTO  noteDto = convertToDto(todos);
			toDoNoteService.removeUser(toDoNoteId,email);
			return new ResponseEntity<User>(HttpStatus.NO_CONTENT);
		}
	
	
	
	
	@PostMapping("/todos") 
	public ResponseEntity<?> addNote(@RequestBody ToDoNote newNote, UriComponentsBuilder builder)throws HttpMessageNotReadableException, ParseException, JsonParseException, JsonMappingException, IOException{
		//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		ToDoNoteDTO noteDTO = convertToDto(newNote); // noteDto su tusciu email list so far
		
		if(newNote==null) {
			return ResponseEntity.noContent().build(); // cia reikia naujo exception ten body not found or smth
		}
		
		if(newNote.getUsers()!=null && checkIfThereAreDuplicates(newNote)==true) {
			return new ResponseEntity<String>("\"Please remove duplicate Users\"",HttpStatus.CONFLICT);
		}
		
		if(newNote.getDateToComplete()!=null && newNote.getDateToComplete().before(dateFormat.parse(dateFormat.format(new Date())))) {
			throw new InvalidFieldException("Invalid Date");
		}
		
		if(newNote.getUsers()==null) { // jeigu Useriai nepaduoti tiesiog sukuriam tuscia
			newNote.setUsers(new ArrayList<User>());
		}
		ArrayList<User> users = newNote.getUsers();
		// eisim per paduotus userius
		for(int i=0; i<users.size(); i++) {
			
			String email = users.get(i).getEmail();
			final String uri = "http://friend:5000/users/"+email;
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> result =null;
			int statusCode=0;
			ObjectMapper mapper = new ObjectMapper();
			User tempUser = users.get(i);
			
			try { // If user exists we will just add it to our ToDoNote
				 result = restTemplate.getForEntity(uri, String.class);
				 ResponsePojo pojo = mapper.readValue(result.getBody(), ResponsePojo.class);
				 User userToRespond = new User(pojo.getData().getEmail(),pojo.getData().getFirstName(),pojo.getData().getLastName());
				 boolean match = checkIfUsersMatch(tempUser,userToRespond);
				 if(match==false) { // putinam/updatinam kitam web service
					 HttpEntity<User> userEntity = new HttpEntity<User>(tempUser);
					 result= restTemplate.exchange(uri, HttpMethod.PUT,userEntity,String.class);
				 }
				// ToDoNote toDoNote = toDoNoteService.getToDoNoteById(toDoNoteId);
				 if(noteDTO.checkIfUserExists(tempUser)==true) { // turetu veikt kaip antiduplicate tik ofc meta error o ne ignorina duplicate
						return new ResponseEntity<String>("\"Please remove duplicate Users\"",HttpStatus.CONFLICT);
				 }
				 	else {
				 		noteDTO.addUserEmail(tempUser.getEmail());
				 		//toDoNoteService.getToDoNoteDTOById(toDoNoteId).addUserEmail(userToRespond.getEmail());
				 		//return new ResponseEntity<User>(userToRespond,HttpStatus.CREATED);
				 	}

				//System.out.println("result" + result);
				//testing lag
				
			}
			
			catch (HttpClientErrorException ex) {
				System.out.println("value"+ ex.getStatusCode().value());
				statusCode=ex.getStatusCode().value();     
			}
			catch(RestClientException ex2) {
				if(ex2.getCause() instanceof ConnectException) {
					System.out.println(ex2.getCause());
					return new ResponseEntity<String>("\"Could not connect to user webservice\"",HttpStatus.SERVICE_UNAVAILABLE);
				}
				else if(ex2.getCause() instanceof UnknownHostException) {
					return new ResponseEntity<String>("\"Unable to connect to user web service\"", HttpStatus.SERVICE_UNAVAILABLE);
					}
				else {
					return new ResponseEntity<String>(ex2.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
			
		
			
			
			try { // If user does not exist by the given email we can POST
				if(statusCode==404) {
					if(tempUser.getEmail()==null || tempUser.getFirstName()==null || tempUser.getLastName()==null) {
						 return new ResponseEntity<String>("\"Required fields are missing( required fields are email,firstName,LastName)\"",HttpStatus.BAD_REQUEST);
					}
					final String uriPost = "http://friend:5000/users";
					result= restTemplate.postForEntity(uriPost, tempUser, String.class);
					System.out.println("result" + result);
					
					ResponsePojo pojo = mapper.readValue(result.getBody(), ResponsePojo.class);
					User userToRespond = new User(pojo.getData().getEmail(),pojo.getData().getFirstName(),pojo.getData().getLastName());
					noteDTO.addUserEmail(userToRespond.getEmail());
					//toDoNoteService.getToDoNoteDTOById(toDoNoteId).addUserEmail(userToRespond.getEmail());
					//return new ResponseEntity<User>(userToRespond,HttpStatus.CREATED);
				}
			}
				catch (HttpClientErrorException ex) {
					System.out.println("value"+ ex.getStatusCode().value());
					return ResponseEntity.status(ex.getRawStatusCode()).headers(ex.getResponseHeaders())
			                .body(ex.getResponseBodyAsString());
				}
			
		}
		toDoNoteService.addToDoNoteDTO(noteDTO);
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(builder.path("/todos/{id}").queryParam("embed", "users").buildAndExpand(noteDTO.getId()).toUri());
		newNote.setId(noteDTO.getId()); // reikia nes laikome DTO objektus tai niekad newNote neuzsetins id
		return new ResponseEntity<ToDoNote>(newNote,headers, HttpStatus.CREATED);
		
		
		//return null;
	}
	
	
	@PutMapping("/todos/{id}")
	public ResponseEntity<?> updateToDoNote(@Valid @RequestBody ToDoNote note, @PathVariable int id) throws ParseException, JsonParseException, JsonMappingException, IOException{
		ToDoNoteDTO oldNoteDTO = toDoNoteService.getToDoNoteDTOById(id); // senas note su Emailais. Kadangi put tai koki paduos i toki ir pakeisim	
		//System.out.println("sizee "+toDoNoteService.getAllToDoNote().size());
		
		if(oldNoteDTO == null) {
			throw new ToDoNoteNotFoundException("Note with id "+ id + " not found");
		}
		
		if(note.getUsers()!=null && checkIfThereAreDuplicates(note)==true) {
			return new ResponseEntity<String>("\"Please remove duplicate Users\"",HttpStatus.CONFLICT);
		}
		
		if(note.getDateToComplete()!=null && note.getDateToComplete().before(dateFormat.parse(dateFormat.format(new Date())))) {
			throw new InvalidFieldException("Invalid Date");
		}
		
		//oldNoteDTO.setId(id);
		
		oldNoteDTO.setName(note.getName());
		oldNoteDTO.setDateToComplete(note.getDateToComplete());
		oldNoteDTO.setDescription(note.getDescription());
		oldNoteDTO.setPriority(note.getPriority());
		oldNoteDTO.setCompleted(note.isCompleted());
		oldNoteDTO.setEmails(new ArrayList<String>()); // nuimam senus useriu emailus
		
		
		if(note.getUsers()==null) { // jeigu Useriai nepaduoti tiesiog sukuriam tuscia
			note.setUsers(new ArrayList<User>());
		}
		ArrayList<User> users = note.getUsers();
		ArrayList<String> tempEmails = new ArrayList<String>();
		// eisim per paduotus userius
		for(int i=0; i<users.size(); i++) {
			
			String email = users.get(i).getEmail();
			final String uri = "http://friend:5000/users/"+email;
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> result =null;
			int statusCode=0;
			ObjectMapper mapper = new ObjectMapper();
			User tempUser = users.get(i);
			
			try { // If user exists we will just add it to our ToDoNote
				 result = restTemplate.getForEntity(uri, String.class);
				 ResponsePojo pojo = mapper.readValue(result.getBody(), ResponsePojo.class);
				 User userToRespond = new User(pojo.getData().getEmail(),pojo.getData().getFirstName(),pojo.getData().getLastName());
				 boolean match = checkIfUsersMatch(tempUser,userToRespond);
				 if(match==false) { // putinam/updatinam kitam web service
					 HttpEntity<User> userEntity = new HttpEntity<User>(tempUser);
					 result= restTemplate.exchange(uri, HttpMethod.PUT,userEntity,String.class);
				 }
				// ToDoNote toDoNote = toDoNoteService.getToDoNoteById(toDoNoteId);
				 if(oldNoteDTO.checkIfUserExists(tempUser)==true) { // turetu veikt kaip antiduplicate tik ofc meta error o ne ignorina duplicate
						return new ResponseEntity<String>("\"Please remove duplicate Users\"",HttpStatus.CONFLICT);
				 }
				 	else {
				 		tempEmails.add(tempUser.getEmail());
				 		///oldNoteDTO.addUserEmail(tempUser.getEmail());
				 		//toDoNoteService.getToDoNoteDTOById(toDoNoteId).addUserEmail(userToRespond.getEmail());
				 		//return new ResponseEntity<User>(userToRespond,HttpStatus.CREATED);
				 	}

				//System.out.println("result" + result);
				//testing lag
				
			}
			
			catch (HttpClientErrorException ex) {
				System.out.println("value"+ ex.getStatusCode().value());
				statusCode=ex.getStatusCode().value();     
			}
			catch(RestClientException ex2) {
				if(ex2.getCause() instanceof ConnectException) {
					System.out.println(ex2.getCause());
					return new ResponseEntity<String>("\"Could not connect to user web service\"",HttpStatus.SERVICE_UNAVAILABLE);
				}
				else if(ex2.getCause() instanceof UnknownHostException) {
					return new ResponseEntity<String>("\"Unable to connect to user web service\"", HttpStatus.SERVICE_UNAVAILABLE);
					}
				else {
					return new ResponseEntity<String>(ex2.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
			
			try { // If user does not exist by the given email we can POST
				if(statusCode==404) {
					if(tempUser.getEmail()==null || tempUser.getFirstName()==null || tempUser.getLastName()==null) {
						 return new ResponseEntity<String>("\"Required fields are missing( required fields are email,firstName,LastName)\"",HttpStatus.BAD_REQUEST);
					}
					final String uriPost = "http://friend:5000/users";
					result= restTemplate.postForEntity(uriPost, tempUser, String.class);
					System.out.println("result" + result);
					
					ResponsePojo pojo = mapper.readValue(result.getBody(), ResponsePojo.class);
					User userToRespond = new User(pojo.getData().getEmail(),pojo.getData().getFirstName(),pojo.getData().getLastName());
					tempEmails.add(userToRespond.getEmail());
					///oldNoteDTO.addUserEmail(userToRespond.getEmail());
					//toDoNoteService.getToDoNoteDTOById(toDoNoteId).addUserEmail(userToRespond.getEmail());
					//return new ResponseEntity<User>(userToRespond,HttpStatus.CREATED);
				}
			}
				catch (HttpClientErrorException ex) {
					System.out.println("value"+ ex.getStatusCode().value());
					return ResponseEntity.status(ex.getRawStatusCode()).headers(ex.getResponseHeaders())
			                .body(ex.getResponseBodyAsString());
				}
			
		}
		oldNoteDTO.setEmails(tempEmails);
		toDoNoteService.updateToDoNoteDTO(oldNoteDTO);
		//toDoNoteService.addToDoNoteDTO(noteDTO);
		//HttpHeaders headers = new HttpHeaders();
		//headers.setLocation(builder.path("/todos/{id}?embedded=true").buildAndExpand(noteDTO.getId()).toUri());
		
		
		
		
		
		
		note.setId(id);
		
		return new ResponseEntity<ToDoNote>(note, HttpStatus.OK);
	}
	
	
	@PatchMapping("/todos/{id}")
	public ResponseEntity<?> partlyUpdateToDoNote(@RequestBody ToDoNote note, @PathVariable int id) throws JsonParseException, JsonMappingException, IOException{
		ToDoNoteDTO oldNoteDTO = toDoNoteService.getToDoNoteDTOById(id);
	
		
		if(oldNoteDTO == null) {
			throw new ToDoNoteNotFoundException("Note with id "+ id + " not found");
		}
		if(note.getUsers()!=null && checkIfThereAreDuplicates(note)==true) {
			return new ResponseEntity<String>("\"Please remove duplicate Users\"",HttpStatus.CONFLICT);
		}
		note.setId(id);
		
		
		if(note.getName()!= null){
			oldNoteDTO.setName(note.getName());
		}
		else {
			note.setName(oldNoteDTO.getName());
		}
		
		if(note.getDateToComplete()!= null){
			oldNoteDTO.setDateToComplete(note.getDateToComplete());
		}
		else {
			note.setDateToComplete(oldNoteDTO.getDateToComplete());
		}
		
		if(note.getDescription()!= null){
			oldNoteDTO.setDescription(note.getDescription());
		}
		else {
			note.setDescription(oldNoteDTO.getDescription());
		}
		
		if(note.getPriority()!= null){
			oldNoteDTO.setPriority(note.getPriority());
		}
		else {
			note.setPriority(oldNoteDTO.getPriority());
		}
		
		if(note.isCompleted()!= null){
			oldNoteDTO.setCompleted(note.isCompleted());
		}
		else {
			note.setCompleted(oldNoteDTO.isCompleted());
		}
		
		if(note.getUsers()!=null) {
			oldNoteDTO.setEmails(new ArrayList<String>()); // jei kazkas paduota tai sena nuimam
			
			ArrayList<User> users = note.getUsers();
			//ArrayList<String> tempEmails = new ArrayList<String>();
			
			// eisim per paduotus userius
			for(int i=0; i<users.size(); i++) {
				
				String email = users.get(i).getEmail();
				final String uri = "http://friend:5000/users/"+email;
				RestTemplate restTemplate = new RestTemplate();
				ResponseEntity<String> result =null;
				int statusCode=0;
				ObjectMapper mapper = new ObjectMapper();
				User tempUser = users.get(i);
				
				try { // If user exists we will just add it to our ToDoNote
					 result = restTemplate.getForEntity(uri, String.class);
					 ResponsePojo pojo = mapper.readValue(result.getBody(), ResponsePojo.class);
					 User userToRespond = new User(pojo.getData().getEmail(),pojo.getData().getFirstName(),pojo.getData().getLastName());
					 boolean match = checkIfUsersMatch(tempUser,userToRespond);
					 if(match==false) { // putinam/updatinam kitam web service
						 HttpEntity<User> userEntity = new HttpEntity<User>(tempUser);
						 result= restTemplate.exchange(uri, HttpMethod.PUT,userEntity,String.class);
					 }
					// ToDoNote toDoNote = toDoNoteService.getToDoNoteById(toDoNoteId);
					 if(oldNoteDTO.checkIfUserExists(tempUser)==true) { // turetu veikt kaip antiduplicate tik ofc meta error o ne ignorina duplicate
							return new ResponseEntity<String>("\"Please remove duplicate Users\"",HttpStatus.CONFLICT);
					 }
					 	else {
					 		///tempEmails.add(tempUser.getEmail());
					 		oldNoteDTO.addUserEmail(tempUser.getEmail());
					 		//toDoNoteService.getToDoNoteDTOById(toDoNoteId).addUserEmail(userToRespond.getEmail());
					 		//return new ResponseEntity<User>(userToRespond,HttpStatus.CREATED);
					 	}

					//System.out.println("result" + result);
					//testing lag
					
				}
				
				catch (HttpClientErrorException ex) {
					System.out.println("value"+ ex.getStatusCode().value());
					statusCode=ex.getStatusCode().value();     
				}
				catch(RestClientException ex2) {
					if(ex2.getCause() instanceof ConnectException) {
						System.out.println(ex2.getCause());
						return new ResponseEntity<String>("\"Could not connect to user web service\"",HttpStatus.SERVICE_UNAVAILABLE);
					}
					else if(ex2.getCause() instanceof UnknownHostException) {
						return new ResponseEntity<String>("\"Unable to connect to user web service\"", HttpStatus.SERVICE_UNAVAILABLE);
						}
					else {
						return new ResponseEntity<String>(ex2.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}
				
			
				
				
				try { // If user does not exist by the given email we can POST
					if(statusCode==404) {
						if(tempUser.getEmail()==null || tempUser.getFirstName()==null || tempUser.getLastName()==null) {
							 return new ResponseEntity<String>("\"Required fields are missing( required fields are email,firstName,LastName)\"",HttpStatus.BAD_REQUEST);
						}
						final String uriPost = "http://friend:5000/users";
						result= restTemplate.postForEntity(uriPost, tempUser, String.class);
						System.out.println("result" + result);
						
						ResponsePojo pojo = mapper.readValue(result.getBody(), ResponsePojo.class);
						User userToRespond = new User(pojo.getData().getEmail(),pojo.getData().getFirstName(),pojo.getData().getLastName());
						///tempEmails.add(userToRespond.getEmail());
						oldNoteDTO.addUserEmail(userToRespond.getEmail());
						//toDoNoteService.getToDoNoteDTOById(toDoNoteId).addUserEmail(userToRespond.getEmail());
						//return new ResponseEntity<User>(userToRespond,HttpStatus.CREATED);
					}
				}
					catch (HttpClientErrorException ex) {
						System.out.println("value"+ ex.getStatusCode().value());
						return ResponseEntity.status(ex.getRawStatusCode()).headers(ex.getResponseHeaders())
				                .body(ex.getResponseBodyAsString());
					}
				
			}
			//oldNoteDTO.setEmails(tempEmails);
			toDoNoteService.updateToDoNoteDTO(oldNoteDTO);
			
		}
		
		else {
			note.setUsers(new ArrayList<User>());
			ArrayList<String> emails = oldNoteDTO.getUserEmails();
			for(int i=0; i< emails.size(); i++) {
				String currentEmail = emails.get(i);
				final String uri = "http://friend:5000/users/"+currentEmail;
				RestTemplate restTemplate = new RestTemplate();
				ResponseEntity<String> result =null;
				int statusCode=0;
				ObjectMapper mapper = new ObjectMapper();
				try {
					result = restTemplate.getForEntity(uri, String.class);
					 ResponsePojo pojo = mapper.readValue(result.getBody(), ResponsePojo.class);
					 User userToRespond = new User(pojo.getData().getEmail(),pojo.getData().getFirstName(),pojo.getData().getLastName());
					 note.addUser(userToRespond);
				}
				catch (HttpClientErrorException ex) {
					System.out.println("value"+ ex.getStatusCode().value());
					statusCode=ex.getStatusCode().value();     
				}
				catch(RestClientException ex2) {
					if(ex2.getCause() instanceof ConnectException) {
						System.out.println(ex2.getCause());
						return new ResponseEntity<String>("\"Could not connect to user web service\"",HttpStatus.SERVICE_UNAVAILABLE);
					}
					else if(ex2.getCause() instanceof UnknownHostException) {
						return new ResponseEntity<String>("\"Unable to connect to user web service\"", HttpStatus.SERVICE_UNAVAILABLE);
						}
					else {
						return new ResponseEntity<String>(ex2.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}
				
			}
		}
		
		
		toDoNoteService.updateToDoNoteDTO(oldNoteDTO);
		//noteDto = convertToDto(oldNote); 
		return new ResponseEntity<ToDoNote>(note, HttpStatus.OK);
	}	
	
	@DeleteMapping("/todos/{toDoNoteId}")
	public ResponseEntity<ToDoNoteDTO> deleteToDoNote(@PathVariable int toDoNoteId){
		ToDoNoteDTO todoDTO = toDoNoteService.getToDoNoteDTOById(toDoNoteId);
		
		if(todoDTO == null) {
			throw new ToDoNoteNotFoundException("Note with id "+ toDoNoteId + " not found. Cannot delete.");
		}
		//ToDoNoteDTO  noteDto = convertToDto(todos);
		toDoNoteService.deleteToDoNoteDTO(toDoNoteId);
		return new ResponseEntity<ToDoNoteDTO>(HttpStatus.NO_CONTENT);
	}
	
	
	
	private ToDoNoteDTO convertToDto(ToDoNote note) { // ideda ne userius o empty email ArrayList
	    ToDoNoteDTO noteDto = modelMapper.map(note, ToDoNoteDTO.class);
	    noteDto.setEmails(new ArrayList<String>());
	    return noteDto;
	}
	
	private ToDoNote convertToEntity(ToDoNoteDTO noteDto, boolean newNote) throws ParseException {
        ToDoNote note = modelMapper.map(noteDto, ToDoNote.class);
        if(newNote==true) {
        ArrayList<User> users= new ArrayList<User>();
        note.setUsers(users);
        return note;
        }
        
        return null;
       // else {
	     //   if (noteDto.getId() != null) {
	      //      ToDoNote oldNote = toDoNoteService.getToDoNoteById(noteDto.getId());
	       //     note.setUsers(oldNote.getUsers());
	          
	       // }
       // }
       // return note;
    }
	
	boolean checkIfUsersMatch(User user1, User user2) { // galimai reikes pakeisti
		if(user1.equals(user2)) {
			return true;
		}
		return false;
	}
	
	boolean checkIfThereAreDuplicates(ToDoNote note) {
		ArrayList<User> users = note.getUsers();
		List<String>emails = new ArrayList<String>();
		for(int i=0; i<users.size();i++) {
			emails.add(users.get(i).getEmail());
		}
		Set<String> set = new HashSet<String>();
		for(String each: emails) {
			if(!set.add(each)) {
				return true;
			}
		}
		return false;
	}
}


