package com.udacity.jdnd.course3.critter.service;

import com.udacity.jdnd.course3.critter.entity.Pet;
import com.udacity.jdnd.course3.critter.pet.PetDTO;
import com.udacity.jdnd.course3.critter.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class PetService {

    @Autowired
    PetRepository petRepository;

    public Pet savePet(Pet pet){
        return petRepository.save(pet);
    }

    public Pet findPetById(Long id){
        return petRepository.getOne(id);
    }

    public List<Pet> allPets(){
        return petRepository.findAll();
    }

    public List<Pet> findPetsByOwner(Long id){
        return petRepository.findPetsByCustomerId(id);
    }
}
