package com.trabajofinaldam.data.model;

import com.google.gson.annotations.SerializedName;

public class DivisionRequest {

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("horas")
    private int horas;

    public DivisionRequest(String descripcion, int horas) {
        this.descripcion = descripcion;
        this.horas = horas;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getHoras() {
        return horas;
    }

    public void setHoras(int horas) {
        this.horas = horas;
    }
}