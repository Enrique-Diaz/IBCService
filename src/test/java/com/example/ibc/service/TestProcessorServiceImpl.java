package com.example.ibc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
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
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;

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

@ExtendWith(MockitoExtension.class)
@DisplayName("TestProcessorServiceImpl")
public class TestProcessorServiceImpl {

	@Mock
	public Logger logger;
	
	@Mock
	public IBCConstants ibcConstants;
	
	@Mock
	public BalanceRepository balanceRepository;
	
	@Mock
	public IssuerRepository issuerRepository;
	
	@Mock
	public ModelMapper modelMapper;
	
	@InjectMocks
	public ProcessorServiceImpl cut;
	
	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	@DisplayName("constructor")
	void testProcessorServiceImpl() {
		assertNotNull(cut, "The constructor did not return the expected results");
	}
	
	@Test
	@DisplayName("Test getBalances")
	void testGetBalances() {
		List<Balance> balances = new ArrayList<>();
		Balance balance = new Balance();
		balance.setId(1);
		
		balances.add(balance);
		
		when(balanceRepository.findAll()).thenReturn(balances);
		
		List<Balance> response = cut.getBalances();
		
		assertNotNull(response);
		assertEquals(balances, response);
		assertEquals(balance.getId(), response.get(0).getId());
	}
	
	@Test
	@DisplayName("Test getBalance")
	@SuppressWarnings("serial")
	void testGetBalance() {
		//ProcessorServiceImpl spy = Mockito.spy(cut);
		
		List<IssuerDTO> issuersDTO = new ArrayList<>();
		IssuerDTO issuerDTO = new IssuerDTO();
		issuerDTO.setIssuerName("GBM");
		issuersDTO.add(issuerDTO);
		
		BalanceDTO balanceDTO = new BalanceDTO();
		balanceDTO.setIssuers(issuersDTO);
		
		Balance balance = new Balance();
		Optional<Balance> optionalBalance = Optional.of(balance);
		
		List<Issuer> issuers = new ArrayList<>();
		Issuer issuer = new Issuer();
		issuers.add(issuer);
		balance.setIssuer(issuers);
		Type listType = new TypeToken<List<IssuerDTO>>() {}.getType();
		
		when(balanceRepository.findById(Mockito.anyInt())).thenReturn(optionalBalance);
		when(modelMapper.map(optionalBalance.get().getIssuer(), listType, "issuerdto-list")).thenReturn(issuersDTO);
		when(modelMapper.map(optionalBalance.get(), BalanceDTO.class)).thenReturn(balanceDTO);
		
		BalanceDTO response = cut.getBalance();
		
		assertNotNull(response);
		assertEquals("GBM", response.getIssuers().get(0).getIssuerName());
	}
	
	@Nested
	@DisplayName("Tests processInitialBalances method")
	class TestProcessInitialBalances {
		@Test
		@DisplayName("Test process initial balance null")
		public void testBalanceNull() {
			Assertions.assertThrows(ServiceException.class, () -> {
				RequestDTO requestDTO = new RequestDTO();
				requestDTO.setInitialBalances(null);
				
				ProcessorServiceImpl spy = Mockito.spy(cut);
				
				List<IssuerDTO> issuersDTO = new ArrayList<>();
				IssuerDTO issuerDTO = new IssuerDTO();
				issuerDTO.setIssuerName("GBM");
				issuersDTO.add(issuerDTO);
				
				BalanceDTO balanceDTO = new BalanceDTO();
				balanceDTO.setIssuers(issuersDTO);
				
				doReturn(balanceDTO).when(spy).getBalance();
				
				BalanceDTO response = spy.getBalance();
				
				assertNotNull(response);
				assertEquals("GBM", balanceDTO.getIssuers().get(0).getIssuerName());
				
				spy.processInitialBalances(requestDTO);
			});
		}
		
