package com.example.karatemanager.services;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient
{
  // Retrofit parser
  private static Gson gson;
  // Retrofit instance
  private static Retrofit retrofit = null;
  // API service instance
  private static APIService service = null;
  // HTTP instance
  private static OkHttpClient httpClient;
  // Intercepts the packages between Retrofit and the API
  private static HttpLoggingInterceptor httpInterceptor;

  private APIClient() {}

  public static APIService getService(String url)
  {
    if (service == null)
    {
      // Get the Interceptor instance
      httpInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger()
      {
        @Override
        public void log(@NotNull String response)
        {
          Log.d("INTERCEPTOR:", response);
        }
      });
      // Define the interception level
      httpInterceptor.level(HttpLoggingInterceptor.Level.BODY);
      // instance HTTP service
      httpClient = new OkHttpClient.Builder().addInterceptor(httpInterceptor).build();
      // instance GSON
      gson = new GsonBuilder().setLenient().create();
      // Instance Retrofit
      retrofit = new Retrofit
        .Builder()
        .baseUrl(url)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create(gson)).build();
      // Asociate Retrofit with our service interface
      service = retrofit.create(APIService.class);
    }

    return service;
  }
}
