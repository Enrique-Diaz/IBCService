package com.example.ibc.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.ibc.dto.BalanceDTO;
import com.example.ibc.dto.RequestDTO;
import com.example.ibc.dto.RequestOrderDTO;
import com.example.ibc.dto.ResponseDTO;
import com.example.ibc.exception.ServiceException;
import com.example.ibc.model.Balance;
import com.example.ibc.service.ProcessorServiceImpl;
import com.example.ibc.utils.IBCConstants;

@ExtendWith(MockitoExtension.class)
@DisplayName("TestProcessorController")
public class TestProcessorController {

	@Mock
	public Logger logger;
	
	@Mock
	public IBCConstants ibcConstants;
	
	@Mock
	public ProcessorServiceImpl processorServiceImpl;
	
	@InjectMocks
	ProcessorController cut;
	
	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	@DisplayName("constructor")
	void testProcessorController() {
		assertNotNull(cut, "The constructor did not return the expected results");
	}
	
	@Test
	@DisplayName("Test getBalances")
	@SuppressWarnings("unchecked")
	void testGetBalances() {
		List<Balance> balances = new ArrayList<>();
		Balance balance = new Balance();
		balance.setId(1);
		
		balances.add(balance);
		
		when(processorServiceImpl.getBalances()).thenReturn(balances);
		
		ResponseEntity<?> response = cut.getBalances();
		
		assertEquals(balances, response.getBody());
		assertEquals(balance.getId(), ((List<Balance>) response.getBody()).get(0).getId());
	}
	
	@Nested
	@DisplayName("Tests processInitialBalance method")
	class TestProcessInitialBalance {
		@Test
		@DisplayName("Test process initial balance null")
		void testBalanceNull() throws ServiceException {
			RequestDTO requestDTO = null;
			
			ResponseEntity<?> responseEntity = cut.processInitialBalance(requestDTO);
			
			assertNotNull(responseEntity);
			assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
		}
		
		@Test
		@DisplayName("Test process initial balance created")
		void testBalanceCreated() throws ServiceException {
			RequestDTO requestDTO = new RequestDTO();
			BalanceDTO initialBalances = new BalanceDTO();
			
			initialBalances.setCash(1000f);
			requestDTO.setInitialBalances(initialBalances);
			
			ResponseEntity<?> responseEntity = cut.processInitialBalance(requestDTO);
			
			assertNotNull(responseEntity);
			assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
		}
	}
	
	@Nested
	@DisplayName("Tests processOrder method")
	class TestProcessOrder {
		RequestOrderDTO requestOrderDTO;
		
		@Test
		@DisplayName("Test process order null")
		void testBalanceNull() throws ServiceException {
			requestOrderDTO = null;
			
			ResponseEntity<?> responseEntity = cut.processOrder(requestOrderDTO);
			
			assertNotNull(responseEntity);
			assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
		}
		
		@Test
		@DisplayName("Test process order market closed")
		void testMarketClosed() throws ServiceException {
			requestOrderDTO = new RequestOrderDTO();
			ProcessorController spy = Mockito.spy(cut);
			
			ResponseDTO responseDTO = new ResponseDTO();
			ResponseEntity<?> response = new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
			
			doReturn(false).when(spy).isMarketOpen(Mockito.anyLong());
			doReturn(response).when(spy).processOrder(requestOrderDTO);
			
			ResponseEntity<?> responseEntity = spy.processOrder(requestOrderDTO);
			
			assertFalse(spy.isMarketOpen(Mockito.anyLong()));
			assertNotNull(responseEntity);
			assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
			assertNotNull(responseEntity.getBody());
		}
		
		@Test
		@DisplayName("Test process order market open")
		void testMarketOpen() throws ServiceException {
			requestOrderDTO = new RequestOrderDTO();
			ProcessorController spy = Mockito.spy(cut);
			
			ResponseDTO responseDTO = new ResponseDTO();
			ResponseEntity<?> response = new ResponseEntity<>(responseDTO, HttpStatus.OK);
			
			doReturn(true).when(spy).isMarketOpen(Mockito.anyLong());
			doReturn(response).when(spy).processOrder(requestOrderDTO);
			
			ResponseEntity<?> responseEntity = spy.processOrder(requestOrderDTO);
			
			assertTrue(spy.isMarketOpen(Mockito.anyLong()));
			assertNotNull(responseEntity);
			assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
			assertNotNull(responseEntity.getBody());
		}
	}
}