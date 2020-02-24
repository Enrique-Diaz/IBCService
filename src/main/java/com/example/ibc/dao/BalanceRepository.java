package com.example.ibc.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.ibc.model.Balance;

@Repository
public interface BalanceRepository  extends CrudRepository<Balance, Integer> {

}