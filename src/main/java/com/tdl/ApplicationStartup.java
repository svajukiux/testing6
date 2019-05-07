package com.tdl;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

	@Override
	  public void onApplicationEvent(final ApplicationReadyEvent event) {
	 
		RestTemplate restTemplate = new RestTemplate();
		//final String uri = "http://friend:5000/";
		
		try {
			//ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class); 
			return;
			
		}
		catch (HttpClientErrorException ex) {
			return;
		     
		}
	 
	    //return;
	  }
}
