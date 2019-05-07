package com.tdl.service;

import java.util.List;

import com.tdl.model.*;

public interface ToDoNoteService {
	
	public List<ToDoNoteDTO> getAllToDoNoteDTO();
	
	public ToDoNoteDTO getToDoNoteDTOById(Integer id);
	
	public ToDoNoteDTO addToDoNoteDTO(ToDoNoteDTO toDoNote);
	
	public void updateToDoNoteDTO(ToDoNoteDTO toDoNote);
	
	public void deleteToDoNoteDTO(Integer id);
	
	public List<String> getAllNotesUsers(ToDoNoteDTO toDoNote);
}
