package com.jakobdario.groshare.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "apartments")
public class Apartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String address;

    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL)
    private List<User> users = new ArrayList<>();

    public Apartment() {
    }

    public Apartment(String address) {
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
