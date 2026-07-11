package com.trabajofinaldam.data.network;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // URL de producción en Somee
    private static final String BASE_URL = "http://efappsmovilesajv.somee.com/";

    private static Retrofit retrofit;
    private static ApiService apiService;

    public static Retrofit getClient() {
        if (retrofit == null) {

            // Configuramos un timeout de 30 segundos (vital para peticiones a IAs)
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient) // Añadimos el cliente con los timeouts
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // Método de conveniencia para llamar directamente al servicio (usado por Newtaskviewmodel)
    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getClient().create(ApiService.class);
        }
        return apiService;
    }
}