package com.example.ibc.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RequestOrderDTO implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private Long timeStamp;
	private Operation operation;
	private String issuerName;
	private int totalShares;
	private float sharePrice;
}