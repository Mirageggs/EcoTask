package com.trabajofinaldam.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DivisionResponse {

    @SerializedName("subtareas")
    private List<String> subtareas;

    public List<String> getSubtareas() {
        return subtareas;
    }

    public void setSubtareas(List<String> subtareas) {
        this.subtareas = subtareas;
    }
}