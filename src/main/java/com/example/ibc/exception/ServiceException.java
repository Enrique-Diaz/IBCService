package com.example.ibc.exception;

import org.springframework.http.HttpStatus;

import com.example.ibc.dto.BalanceDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceException extends Exception{

	private static final long serialVersionUID = 1L;
	
	private String statusMessage;
	private HttpStatus httpStatusCode;
	private BalanceDTO balanceDTO;

	/*
	 * This method sets a custom message as the RuntimeException message
	 * */
	public ServiceException(String statusMessage, BalanceDTO balanceDTO, HttpStatus httpStatusCode) {
		super(statusMessage);
		this.statusMessage = statusMessage;
		this.httpStatusCode = httpStatusCode;
		this.balanceDTO = balanceDTO;
	}
	
	/*
	 * This method sets a third party message as the RuntimeException message
	 * */
	public ServiceException(String message, String statusMessage, HttpStatus httpStatusCode) {
		super(message);
		this.statusMessage = statusMessage;
	}
}