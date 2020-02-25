package com.example.ibc.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application-default.yml")
public class IBCConstants {

	@Value("${constants.invalid_operationl}")
	public String INVALID_OPERATION;
	
	@Value("${constants.market_closed}")
	public String MARKET_CLOSED;
	
	@Value("${constants.duplicated_operation}")
	public String DUPLICATED_OPERATION;
	
	@Value("${constants.insufficient_balance}")
	public String INSUFFICIENT_BALANCE;
	
	@Value("${constants.insufficient_stocks}")
	public String INSUFFICIENT_STOCKS;
	
	@Value("${constants.timezone}")
	public String TIME_ZONE;
	
	@Value("${constants.zero}")
	public int ZERO;
	
	@Value("${constants.open_hour}")
	public int OPEN_HOUR;
	
	@Value("${constants.close_hour}")
	public int CLOSE_HOUR;
	
	@Value("${constants.close_minute}")
	public int CLOSE_MINUTE;
	
	@Value("${constants.close_second}")
	public int CLOSE_SECOND;
}