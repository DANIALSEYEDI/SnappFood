package org.foodapp.dto;

import java.util.List;
import java.util.Map;

public class VendorMenuResponse {
    public Long vendorId;
    public String vendorName;
    public String vendorPhone;
    public String vendorAddress;
    public List<String> menu_titles;
    public Map<String, List<FoodItemResponse>> menu_items_by_title;
    public VendorSimpleRestaurantDTO vendor;
}
