package com.example.ibc.service;

import java.util.List;

import com.example.ibc.dto.BalanceDTO;
import com.example.ibc.dto.RequestDTO;
import com.example.ibc.dto.RequestOrderDTO;
import com.example.ibc.exception.ServiceException;
import com.example.ibc.model.Balance;

public interface ProcessorService {

	public BalanceDTO processOrderWhileOpenMarket(RequestOrderDTO requestOrderDTO) throws ServiceException;
	
	public void processInitialBalances(RequestDTO requestDTO) throws ServiceException;
	
	public List<Balance> getBalances();
	
	public BalanceDTO getBalance();
}