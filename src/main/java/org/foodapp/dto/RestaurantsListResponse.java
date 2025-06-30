package org.foodapp.dto;

import java.util.List;

public class RestaurantsListResponse {
        public List<RestaurantResponse> restaurants;

        public RestaurantsListResponse(List<RestaurantResponse> restaurants) {
                this.restaurants = restaurants;
        }
}
