package com.example.ibc.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.ibc.model.Issuer;

@Repository
public interface IssuerRepository extends CrudRepository<Issuer, Integer>{

}