		@Test
		@DisplayName("Test process initial balance created")
		public void testBalanceCreated() throws ServiceException {
			RequestDTO requestDTO = new RequestDTO();
			BalanceDTO balanceDTO = new BalanceDTO();
			List<IssuerDTO> issuersDTO = new ArrayList<>();
			IssuerDTO issuerDTO = new IssuerDTO();
			
			issuerDTO.setIssuerName("GBM");
			issuerDTO.setSharePrice(10f);
			issuerDTO.setTotalShares(100);
			issuersDTO.add(issuerDTO);
			
			balanceDTO.setCash(100f);
			balanceDTO.setIssuers(issuersDTO);
			
			requestDTO.setInitialBalances(balanceDTO);
			
			Balance balance = new Balance();
			Issuer issuer = new Issuer();
			List<Issuer> issuers = new ArrayList<>();
			issuers.add(issuer);

			when(modelMapper.map(Mockito.any(), Mockito.any())).thenReturn(balance);
			when(modelMapper.map(Mockito.anyList(), Mockito.any(Type.class), Mockito.anyString())).thenReturn(issuers);
			when(balanceRepository.save(balance)).thenReturn(balance);
			
			cut.processInitialBalances(requestDTO);
			
			verify(balanceRepository, times(1)).save(balance);
			assertEquals(issuers, balance.getIssuer());
			assertEquals(1, issuers.get(0).getId());
		}
	}
	
	@Nested
	@DisplayName("Tests processOrderWhileOpenMarket method")
	class TestProcessOrderWhileOpenMarket {
		@Test
		@DisplayName("Test processOrderWhileOpenMarket balance not found")
		public void testBalanceNotFound() {
			Assertions.assertThrows(ServiceException.class, () -> {
				RequestOrderDTO requestOrderDTO = new RequestOrderDTO();
				requestOrderDTO.setIssuerName("GBM");
				requestOrderDTO.setOperation(Operation.BUY);
				Optional<Balance> optionalBalance = Optional.empty();
				
				when(balanceRepository.findById(Mockito.anyInt())).thenReturn(optionalBalance);
				
				ProcessorServiceImpl spy = Mockito.spy(cut);
				
				List<IssuerDTO> issuersDTO = new ArrayList<>();
				IssuerDTO issuerDTO = new IssuerDTO();
				issuerDTO.setIssuerName("GBM");
				issuersDTO.add(issuerDTO);
				
				BalanceDTO balanceDTO = new BalanceDTO();
				balanceDTO.setIssuers(issuersDTO);
				
				doReturn(balanceDTO).when(spy).getBalance();
				
				spy.processOrderWhileOpenMarket(requestOrderDTO);
			});
		}
		
		@Test
		@DisplayName("Test processOrderWhileOpenMarket balance found and issuer empty")
		public void testBalanceFoundIssuerEmpty() {
			Assertions.assertThrows(ServiceException.class, () -> {
				RequestOrderDTO requestOrderDTO = new RequestOrderDTO();
				requestOrderDTO.setIssuerName("GBM");
				requestOrderDTO.setOperation(Operation.BUY);
				Balance balance = new Balance();
				balance.setIssuer(new ArrayList<Issuer>());
				Optional<Balance> optionalBalance = Optional.of(balance);
				
				when(balanceRepository.findById(Mockito.anyInt())).thenReturn(optionalBalance);
				
				ProcessorServiceImpl spy = Mockito.spy(cut);
				
				List<IssuerDTO> issuersDTO = new ArrayList<>();
				IssuerDTO issuerDTO = new IssuerDTO();
				issuerDTO.setIssuerName("GBM");
				issuersDTO.add(issuerDTO);
				
				BalanceDTO balanceDTO = new BalanceDTO();
				balanceDTO.setIssuers(issuersDTO);
				
				doReturn(balanceDTO).when(spy).getBalance();
				
				spy.processOrderWhileOpenMarket(requestOrderDTO);
			});
		}
		
