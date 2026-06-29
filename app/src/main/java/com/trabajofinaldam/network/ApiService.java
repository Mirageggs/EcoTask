package com.trabajofinaldam.network;

import com.trabajofinaldam.EcoConsejo;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("api/ecoconsejos/hoy")
    Call<EcoConsejo> obtenerConsejoHoy();
}
