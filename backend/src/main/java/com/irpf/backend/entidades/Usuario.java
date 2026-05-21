package com.irpf.backend.entidades;

import jakarta.persistence.*;

/**
 * Entidad JPA que representa la tabla y los datos de dominio de Usuario.
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario")
    private String usuario;

    private String email;

    private String password; 

    private String esadmin; 
    
    private String esverificado;

    @Column(name = "NUM_ACC_ERRONEOS", nullable = false)
    private Integer numAccErroneos = 0;

    // === Getters and Setters ===

    public void setEsadmin(String esadmin) {
        this.esadmin = esadmin;
    }

    public String getEsadmin() {
        return esadmin;
    }

    public String getEsverificado() {
        return esverificado;
    }

    public void setEsverificado(String esverificado) {
        this.esverificado = esverificado;
    }

    public Integer getNumAccErroneos() {
        return numAccErroneos;
    }

    public void setNumAccErroneos(Integer numAccErroneos) {
        this.numAccErroneos = numAccErroneos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}

