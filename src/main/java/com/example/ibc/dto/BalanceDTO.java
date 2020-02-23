package com.example.ibc.dto;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BalanceDTO implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private float cash;
	private ArrayList<IssuerDTO> issuers;
	
	public BalanceDTO() {
		this.issuers = new ArrayList<>();
	}
}