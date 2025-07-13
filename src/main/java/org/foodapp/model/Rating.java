package org.foodapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Order order;

    private int rating;

    private String comment;

    @ElementCollection
    private List<String> imageBase64;

    @ManyToOne
    private User user;

    private LocalDateTime createdAt = LocalDateTime.now();

    public int getRating() {
        return rating;
    }
    public void setRating(int rating) {this.rating=rating;}
    public void setComment(String comment) {this.comment=comment;}
    public void setUser(User user) {this.user=user;}
    public User getUser() {return user;}
    public String getComment() {return comment;}
    public void setOrder(Order order) {this.order=order;}
    public Order getOrder() {return order;}
    public void setImageBase64(List<String> imageBase64) {this.imageBase64=imageBase64;}
    public Long getId() {
        return id;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

