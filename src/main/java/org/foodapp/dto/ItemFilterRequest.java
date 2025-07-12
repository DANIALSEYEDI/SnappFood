package org.foodapp.dto;

import java.util.List;

public class ItemFilterRequest {
    public String search;
    public Integer price;
    public List<String> keywords;

    public String getSearch() {
        return search;
    }

    public Integer getPrice() {
        return price;
    }

    public List<String> getKeywords() {
        return keywords;
    }
}
