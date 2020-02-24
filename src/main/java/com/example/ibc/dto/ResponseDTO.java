package com.example.ibc.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private BalanceDTO currentBalance;
	private List<String> businessErrors;
	
	public ResponseDTO() {
		this.businessErrors = new ArrayList<>();
	}
}