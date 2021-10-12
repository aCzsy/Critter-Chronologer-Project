package com.udacity.jdnd.course3.critter.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Customer extends User {
    @Column(name = "phone_number")
    private String phoneNumber;
    private String notes;
    //mappedBy = referenced by
    @OneToMany(fetch = FetchType.EAGER,mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pet> pets = new ArrayList<>();

    public Customer() {
    }

    //Adding addPet and removePet methods to make sure both sides of relationship are always in sync.
    public void addPet(Pet pet){
        pets.add(pet);
        pet.setCustomer(this);
    }

    public void removePet(Pet pet){
        pets.remove(pet);
        pet.setCustomer(null);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<Pet> getPets() {
        return pets;
    }

    public void setPets(List<Pet> pets) {
        this.pets = pets;
    }
}
