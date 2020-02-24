package com.example.ibc.service;

import java.util.List;
import java.util.Map;

import com.example.ibc.dto.BalanceDTO;
import com.example.ibc.dto.RequestDTO;
import com.example.ibc.dto.RequestOrderDTO;
import com.example.ibc.model.Balance;

public interface ProcessorServiceImpl {

	public BalanceDTO processOrderWhileOpenMarket(RequestOrderDTO requestOrderDTO);
	
	public void processInitialBalances(RequestDTO requestDTO);
	
	public Map<String, BalanceDTO> getMap();
	
	public List<Balance> getBalances();
}