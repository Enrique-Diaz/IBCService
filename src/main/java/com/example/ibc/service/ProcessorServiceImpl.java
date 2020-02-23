package com.example.ibc.service;

import java.util.Map;

import com.example.ibc.dto.BalanceDTO;
import com.example.ibc.dto.RequestDTO;
import com.example.ibc.dto.RequestOrderDTO;

public interface ProcessorServiceImpl {

	public BalanceDTO processOrderWhileOpenMarket(RequestOrderDTO requestOrderDTO);
	
	public BalanceDTO processInitialBalance(RequestDTO requestDTO);
	
	public Map<String, BalanceDTO> getMap();
}