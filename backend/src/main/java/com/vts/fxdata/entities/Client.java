package com.vts.fxdata.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "clients",
        uniqueConstraints={
                @UniqueConstraint(columnNames = {"token"})
        })
public class Client {
    @Id
    @SequenceGenerator(
            name = "client_sequence",
            sequenceName = "client_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "client_sequence"
    )
    private Long Id;
    private String token;

    public Client(String token) {
        this.token = token;
    }

    public Client() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}


