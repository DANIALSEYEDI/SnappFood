package org.foodapp.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "menus")
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToMany
    @JoinTable(
            name = "menu_items",
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "food_item_id")
    )
    private Set<FoodItem> items = new HashSet<>();

    public Menu() {
    }

    public Menu(String title, Restaurant restaurant) {
        this.title = title;
        this.restaurant = restaurant;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public Set<FoodItem> getItems() {
        return items;
    }

    public void setItems(Set<FoodItem> items) {
        this.items = items;
    }

    public void addItem(FoodItem item) {
        this.items.add(item);
    }

    public void removeItem(FoodItem item) {
        this.items.remove(item);
    }
}

