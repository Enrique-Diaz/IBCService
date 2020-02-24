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
	
	// Used for the cache as key
	private final String DUMMY_USER = "user";
	
	@Override
	public BalanceDTO processOrderWhileOpenMarket(RequestOrderDTO requestOrderDTO) {
		logger.info("At processOrderWhileOpenMarket, PROCESSING ORDER: " + requestOrderDTO.getIssuerName() + " ACTION: " + requestOrderDTO.getOperation());

		BalanceDTO responseBalanceDTO = new BalanceDTO();
		if (cacheMap.containsKey(DUMMY_USER)) {
			BalanceDTO balanceDTO = cacheMap.get(DUMMY_USER);
			IssuerDTO issuerDTO = balanceDTO.getIssuers().get(0);

			if (requestOrderDTO.getOperation()==Operation.BUY) {
				float amount = requestOrderDTO.getTotalShares() * requestOrderDTO.getSharePrice();
				
				if (amount <= balanceDTO.getCash()) {
					issuerDTO.setTotalShares(issuerDTO.getTotalShares() + requestOrderDTO.getTotalShares());
					issuerDTO.setSharePrice(requestOrderDTO.getSharePrice());
					balanceDTO.setCash(balanceDTO.getCash() - amount);
					
					cacheMap.put(DUMMY_USER, balanceDTO);
					responseBalanceDTO = balanceDTO;
				} else {
					logger.info("INVALID_OPERATION");
				}
			} else {
				float amount = requestOrderDTO.getTotalShares() * requestOrderDTO.getSharePrice();
				
				if (requestOrderDTO.getTotalShares() <= issuerDTO.getTotalShares()) {
					issuerDTO.setTotalShares(issuerDTO.getTotalShares() - requestOrderDTO.getTotalShares());
					issuerDTO.setSharePrice(requestOrderDTO.getSharePrice());
					balanceDTO.setCash(balanceDTO.getCash() + amount);
					
					cacheMap.put(DUMMY_USER, balanceDTO);
					responseBalanceDTO = balanceDTO;
				} else {
					logger.info("INVALID_OPERATION");
				}
			}
		} else {
			logger.info("INVALID_OPERATION");
		}
		
		logger.info("Leaving processOrderWhileOpenMarket");
		return responseBalanceDTO;
	}

	@Override
	public void processInitialBalances(RequestDTO requestDTO) {
		logger.info("At processInitialBalances");
		
		cacheMap.put(DUMMY_USER, requestDTO.getInitialBalances());
			
		logger.info("Leaving processInitialBalances");
	}

	@Override
	public Map<String, BalanceDTO> getMap() {
		return cacheMap;
	}
}