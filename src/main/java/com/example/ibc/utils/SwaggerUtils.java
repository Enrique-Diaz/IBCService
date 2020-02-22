package com.example.ibc.utils;

import java.util.Collections;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

public class SwaggerUtils {
	
	static SwaggerUtils instance = null;
	
	SwaggerUtils() { }
	
	public static SwaggerUtils getInstance() {
		if (instance == null) {
			instance = new SwaggerUtils();
		}
		return instance;
	}
	
	public Docket getSwaggerDocket(String appName, String description) {
		Docket docket = new Docket(DocumentationType.SWAGGER_2);
		
		return docket.select()                                  
                .apis(RequestHandlerSelectors.basePackage("com.example"))              
                .paths(PathSelectors.any())                          
                .build()
                .apiInfo(apiInfo(appName, description));
	}
	
    private ApiInfo apiInfo(String appName, String description) {
        return new ApiInfo(
          appName, 
          description, 
          "0.0.1-SNAPSHOT", 
          "Terms of service URL", 
          new Contact("Enrique Diaz", "www.ibc.com", "jd_0005@hotmail.com"), 
          "License of API", "API license URL", Collections.emptyList());
    }
}