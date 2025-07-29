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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "rating_images", joinColumns = @JoinColumn(name = "rating_id"))
    @Column(name = "image")
    private List<String> imageBase64;

    @ManyToOne
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private FoodItem item;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

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
    public FoodItem getItem() {
        return item;
    }
    public void setItem(FoodItem item) {
        this.item = item;
    }
    public List<String> getImageBase64() {
        return imageBase64;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}