package com.example.karatemanager.model;

import java.io.Serializable;

public class User implements Serializable
{
  private String name;
  private String surName;
  private String country;
  private String city;

  public User() {}

  public User(String name, String surName, String country, String city)
  {
    this.name = name;
    this.surName = surName;
    this.country = country;
    this.city = city;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getSurName()
  {
    return surName;
  }

  public void setSurName(String surName)
  {
    this.surName = surName;
  }

  public String getCountry()
  {
    return country;
  }

  public void setCountry(String country)
  {
    this.country = country;
  }

  public String getCity()
  {
    return city;
  }

  public void setCity(String city)
  {
    this.city = city;
  }

  @Override
  public String toString()
  {
    return name + " " + surName;
  }
}
