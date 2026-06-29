package com.trabajofinaldam.data.model;

public class DivisionRequest {
    private String descripcion;
    private int    horas;

    public DivisionRequest(String descripcion, int horas) {
        this.descripcion = descripcion;
        this.horas       = horas;
    }

    public String getDescripcion() { return descripcion; }
    public int    getHoras()       { return horas; }
}
