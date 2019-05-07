package com.tdl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.modelmapper.ModelMapper;
//import org.springframework.hateoas.config.EnableHypermediaSupport;

//@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@SpringBootApplication
public class SpringWebServiceToDoListApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringWebServiceToDoListApplication.class, args);
	}
	
	@Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
	

}