		@Test
		@DisplayName("Test processOrderWhileOpenMarket duplicated order buy")
		public void testDuplicatedOrderBuy() {
			Assertions.assertThrows(ServiceException.class, () -> {
				RequestOrderDTO requestOrderDTO = new RequestOrderDTO();
				requestOrderDTO.setIssuerName("GBM");
				requestOrderDTO.setOperation(Operation.BUY);
				requestOrderDTO.setTimeStamp(1000L);
				Balance balance = new Balance();
				List<Issuer> issuers = new ArrayList<>();
				Issuer issuer = new Issuer();
				issuers.add(issuer);
				balance.setIssuer(issuers);
				Optional<Balance> optionalBalance = Optional.of(balance);
				
				when(balanceRepository.findById(Mockito.anyInt())).thenReturn(optionalBalance);
				
				ProcessorServiceImpl spy = Mockito.spy(cut);
				
				List<IssuerDTO> issuersDTO = new ArrayList<>();
				IssuerDTO issuerDTO = new IssuerDTO();
				issuerDTO.setIssuerName("GBM");
				issuersDTO.add(issuerDTO);
				
				BalanceDTO balanceDTO = new BalanceDTO();
				balanceDTO.setIssuers(issuersDTO);
				
				doReturn(balanceDTO).when(spy).getBalance();
				doReturn(true).when(spy).isDuplicated(requestOrderDTO.getTimeStamp(), Operation.BUY);
				
				spy.processOrderWhileOpenMarket(requestOrderDTO);
			});
		}
		
		@Test
		@DisplayName("Test processOrderWhileOpenMarket duplicated order sell")
		public void testDuplicatedOrderSell() {
			Assertions.assertThrows(ServiceException.class, () -> {
				RequestOrderDTO requestOrderDTO = new RequestOrderDTO();
				requestOrderDTO.setIssuerName("GBM");
				requestOrderDTO.setOperation(Operation.SELL);
				requestOrderDTO.setTimeStamp(1000L);
				Balance balance = new Balance();
				List<Issuer> issuers = new ArrayList<>();
				Issuer issuer = new Issuer();
				issuers.add(issuer);
				balance.setIssuer(issuers);
				Optional<Balance> optionalBalance = Optional.of(balance);
				
				when(balanceRepository.findById(Mockito.anyInt())).thenReturn(optionalBalance);
				
				ProcessorServiceImpl spy = Mockito.spy(cut);
				
				List<IssuerDTO> issuersDTO = new ArrayList<>();
				IssuerDTO issuerDTO = new IssuerDTO();
				issuerDTO.setIssuerName("GBM");
				issuersDTO.add(issuerDTO);
				
				BalanceDTO balanceDTO = new BalanceDTO();
				balanceDTO.setIssuers(issuersDTO);
				
				doReturn(balanceDTO).when(spy).getBalance();
				doReturn(true).when(spy).isDuplicated(requestOrderDTO.getTimeStamp(), Operation.SELL);
				
				spy.processOrderWhileOpenMarket(requestOrderDTO);
			});
		}
		
