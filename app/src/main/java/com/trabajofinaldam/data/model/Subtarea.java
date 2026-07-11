package com.trabajofinaldam.data.model;

public class Subtarea {
    private int id;
    private int tareaId;
    private String descripcion;
    private boolean completada;

    // Constructor completo (usado al leer de SQLite)
    public Subtarea(int id, int tareaId, String descripcion, boolean completada) {
        this.id = id;
        this.tareaId = tareaId;
        this.descripcion = descripcion;
        this.completada = completada;
    }

    // Constructor sin ID (usado al insertar)
    public Subtarea(int tareaId, String descripcion, boolean completada) {
        this.tareaId = tareaId;
        this.descripcion = descripcion;
        this.completada = completada;
    }

    public int getId() { return id; }
    public int getTareaId() { return tareaId; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean isCompletada() { return completada; }
    public void setCompletada(boolean completada) { this.completada = completada; }
}