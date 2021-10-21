package com.udacity.jdnd.course3.critter.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.udacity.jdnd.course3.critter.view.Views;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@DiscriminatorColumn(name = "user_type")
//Each entity is mapped to a table and all subclasses also have fields from a parent class.
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class User {
    @Id
    @GeneratedValue
    @JsonView(Views.Public.class)
    Long id;
    @JsonView(Views.Public.class)
    @Nationalized
    String name;
    @JsonView(Views.Public.class)
    @Transient
    private final String type = this.getClass().getSimpleName();

    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
