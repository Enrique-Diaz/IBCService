package com.example.ibc.exception;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.example.ibc.dto.ResponseDTO;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ExceptionHandlerController extends ResponseEntityExceptionHandler{

	@Autowired
	private Logger logger;
	
	/*
	 * To handle checked exceptions and business exceptions
	 * */
	@ExceptionHandler(ServiceException.class)
	public ResponseEntity<?> apiException(ServiceException apiException){
		ResponseDTO responseDTO = new ResponseDTO();
		List<String> businessList = new ArrayList<>();
		businessList.add(apiException.getMessage());
		
		responseDTO.setCurrentBalance(apiException.getBalanceDTO());
		responseDTO.setBusinessErrors(businessList);
		
		logger.error("apiException: ", apiException);
		return new ResponseEntity<>(responseDTO, apiException.getHttpStatusCode());
	}
	
	/*
	 * to handle unchecked exceptions like;
	 * IndexOutOfBoundsException, IllegalArgumentException
	 * NumberFormatException, NullPointerException...
	 * */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> uncheckedException(Exception exception){
		ResponseDTO responseDTO = new ResponseDTO();
		List<String> businessList = new ArrayList<>();
		businessList.add(exception.getMessage());
		
		responseDTO.setBusinessErrors(businessList);
		
		logger.error("uncheckedException", exception);
		return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}