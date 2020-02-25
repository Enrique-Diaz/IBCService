package com.example.ibc.service;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.ibc.dao.BalanceRepository;
import com.example.ibc.dao.IssuerRepository;
import com.example.ibc.dto.BalanceDTO;
import com.example.ibc.dto.IssuerDTO;
import com.example.ibc.dto.Operation;
import com.example.ibc.dto.RequestDTO;
import com.example.ibc.dto.RequestOrderDTO;
import com.example.ibc.exception.ServiceException;
import com.example.ibc.model.Balance;
import com.example.ibc.model.Issuer;
import com.example.ibc.utils.IBCConstants;
import com.google.common.reflect.TypeToken;

@Service
public class ProcessorServiceImpl implements ProcessorService{

	@Autowired
	private Logger logger;
	
	@Autowired
	private IBCConstants ibcConstants;
	
	@Autowired
	private BalanceRepository balanceRepository;
	
	@Autowired
	private IssuerRepository issuerRepository;
	
	// Used for DB
	private final int DUMMY_ID = 1;
	
	// Used to check if 5 minutes has elapsed since last operation
	private final int FIVE_MINUTES = 5 * 60 * 1000;
	
	// ModelMapper used to convert from DTO and Entity as needed
	private ModelMapper modelMapper = new ModelMapper();
	
	/**
	 * Method that process orders (buy/sell)
	 * 
	 * @param requestOrderDTO
	 * 
	 * @return responseBalanceDTO
	 * */
	@SuppressWarnings("serial")
	@Override
	public BalanceDTO processOrderWhileOpenMarket(RequestOrderDTO requestOrderDTO) throws ServiceException {
		logger.info("At processOrderWhileOpenMarket, PROCESSING ORDER: " + requestOrderDTO.getIssuerName() + " ACTION: " + requestOrderDTO.getOperation());
		
		// Get Balance from DB using Dummy
		Optional<Balance> optionalBalance = balanceRepository.findById(DUMMY_ID);
		
		BalanceDTO responseBalanceDTO = new BalanceDTO();
		
		// Validate if the balance is present from the DB
		if (optionalBalance.isPresent()) {
			Balance responseBalance = new Balance();
			Balance balance = optionalBalance.get();
			
			// Validate if the Issuer is empty; throw INVALID_OPERATION if so.
			if (balance.getIssuer().isEmpty()) {
				logger.info(ibcConstants.INVALID_OPERATION);
				throw new ServiceException(ibcConstants.INVALID_OPERATION, getBalance(), HttpStatus.BAD_REQUEST);
			} else {
				Issuer issuer = balance.getIssuer().get(ibcConstants.ZERO);

				// Validate operation SELL or BUY
				if (requestOrderDTO.getOperation() == Operation.BUY && !isDuplicated(requestOrderDTO.getTimeStamp(), Operation.BUY)) {
					responseBalance = buyOrder(requestOrderDTO, issuer, balance);
				} else if (requestOrderDTO.getOperation() == Operation.SELL && !isDuplicated(requestOrderDTO.getTimeStamp(), Operation.SELL)) {
					responseBalance = sellOrder(requestOrderDTO, issuer, balance);
				} else {
					logger.info(ibcConstants.DUPLICATED_OPERATION);
					throw new ServiceException(ibcConstants.DUPLICATED_OPERATION, getBalance(), HttpStatus.BAD_REQUEST);
				}
				Type listType = new TypeToken<List<IssuerDTO>>() {}.getType();
				List<IssuerDTO> issuersDTO = modelMapper.map(responseBalance.getIssuer(), listType, "issuerdto-list");
				
				responseBalanceDTO = modelMapper.map(balance, BalanceDTO.class);
				responseBalanceDTO.setIssuers(issuersDTO);
			}
		} else {
			logger.info(ibcConstants.INVALID_OPERATION);
			throw new ServiceException(ibcConstants.INVALID_OPERATION, getBalance(), HttpStatus.BAD_REQUEST);
		}
		
		logger.info("Leaving processOrderWhileOpenMarket");
		return responseBalanceDTO;
	}

