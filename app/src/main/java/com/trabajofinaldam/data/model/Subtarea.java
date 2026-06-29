package com.trabajofinaldam.data.model;

public class Subtarea {
    private int     id;
    private int     tareaId;
    private String  descripcion;
    private boolean completada;

    public Subtarea(int id, int tareaId, String descripcion, boolean completada) {
        this.id          = id;
        this.tareaId     = tareaId;
        this.descripcion = descripcion;
        this.completada  = completada;
    }

    public Subtarea(int tareaId, String descripcion) {
        this(-1, tareaId, descripcion, false);
    }

    public int     getId()          { return id; }
    public int     getTareaId()     { return tareaId; }
    public String  getDescripcion() { return descripcion; }
    public boolean isCompletada()   { return completada; }
    public void    setCompletada(boolean completada) { this.completada = completada; }
}
