package com.example.karatemanager.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Evento implements Serializable
{
  @Expose
  @SerializedName("id")
  private int id;

  @Expose
  @SerializedName("nombre")
  private String name;

  @Expose
  @SerializedName("ciudad")
  private String city;

  @Expose
  @SerializedName("fechaInicio")
  private String startDate;

  @Expose
  @SerializedName("fechaFin")
  private String endDate;

  public Evento() {}

  public Evento(int id, String name, String city, String startDate, String endDate)
  {
    this.id = id;
    this.name = name;
    this.city = city;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public int getId() { return id; }

  public void setId(int id) { this.id = id; }

  public String getName() { return name; }

  public void setName(String name) { this.name = name; }

  public String getCity() { return city; }

  public void setCity(String city) { this.city = city; }

  public String getEndDate() { return endDate; }

  public void setEndDate(String endDate) { this.endDate = endDate; }

  public String getStartDate() { return startDate; }

  public void setStartDate(String startDate) { this.startDate = startDate; }

  @Override
  public String toString() { return "Nombre: " + name; }
}
