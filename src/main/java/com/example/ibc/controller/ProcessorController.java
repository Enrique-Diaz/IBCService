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
import com.example.ibc.service.ProcessorService;

@RestController
@RequestMapping("/process")
public class ProcessorController {

	@Autowired
	private Logger logger;
	
	@Autowired
	private ProcessorService processOrderService;
	
	/**
     * Process Orders BUY/SELL
     *
     * @RequestBody orderDTO
     * 
     * @return responseObject
     */
	@PostMapping("/order")
	public ResponseEntity<?> processOrder(@RequestBody RequestOrderDTO orderDTO) {
		logger.info("Entering Controller layer at processOrder, issuerName to process:{}", orderDTO.getIssuerName());

		ResponseDTO responseDTO = new ResponseDTO();
		
		if (orderDTO != null && isMarketOpen(orderDTO.getTimeStamp())) {
			logger.info("PROCESSING ORDER: " + orderDTO.getIssuerName());
			responseDTO.setCurrentBalance(processOrderService.processOrderWhileOpenMarket(orderDTO));
		} else {
			logger.info("NOT PROCESSING ORDER: " + orderDTO.getIssuerName());
			responseDTO.setCurrentBalance(processOrderService.getMap().get(orderDTO.getIssuerName()));
			responseDTO.getBussinessErrors().add("INVALID_OPERATION");
		}
		
		logger.info("Leaving Controller layer at processOrder");
		return new ResponseEntity<>(responseDTO, HttpStatus.OK);
	}
	
	/**
     * Process Initial Balance
     *
     * @RequestBody orderDTO
     * 
     * @return responseObject
     */
	@PutMapping("/balance")
	public ResponseEntity<?> processInitialBalance(@RequestBody RequestDTO requestDTO) {
		logger.info("Entering Controller layer at processInitialBalance");

		ResponseEntity<?> response;
		
		// Validate if the request is not null
		if (requestDTO == null) {
			response = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} else {
			processOrderService.processInitialBalances(requestDTO);
			response = new ResponseEntity<>(HttpStatus.CREATED);
		}
		
		logger.info("Leaving Controller layer at processInitialBalance");
		return response;
	}
	
	/**
     * Get Map
     *
     * @return responseObject
     */
	@GetMapping("/map")
	public ResponseEntity<?> getMap() {
		logger.info("Entering Controller layer at getMap");
		
		logger.info("Leaving Controller layer at getMap");
		return new ResponseEntity<>(processOrderService.getMap(), HttpStatus.OK);
	}
	
	/**
     * Get Balances
     *
     * @return responseObject
     */
	@GetMapping("/balances")
	public ResponseEntity<?> getBalances() {
		logger.info("Entering Controller layer at getMap");
		
		logger.info("Leaving Controller layer at getMap");
		return new ResponseEntity<>(processOrderService.getBalances(), HttpStatus.OK);
	}
	
	/**
	 * Method to validate if the market is open from 6am to 3pm
	 * */
	private static boolean isMarketOpen(Long timeStamp) {
		boolean isOpen = false;
		
		LocalDateTime localDateTimeNoTimeZone = new Timestamp(timeStamp).toLocalDateTime();
		ZonedDateTime currentTime = localDateTimeNoTimeZone.atZone(ZoneId.of("America/Mexico_City"));
		
		ZonedDateTime marketOpen = currentTime.withHour(6).withMinute(0).withSecond(0);
		ZonedDateTime marketClose = currentTime.withHour(15).withMinute(59).withSecond(59);
		
		if(currentTime.compareTo(marketOpen) > 0 && currentTime.compareTo(marketClose) < 0) {
			isOpen = true;
		}
		
		return isOpen;
	}
}