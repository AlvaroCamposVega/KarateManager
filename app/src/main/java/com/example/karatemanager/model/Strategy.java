package com.example.karatemanager.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Strategy implements Serializable
{
  @Expose
  @SerializedName("id")
  private int id;

  @Expose
  @SerializedName("target")
  private String target;

  @Expose
  @SerializedName("category")
  private String category;

  @Expose
  @SerializedName("des")
  private String desc;

  public Strategy() {}

  public Strategy(int id, String target, String category, String desc)
  {
    this.id = id;
    this.target = target;
    this.category = category;
    this.desc = desc;
  }

  public int getId() { return id; }

  public void setId(int id) { this.id = id; }

  public String getTarget() { return target; }

  public void setTarget(String target) { this.target = target; }

  public String getCategory() { return category; }

  public void setCategory(String category) { this.category = category; }

  public String getDesc() { return desc; }

  public void setDesc(String desc) { this.desc = desc; }
}
