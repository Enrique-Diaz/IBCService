package com.example.ibc.dto;

import java.io.Serializable;

public enum Operation implements Serializable {
	
    BUY("B"), SELL("S");

    String code;
 
    private Operation(String code) {
        this.code = code;
    }
 
    public String getCode() {
        return code;
    }
}