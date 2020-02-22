package com.example.ibc.service;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProcessOrderService implements ProcessOrderServiceImpl{

	@Autowired
	private Logger logger;
	
	@Override
	public String processOrderWhileOpenMarket(String order) {
		if (isMarketOpen()) {
			logger.info("PROCESSING ORDER: " + order);
			order = order + " processed";
		} else {
			logger.info("NOT PROCESSING ORDER: " + order);
			order = order + " not processed";
		}
		
		return order;
	}
	
	private boolean isMarketOpen() {
		boolean isOpen = false;
		
		ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of("America/Mexico_City"));
		ZonedDateTime marketOpen = currentTime.withHour(6).withMinute(0).withSecond(0);
		ZonedDateTime marketClose = currentTime.withHour(15).withMinute(59).withSecond(59);
		
		if(currentTime.compareTo(marketOpen) > 0 && currentTime.compareTo(marketClose) < 0) {
			isOpen = true;
		}
		
		return isOpen;
	}
}