		@Test
		@DisplayName("Test processOrderWhileOpenMarket process order buy")
		@SuppressWarnings("serial")
		public void testProcessOrderBuy() throws ServiceException {
			RequestOrderDTO requestOrderDTO = new RequestOrderDTO();
			requestOrderDTO.setIssuerName("GBM");
			requestOrderDTO.setOperation(Operation.BUY);
			requestOrderDTO.setTimeStamp(1000L);
			Balance balance = new Balance();
			List<Issuer> issuers = new ArrayList<>();
			Issuer issuer = new Issuer();
			issuers.add(issuer);
			balance.setIssuer(issuers);
			Optional<Balance> optionalBalance = Optional.of(balance);
			
			when(balanceRepository.findById(Mockito.anyInt())).thenReturn(optionalBalance);
			
			ProcessorServiceImpl spy = Mockito.spy(cut);
			
			List<IssuerDTO> issuersDTO = new ArrayList<>();
			IssuerDTO issuerDTO = new IssuerDTO();
			issuerDTO.setIssuerName("GBM");
			issuersDTO.add(issuerDTO);
			
			BalanceDTO balanceDTO = new BalanceDTO();
			// Response
			balanceDTO.setCash(100f);
			Balance responseBalance = new Balance();
			responseBalance.setIssuer(issuers);
			
			Type listType = new TypeToken<List<IssuerDTO>>() {}.getType();
			
			doReturn(false).when(spy).isDuplicated(requestOrderDTO.getTimeStamp(), Operation.BUY);
			doReturn(responseBalance).when(spy).buyOrder(requestOrderDTO, issuer, balance);
			
			when(modelMapper.map(responseBalance.getIssuer(),listType, "issuerdto-list")).thenReturn(issuersDTO);
			when(modelMapper.map(Mockito.any(), Mockito.any())).thenReturn(balanceDTO);
			
			BalanceDTO responseBalanceDTO = spy.processOrderWhileOpenMarket(requestOrderDTO);
			
			assertNotNull(responseBalanceDTO);
			assertEquals(100f, responseBalanceDTO.getCash());
			assertEquals("GBM", responseBalanceDTO.getIssuers().get(0).getIssuerName());
		}
		
		@Test
		@DisplayName("Test processOrderWhileOpenMarket process order sell")
		@SuppressWarnings("serial")
		public void testProcessOrderSell() throws ServiceException {
			RequestOrderDTO requestOrderDTO = new RequestOrderDTO();
			requestOrderDTO.setIssuerName("GBM");
			requestOrderDTO.setOperation(Operation.SELL);
			requestOrderDTO.setTimeStamp(1000L);
			Balance balance = new Balance();
			List<Issuer> issuers = new ArrayList<>();
			Issuer issuer = new Issuer();
			issuers.add(issuer);
			balance.setIssuer(issuers);
			Optional<Balance> optionalBalance = Optional.of(balance);
			
			when(balanceRepository.findById(Mockito.anyInt())).thenReturn(optionalBalance);
			
			ProcessorServiceImpl spy = Mockito.spy(cut);
			
			List<IssuerDTO> issuersDTO = new ArrayList<>();
			IssuerDTO issuerDTO = new IssuerDTO();
			issuerDTO.setIssuerName("GBM");
			issuersDTO.add(issuerDTO);
			
			BalanceDTO balanceDTO = new BalanceDTO();
			// Response
			balanceDTO.setCash(100f);
			Balance responseBalance = new Balance();
			responseBalance.setIssuer(issuers);
			
			Type listType = new TypeToken<List<IssuerDTO>>() {}.getType();
			
			doReturn(false).when(spy).isDuplicated(requestOrderDTO.getTimeStamp(), Operation.SELL);
			doReturn(responseBalance).when(spy).sellOrder(requestOrderDTO, issuer, balance);
			
			when(modelMapper.map(responseBalance.getIssuer(),listType, "issuerdto-list")).thenReturn(issuersDTO);
			when(modelMapper.map(Mockito.any(), Mockito.any())).thenReturn(balanceDTO);
			
			BalanceDTO responseBalanceDTO = spy.processOrderWhileOpenMarket(requestOrderDTO);
			
			assertNotNull(responseBalanceDTO);
			assertEquals(100f, responseBalanceDTO.getCash());
			assertEquals("GBM", responseBalanceDTO.getIssuers().get(0).getIssuerName());
		}
	}
	
	@Nested
	@DisplayName("Tests process buyOrder method")
	class TestProcessBuyOrder {
		@Test
		@DisplayName("Test process buy order inssuficient balance")
		public void testBuyOrderInsufficientBalance() {
			Assertions.assertThrows(ServiceException.class, () -> {
				RequestOrderDTO requestOrderDTO = new RequestOrderDTO();
				requestOrderDTO.setTotalShares(100);
				requestOrderDTO.setSharePrice(10f);
				
				Issuer issuer = new Issuer();
				
				Balance balance = new Balance();
				balance.setCash(10f);
				
				ProcessorServiceImpl spy = Mockito.spy(cut);
				
				List<IssuerDTO> issuersDTO = new ArrayList<>();
				IssuerDTO issuerDTO = new IssuerDTO();
				issuerDTO.setIssuerName("GBM");
				issuersDTO.add(issuerDTO);
				
				BalanceDTO balanceDTO = new BalanceDTO();
				balanceDTO.setIssuers(issuersDTO);
				
				doReturn(balanceDTO).when(spy).getBalance();
				
				spy.buyOrder(requestOrderDTO, issuer, balance);
			});
		}
		
