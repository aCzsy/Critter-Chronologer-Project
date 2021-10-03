package com.udacity.jdnd.course3.critter.repository;

import com.udacity.jdnd.course3.critter.entity.Customer;
import com.udacity.jdnd.course3.critter.entity.Employee;
import com.udacity.jdnd.course3.critter.entity.Pet;
import com.udacity.jdnd.course3.critter.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    @Query("select s from Schedule s where :employee member of s.employeeList")
    List<Schedule> findSchedulesByEmployee(@Param("employee") Employee employee);

    @Query("select s from Schedule s where :pet member of s.petList")
    List<Schedule> findSchedulesByPet(@Param("pet")Pet pet);

    List<Schedule> findAllByPetListIn(List<Pet> petLists);
}
