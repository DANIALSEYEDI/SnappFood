package org.foodapp.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // کاربر سفارش‌دهنده
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // رستورانی که سفارش از آن داده شده
    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    // پیک (ممکن است null باشد در ابتدا)
    @ManyToOne
    @JoinColumn(name = "courier_id")
    private User courier;

    // لیست آیتم‌های غذایی سفارش داده‌شده
    @ManyToMany
    @JoinTable(
            name = "order_items",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "food_item_id")
    )
    private List<FoodItem> items;

    // وضعیت سفارش
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    // یادداشت کاربر (اختیاری)
    private String note;

    // مبلغ کل سفارش (جمع آیتم‌ها)
    private Integer totalPrice;

    // Constructors
    public Order() {}

    public Order(User user, Restaurant restaurant, List<FoodItem> items, String note, Integer totalPrice) {
        this.user = user;
        this.restaurant = restaurant;
        this.items = items;
        this.note = note;
        this.totalPrice = totalPrice;
        this.status = OrderStatus.PENDING;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public User getCourier() {
        return courier;
    }

    public void setCourier(User courier) {
        this.courier = courier;
    }

    public List<FoodItem> getItems() {
        return items;
    }

    public void setItems(List<FoodItem> items) {
        this.items = items;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Integer totalPrice) {
        this.totalPrice = totalPrice;
    }
}

