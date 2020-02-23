package com.example.ibc.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private BalanceDTO initialBalance;
}