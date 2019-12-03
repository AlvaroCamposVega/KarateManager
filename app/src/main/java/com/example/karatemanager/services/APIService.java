package com.example.karatemanager.services;

import com.example.karatemanager.model.Evento;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface APIService
{
  @GET("api/eventos")
  Call<List<Evento>> getAllEventos();

  /*@GET("api/eventos")
  Call<Evento> getEvento(@Query("id") int id);*/
}
