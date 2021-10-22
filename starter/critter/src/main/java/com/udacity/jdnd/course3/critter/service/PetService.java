package com.udacity.jdnd.course3.critter.service;

import com.udacity.jdnd.course3.critter.Exception.PetNotFoundException;
import com.udacity.jdnd.course3.critter.entity.Customer;
import com.udacity.jdnd.course3.critter.entity.Pet;
import com.udacity.jdnd.course3.critter.pet.PetDTO;
import com.udacity.jdnd.course3.critter.repository.PetRepository;
import com.udacity.jdnd.course3.critter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;

    @Autowired
    public PetService(PetRepository petRepository, UserRepository userRepository) {
        this.petRepository = petRepository;
        this.userRepository = userRepository;
    }

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

    public void deletePetById(Long id){
        Pet pet;
        if(petRepository.findById(id).isPresent()){
            pet = petRepository.findById(id).get();
        }else {
            throw new PetNotFoundException("Pet not found");
        }
        pet.getCustomer().removePet(pet);
    }

    public Pet update(Pet newPet){
        return petRepository.findById(newPet.getId())
                .map(pet -> {
                    pet.setName(newPet.getName());
                    pet.setBirthDate(newPet.getBirthDate());
                    pet.setType(newPet.getType());
                    pet.setNotes(Optional.ofNullable(newPet.getNotes()).orElse(pet.getNotes()));
                    return petRepository.save(pet);
                }).orElseThrow(PetNotFoundException::new);
    }
}
