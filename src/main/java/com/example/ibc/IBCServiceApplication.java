package com.example.ibc;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import com.example.ibc.dto.BalanceDTO;
import com.example.ibc.utils.SwaggerUtils;

import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class IBCServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(IBCServiceApplication.class, args);
	}
	
	@Bean
    @Autowired
	public Docket api(ServletContext servletContext, Environment env) {
		return SwaggerUtils.getInstance().getSwaggerDocket("IBC Service",
				"This service handles Investment Bank Console requests");
	}
	
	@Bean
	public Map<String, BalanceDTO> cacheMap(){
		return new HashMap<>();
	}
}