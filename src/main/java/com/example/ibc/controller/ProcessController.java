package com.example.ibc.controller;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ibc.service.ProcessOrderService;

@RestController
@RequestMapping("/process")
public class ProcessController {

	@Autowired
	private Logger logger;
	
	@Autowired
	private ProcessOrderService processOrderService;
	
	/**
     * Get test.
     *
     * @param id
     * 
     * @return id
     */
	@GetMapping("/orders/{id}")
	public ResponseEntity<?> processOrder(@PathVariable(value="id") String id) {
		logger.info("Entering Controller layer at processOrder, id to process:{}", id);

		String response = processOrderService.processOrderWhileOpenMarket(id);
		
		logger.info("Leaving Controller layer at processOrder");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}