	@Override
	@SuppressWarnings("serial")
	public void processInitialBalances(RequestDTO requestDTO) throws ServiceException {
		logger.info("At processInitialBalances");
		
		// Validate if the initial balance is not null
		if (requestDTO.getInitialBalances() == null) {
			logger.info(ibcConstants.INVALID_OPERATION);
			throw new ServiceException(ibcConstants.INVALID_OPERATION, getBalance(), HttpStatus.BAD_REQUEST);
		} else {
			// Convert from DTO to Entity
			Balance balance = modelMapper.map(requestDTO.getInitialBalances(), Balance.class);
			
			// To work only with the first id at this moment.
			balance.setId(DUMMY_ID);
			Type listType = new TypeToken<List<Issuer>>() {}.getType();
			List<Issuer> issuers = modelMapper.map(requestDTO.getInitialBalances().getIssuers(), listType, "issuer-list");
			
			if (!issuers.isEmpty()) {
				issuers.get(ibcConstants.ZERO).setId(DUMMY_ID);
			}
			
			balance.setIssuer(issuers);
						
			// Save balance entity and childs (issuers)
			balance = balanceRepository.save(balance);
		}
		
		logger.info("Leaving processInitialBalances");
	}

	@Override
	public List<Balance> getBalances() {
		
		return (List<Balance>) balanceRepository.findAll();
	}
	
	@Override
	@SuppressWarnings("serial")
	public BalanceDTO getBalance() {
		
		BalanceDTO responseBalanceDTO;
		Balance balance = balanceRepository.findById(DUMMY_ID).get();
		
		Type listType = new TypeToken<List<IssuerDTO>>() {}.getType();
		List<IssuerDTO> issuersDTO = modelMapper.map(balance.getIssuer(), listType, "issuerdto-list");
		
		responseBalanceDTO = modelMapper.map(balance, BalanceDTO.class);
		responseBalanceDTO.setIssuers(issuersDTO);
		
		return responseBalanceDTO;
	}
	
	public Balance buyOrder(RequestOrderDTO requestOrderDTO, Issuer issuer, Balance balance) throws ServiceException {
		Balance responseBalance = new Balance();
		float amount = requestOrderDTO.getTotalShares() * requestOrderDTO.getSharePrice();
		
		if (amount <= balance.getCash()) {
			issuer.setTotalShares(issuer.getTotalShares() + requestOrderDTO.getTotalShares());
			issuer.setSharePrice(requestOrderDTO.getSharePrice());
			issuer.setLastBuy(requestOrderDTO.getTimeStamp());
			balance.setCash(balance.getCash() - amount);
			
			responseBalance = balanceRepository.save(balance);
		} else {
			logger.info(ibcConstants.INSUFFICIENT_BALANCE);
			throw new ServiceException(ibcConstants.INSUFFICIENT_BALANCE, getBalance(), HttpStatus.BAD_REQUEST);
		}
		
		return responseBalance;
	}
	
	public Balance sellOrder(RequestOrderDTO requestOrderDTO, Issuer issuer, Balance balance) throws ServiceException {
		Balance responseBalance = new Balance();
		float amount = requestOrderDTO.getTotalShares() * requestOrderDTO.getSharePrice();
		
		if (requestOrderDTO.getTotalShares() <= issuer.getTotalShares()) {
			issuer.setTotalShares(issuer.getTotalShares() - requestOrderDTO.getTotalShares());
			issuer.setSharePrice(requestOrderDTO.getSharePrice());
			issuer.setLastSell(requestOrderDTO.getTimeStamp());
			balance.setCash(balance.getCash() + amount);
			
			responseBalance = balanceRepository.save(balance);
		} else {
			logger.info(ibcConstants.INSUFFICIENT_STOCKS);
			throw new ServiceException(ibcConstants.INSUFFICIENT_STOCKS, getBalance(), HttpStatus.BAD_REQUEST);
		}
		
		return responseBalance;
	}
	
	/**
	 * Method to validate if the operation is duplicated
	 * */
	public boolean isDuplicated(Long timeStamp, Operation operation) {
		boolean isDuplicated = true;
		
		Issuer issuer = issuerRepository.findById(DUMMY_ID).get();
		
		if (operation == Operation.BUY) {
			if ((issuer.getLastBuy() + FIVE_MINUTES) < timeStamp) {
				isDuplicated = false;
				logger.info("timeStamp is older than lastBuy by 5+ minutes");
			}
		} else {
			if ((issuer.getLastSell() + FIVE_MINUTES) < timeStamp) {
				isDuplicated = false;
				logger.info("timeStamp is older than lastSell by 5+ minutes");
			}
		}
		
		return isDuplicated;
	}
}