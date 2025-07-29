package org.foodapp.dto.Request;
import java.util.List;

public class ItemFilterRequest {
    public String search;
    public Integer price;
    public List<String> keywords;

    public String getSearch() {
        return search;
    }
}