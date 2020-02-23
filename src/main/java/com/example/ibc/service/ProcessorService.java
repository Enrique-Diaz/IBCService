package com.example.ibc.service;

import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.ibc.dto.BalanceDTO;
import com.example.ibc.dto.IssuerDTO;
import com.example.ibc.dto.Operation;
import com.example.ibc.dto.RequestDTO;
import com.example.ibc.dto.RequestOrderDTO;

@Service
public class ProcessorService implements ProcessorServiceImpl{

	@Autowired
	private Logger logger;
	
	@Autowired
	private Map<String, BalanceDTO> cacheMap;
	
	@Override
	public BalanceDTO processOrderWhileOpenMarket(RequestOrderDTO requestOrderDTO) {
		logger.info("At processOrderWhileOpenMarket, PROCESSING ORDER: " + requestOrderDTO.getIssuerName() + " ACTION: " + requestOrderDTO.getOperation());
		
		BalanceDTO balance = cacheMap.get(requestOrderDTO.getIssuerName());
		
		IssuerDTO issuer = balance.getIssuers().get(0);
		
		issuer.setTotalShares(requestOrderDTO.getOperation()==Operation.BUY ? 
				issuer.getTotalShares() + requestOrderDTO.getTotalShares() : 
				issuer.getTotalShares() - requestOrderDTO.getTotalShares());
		issuer.setSharePrice(requestOrderDTO.getSharePrice());
		
		balance.setCash(requestOrderDTO.getSharePrice() * issuer.getTotalShares());
		
		logger.info("Leaving processOrderWhileOpenMarket");
		return balance;
	}

	@Override
	public BalanceDTO processInitialBalance(RequestDTO requestDTO) {
		logger.info("At processInitialBalance");
		
		cacheMap.put(requestDTO.getInitialBalance().getIssuers().get(0).getIssuerName(), requestDTO.getInitialBalance());
		
		logger.info("Leaving processInitialBalance");
		return null;
	}

	@Override
	public Map<String, BalanceDTO> getMap() {
		return cacheMap;
	}
}