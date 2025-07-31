package org.example.snappfrontend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Menu {
    private Integer id;
    private String title;

    public Menu() {}

    public Integer getId() {return id;}
    public void setId(Integer id) {this.id = id;}
    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}
}