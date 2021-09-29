package com.udacity.jdnd.course3.critter.pet;

import com.udacity.jdnd.course3.critter.entity.Customer;
import com.udacity.jdnd.course3.critter.entity.Pet;
import com.udacity.jdnd.course3.critter.service.PetService;
import com.udacity.jdnd.course3.critter.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles web requests related to Pets.
 */
@RestController
@RequestMapping("/pet")
public class PetController {
    @Autowired
    PetService petService;

    @Autowired
    UserService userService;

    @PostMapping
    public PetDTO savePet(@RequestBody PetDTO petDTO) {
        //Saving pet
        Pet savedPet = petService.savePet(convertPetDTOtoPetEntity(petDTO));
        //Owner if this pet
        Customer customer = (Customer) userService.findUserById(petDTO.getOwnerId());
        //adds pet to customer and sets customer for current pet
        customer.addPet(savedPet);
        //DTO to be returned
        PetDTO petDTOtoReturn = convertPetToPetDTO(petService.findPetById(savedPet.getId()));
        //Setting ownerId of a DTO to be returned
        petDTOtoReturn.setOwnerIdOfPetDTO(petDTOtoReturn,customer);
        return petDTOtoReturn;
    }

    @GetMapping("/{petId}")
    public PetDTO getPet(@PathVariable long petId) {
        Pet pet = petService.findPetById(petId);
        PetDTO petDTOToBeReturned = convertPetToPetDTO(pet);
        petDTOToBeReturned.setOwnerId(pet.getCustomer().getId());
        return petDTOToBeReturned;
    }

    @GetMapping
    public List<PetDTO> getPets(){
        List<PetDTO> listOfPets =
                petService.allPets()
                .stream()
                .map(pet -> {
                    PetDTO petDTOtoBeReturned = convertPetToPetDTO(pet);
                    petDTOtoBeReturned.setOwnerId(pet.getCustomer().getId());
                    return petDTOtoBeReturned;
                })
                .collect(Collectors.toList());
        return listOfPets;
    }

    @GetMapping("/owner/{ownerId}")
    public List<PetDTO> getPetsByOwner(@PathVariable long ownerId) {
        List<PetDTO> allPetsByOwner =
                petService.findPetsByOwner(ownerId)
                .stream()
                .map(pet -> {
                    PetDTO petDTOtoBeReturned = convertPetToPetDTO(pet);
                    petDTOtoBeReturned.setOwnerId(pet.getCustomer().getId());
                    return petDTOtoBeReturned;
                })
                .collect(Collectors.toList());
        return allPetsByOwner;
    }

    private static Pet convertPetDTOtoPetEntity(PetDTO petDTO){
        Pet pet = new Pet();
        BeanUtils.copyProperties(petDTO,pet);
        return pet;
    }

    private static PetDTO convertPetToPetDTO(Pet pet){
        PetDTO petDTO = new PetDTO();
        BeanUtils.copyProperties(pet,petDTO);
        petDTO.setOwnerId(pet.getCustomer().getId());
        return petDTO;
    }
}
