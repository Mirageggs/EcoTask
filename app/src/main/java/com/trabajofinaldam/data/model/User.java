package com.trabajofinaldam.data.model;

public class User {

    public static final String ROL_ESTUDIANTE   = "ESTUDIANTE";
    public static final String ROL_PROFESIONAL  = "PROFESIONAL";
    public static final String ROL_GENERAL      = "GENERAL";

    private int    id;
    private String nombre;
    private String email;
    private String passwordHash;
    private String rol;
    private String fechaRegistro;

    public User(int id, String nombre, String email,
                String passwordHash, String rol, String fechaRegistro) {
        this.id             = id;
        this.nombre         = nombre;
        this.email          = email;
        this.passwordHash   = passwordHash;
        this.rol            = rol;
        this.fechaRegistro  = fechaRegistro;
    }

    public User(String nombre, String email, String passwordHash, String rol) {
        this(-1, nombre, email, passwordHash, rol, "");
    }

    public int    getId()            { return id; }
    public String getNombre()        { return nombre; }
    public String getEmail()         { return email; }
    public String getPasswordHash()  { return passwordHash; }
    public String getRol()           { return rol; }
    public String getFechaRegistro() { return fechaRegistro; }

    public void setId(int id)          { this.id = id; }
    public void setNombre(String n)    { this.nombre = n; }
    public void setEmail(String e)     { this.email = e; }
    public void setRol(String r)       { this.rol = r; }
}
