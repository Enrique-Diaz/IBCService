package com.example.ibc.controller;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ibc.dto.RequestDTO;
import com.example.ibc.dto.RequestOrderDTO;
import com.example.ibc.dto.ResponseDTO;
import com.example.ibc.exception.ServiceException;
import com.example.ibc.service.ProcessorServiceImpl;
import com.example.ibc.utils.IBCConstants;

@RestController
@RequestMapping("/process")
public class ProcessorController {

	@Autowired
	private Logger logger;
	
	@Autowired
	private IBCConstants ibcConstants;
	
	@Autowired
	private ProcessorServiceImpl processorServiceImpl;
	
	/**
     * Process Orders BUY/SELL
     *
     * @RequestBody orderDTO
     * 
     * @return responseObject
	 * @throws ServiceException 
     */
	@PostMapping("/order")
	public ResponseEntity<?> processOrder(@RequestBody RequestOrderDTO requestOrderDTO) throws ServiceException {
		logger.info("Entering Controller layer at processOrder");

		ResponseEntity<?> response;
		ResponseDTO responseDTO = new ResponseDTO();
		
		if (requestOrderDTO == null) {
			logger.info("NOT PROCESSING ORDER");
			response = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} else if (isMarketOpen(requestOrderDTO.getTimeStamp())) {
			logger.info("PROCESSING ORDER: " + requestOrderDTO.getIssuerName());
			responseDTO.setCurrentBalance(processorServiceImpl.processOrderWhileOpenMarket(requestOrderDTO));
			response = new ResponseEntity<>(responseDTO, HttpStatus.OK);
		} else {
			logger.info("NOT PROCESSING ORDER");
			responseDTO.setCurrentBalance(processorServiceImpl.getBalance());
			responseDTO.getBusinessErrors().add(ibcConstants.MARKET_CLOSED);
			response = new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
		}
		logger.info("Leaving Controller layer at processOrder");
		return response;
	}
	
	/**
     * Process Initial Balance
     *
     * @RequestBody orderDTO
     * 
     * @return responseObject
	 * @throws ServiceException 
     */
	@PutMapping("/balance")
	public ResponseEntity<?> processInitialBalance(@RequestBody RequestDTO requestDTO) throws ServiceException {
		logger.info("Entering Controller layer at processInitialBalance");

		ResponseEntity<?> response;
		
		// Validate if the request is not null
		if (requestDTO == null) {
			response = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} else {
			processorServiceImpl.processInitialBalances(requestDTO);
			response = new ResponseEntity<>(HttpStatus.CREATED);
		}
		
		logger.info("Leaving Controller layer at processInitialBalance");
		return response;
	}
	
	/**
     * Get Balances
     *
     * @return responseObject
     */
	@GetMapping("/balances")
	public ResponseEntity<?> getBalances() {
		logger.info("Entering Controller layer at getBalances");
		
		logger.info("Leaving Controller layer at getBalances");
		return new ResponseEntity<>(processorServiceImpl.getBalances(), HttpStatus.OK);
	}
	
	/**
	 * Method to validate if the market is open from 6am to 3pm
	 * */
	public boolean isMarketOpen(Long timeStamp) {
		boolean isOpen = false;
		
		LocalDateTime localDateTimeNoTimeZone = new Timestamp(timeStamp).toLocalDateTime();
		ZonedDateTime currentTime = localDateTimeNoTimeZone.atZone(ZoneId.of(ibcConstants.TIME_ZONE));
		
		ZonedDateTime marketOpen = currentTime.withHour(ibcConstants.OPEN_HOUR).withMinute(ibcConstants.ZERO).withSecond(ibcConstants.ZERO);
		ZonedDateTime marketClose = currentTime.withHour(ibcConstants.CLOSE_HOUR).withMinute(ibcConstants.CLOSE_MINUTE).withSecond(ibcConstants.CLOSE_SECOND);
		
		if(currentTime.compareTo(marketOpen) > ibcConstants.ZERO && currentTime.compareTo(marketClose) < ibcConstants.ZERO) {
			isOpen = true;
		}
		
		return isOpen;
	}
}