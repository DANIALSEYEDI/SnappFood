package org.example.snappfrontend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
public class MenuResponse {
    @JsonProperty("id")
    private long id;

    @JsonProperty("title")
    private String title;

    // Getters and Setters
    public long getId() {return id;}
    public void setId(long id) {this.id = id;}
    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}
}