package com.zc.cris.jpa.springdata.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import com.zc.cris.jpa.springdata.entities.Dog;

@Repository
public class DogDaoImpl implements DogDao{
	
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public void test() {
		Dog dog = entityManager.find(Dog.class, 14);
		System.out.println("----->" + dog);
	}
	
}
