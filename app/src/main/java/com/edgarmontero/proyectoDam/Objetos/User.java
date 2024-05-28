package com.edgarmontero.proyectoDam.Objetos;

public class User {
    private int idUser;
    private String name;
    private String password;
    private String email;

    public User(int idUser, String name) {
        this.idUser = idUser;
        this.name = name;

    }
    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


}
