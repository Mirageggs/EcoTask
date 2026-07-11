package com.trabajofinaldam.data.network;

import com.trabajofinaldam.data.model.DivisionRequest;
import com.trabajofinaldam.data.model.DivisionResponse;
import com.trabajofinaldam.data.model.EcoConsejo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    // Endpoint para el Dashboard (hecho por tu amigo)
    @GET("api/ecoconsejos/hoy")
    Call<EcoConsejo> obtenerConsejoHoy();

    // Endpoint para la Inteligencia Artificial (hecho por ti)
    @POST("api/tareas/dividir")
    Call<DivisionResponse> dividirTarea(@Body DivisionRequest request);

}