		@Test
		@DisplayName("Test process buy order")
		public void testBuyOrder() throws ServiceException {
			RequestOrderDTO requestOrderDTO = new RequestOrderDTO();
			requestOrderDTO.setTotalShares(100);
			requestOrderDTO.setSharePrice(10f);
			requestOrderDTO.setTimeStamp(10000L);;
			
			Issuer issuer = new Issuer();
			issuer.setTotalShares(10);
			
			Balance balance = new Balance();
			balance.setCash(2000f);
			
			List<Issuer> issuers = new ArrayList<>();
			issuers.add(issuer);
			balance.setIssuer(issuers);
			
			when(balanceRepository.save(balance)).thenReturn(balance);
			
			Balance responseBalance = cut.buyOrder(requestOrderDTO, issuer, balance);
			
			assertNotNull(responseBalance);
			assertEquals(1000f, responseBalance.getCash());
			assertNotNull(responseBalance.getIssuer());
			assertEquals(110, responseBalance.getIssuer().get(0).getTotalShares());
			assertEquals(10000L, responseBalance.getIssuer().get(0).getLastBuy());
		}
	}
	
	@Nested
	@DisplayName("Tests process sellOrder method")
	class TestProcessSellOrder {
		@Test
		@DisplayName("Test process sell order inssuficient stock")
		public void testSellOrderInsufficientStock() {
			Assertions.assertThrows(ServiceException.class, () -> {
				RequestOrderDTO requestOrderDTO = new RequestOrderDTO();
				requestOrderDTO.setTotalShares(100);
				requestOrderDTO.setSharePrice(10f);
				
				Issuer issuer = new Issuer();
				issuer.setTotalShares(10);
				
				Balance balance = new Balance();
				balance.setCash(10f);
				
				ProcessorServiceImpl spy = Mockito.spy(cut);
				
				List<IssuerDTO> issuersDTO = new ArrayList<>();
				IssuerDTO issuerDTO = new IssuerDTO();
				issuerDTO.setIssuerName("GBM");
				issuersDTO.add(issuerDTO);
				
				BalanceDTO balanceDTO = new BalanceDTO();
				balanceDTO.setIssuers(issuersDTO);
				
				doReturn(balanceDTO).when(spy).getBalance();
				
				spy.sellOrder(requestOrderDTO, issuer, balance);
			});
		}
		
		@Test
		@DisplayName("Test process sell order")
		public void testSellOrder() throws ServiceException {
			RequestOrderDTO requestOrderDTO = new RequestOrderDTO();
			requestOrderDTO.setTotalShares(10);
			requestOrderDTO.setSharePrice(10f);
			requestOrderDTO.setTimeStamp(10000L);;
			
			Issuer issuer = new Issuer();
			issuer.setTotalShares(100);
			
			Balance balance = new Balance();
			balance.setCash(2000f);
			
			List<Issuer> issuers = new ArrayList<>();
			issuers.add(issuer);
			balance.setIssuer(issuers);
			
			when(balanceRepository.save(balance)).thenReturn(balance);
			
			Balance responseBalance = cut.sellOrder(requestOrderDTO, issuer, balance);
			
			assertNotNull(responseBalance);
			assertEquals(2100f, responseBalance.getCash());
			assertNotNull(responseBalance.getIssuer());
			assertEquals(90, responseBalance.getIssuer().get(0).getTotalShares());
			assertEquals(10000L, responseBalance.getIssuer().get(0).getLastSell());
		}
	}
}









