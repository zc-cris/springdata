package com.zc.cris.jpa.springdata.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zc.cris.jpa.springdata.entities.Dog;
import com.zc.cris.jpa.springdata.repository.DogRepository;

@Service
public class DogService {
	
	@Autowired
	private DogRepository dogRepository;
	
	@Transactional
	public void saveDog(Dog dog) {
		dogRepository.save(dog);
	}
	
	@Transactional
	public void saveDogs(List<Dog> dogs) {
		dogRepository.saveAll(dogs);
	}
	
	@Transactional
	public void updateDogAge(Integer age, Integer id) {
		dogRepository.updateDogAge(age, id);
	}
	
	
}
