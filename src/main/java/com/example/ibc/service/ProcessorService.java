package com.example.ibc.service;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.ibc.dao.BalanceRepository;
import com.example.ibc.dto.BalanceDTO;
import com.example.ibc.dto.IssuerDTO;
import com.example.ibc.dto.Operation;
import com.example.ibc.dto.RequestDTO;
import com.example.ibc.dto.RequestOrderDTO;
import com.example.ibc.model.Balance;
import com.example.ibc.model.Issuer;
import com.google.common.reflect.TypeToken;

@Service
public class ProcessorService implements ProcessorServiceImpl{

	@Autowired
	private Logger logger;
	
	@Autowired
	private Map<String, BalanceDTO> cacheMap;
	
	@Autowired
	private BalanceRepository balanceRepository;
	
	// Used for the cache as key
	private final String DUMMY_USER = "user";
	
	// Used for DB
	private final int DUMMY_ID = 1;

	// ModelMapper used to convert from DTO and Entity as needed
	private ModelMapper modelMapper = new ModelMapper();
	
	@SuppressWarnings("serial")
	@Override
	public BalanceDTO processOrderWhileOpenMarket(RequestOrderDTO requestOrderDTO) {
		logger.info("At processOrderWhileOpenMarket, PROCESSING ORDER: " + requestOrderDTO.getIssuerName() + " ACTION: " + requestOrderDTO.getOperation());
		
//		To use cacheMap
//		if (cacheMap.containsKey(DUMMY_USER)) {
//			BalanceDTO balance = cacheMap.get(requestOrderDTO.getIssuerName());
//			IssuerDTO issuer = balance.getIssuers().get(0);
//			
//			issuer.setTotalShares(requestOrderDTO.getOperation()==Operation.BUY ? 
//					issuer.getTotalShares() + requestOrderDTO.getTotalShares() : 
//					issuer.getTotalShares() - requestOrderDTO.getTotalShares());
//			issuer.setSharePrice(requestOrderDTO.getSharePrice());
//			
//			balance.setCash(requestOrderDTO.getSharePrice() * issuer.getTotalShares());
//		} else {
//			logger.info("INVALID_OPERATION");
//		}
		
//		To use DB
		Optional<Balance> optionalBalance = balanceRepository.findById(DUMMY_ID);
		
		BalanceDTO responseBalanceDTO = new BalanceDTO();
		
		if (optionalBalance.isPresent()) {
			Balance responseBalance = new Balance();
			Balance balance = optionalBalance.get();
			Issuer issuer = balance.getIssuer().get(0);

			if (requestOrderDTO.getOperation()==Operation.BUY) {
				float amount = requestOrderDTO.getTotalShares() * requestOrderDTO.getSharePrice();
				
				if (amount <= balance.getCash()) {
					issuer.setTotalShares(issuer.getTotalShares() + requestOrderDTO.getTotalShares());
					issuer.setSharePrice(requestOrderDTO.getSharePrice());
					balance.setCash(balance.getCash() - amount);
					
					responseBalance = balanceRepository.save(balance);
				} else {
					logger.info("INVALID_OPERATION");
				}
			} else {
				float amount = requestOrderDTO.getTotalShares() * requestOrderDTO.getSharePrice();
				
				if (requestOrderDTO.getTotalShares() <= issuer.getTotalShares()) {
					issuer.setTotalShares(issuer.getTotalShares() - requestOrderDTO.getTotalShares());
					issuer.setSharePrice(requestOrderDTO.getSharePrice());
					balance.setCash(balance.getCash() + amount);
					
					responseBalance = balanceRepository.save(balance);
				} else {
					logger.info("INVALID_OPERATION");
				}
			}
			
			Type listType = new TypeToken<List<IssuerDTO>>() {}.getType();
			List<IssuerDTO> issuersDTO = modelMapper.map(responseBalance.getIssuer(), listType, "issuerdto-list");
			
			responseBalanceDTO = modelMapper.map(balance, BalanceDTO.class);
			responseBalanceDTO.setIssuers(issuersDTO);
		} else {
			logger.info("INVALID_OPERATION");
		}
		
		logger.info("Leaving processOrderWhileOpenMarket");
		return responseBalanceDTO;
	}

	@Override
	@SuppressWarnings("serial")
	public void processInitialBalances(RequestDTO requestDTO) {
		logger.info("At processInitialBalances");
		
		// Validate if the initial balance is not null
		if (requestDTO.getInitialBalances() == null) {
			// TODO
		} else {
			// Either use a cacheMap or Entity
			//cacheMap.put(DUMMY_USER, requestDTO.getInitialBalances());
			
			// Convert from DTO to Entity
			Balance balance = modelMapper.map(requestDTO.getInitialBalances(), Balance.class);
			
			// Create a Type to let know the mapper the type of object to convert
			// And set it directly to the balance Entity
			// Type listType = new TypeToken<List<Issuer>>() {}.getType();
			// balance.setIssuer(modelMapper.map(requestDTO.getInitialBalances().getIssuers(), listType, "issuer-list"));

			// To work only with the first id at this moment.
			balance.setId(DUMMY_ID);
			Type listType = new TypeToken<List<Issuer>>() {}.getType();
			List<Issuer> issuer = modelMapper.map(requestDTO.getInitialBalances().getIssuers(), listType, "issuer-list");
			issuer.get(0).setId(DUMMY_ID);
			balance.setIssuer(issuer);
						
			// Save balance entity and childs (issuers)
			balance = balanceRepository.save(balance);
		}
		
		logger.info("Leaving processInitialBalances");
	}

	@Override
	public Map<String, BalanceDTO> getMap() {
		return cacheMap;
	}

	@Override
	public List<Balance> getBalances() {
		
		return (List<Balance>) balanceRepository.findAll();
	}
}