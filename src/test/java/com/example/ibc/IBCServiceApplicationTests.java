package com.example.ibc;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("IBCServiceApplicationTests")
class IBCServiceApplicationTests {

	IBCServiceApplication cut;
	
	@BeforeEach
	void init() {
		cut = new IBCServiceApplication();
	}
	
	@Test
	@DisplayName("constructor")
	public void testDispatchServiceApplication() {
		assertNotNull(cut, "The call to the constructor did not return the expected results");
	}
}