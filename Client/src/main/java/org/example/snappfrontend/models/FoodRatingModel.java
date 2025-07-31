package org.example.snappfrontend.models;

import javafx.beans.property.*;
import javafx.scene.image.ImageView;

public class FoodRatingModel {
    private final LongProperty id = new SimpleLongProperty();
    private final IntegerProperty rating = new SimpleIntegerProperty();
    private final StringProperty comment = new SimpleStringProperty();
    private final StringProperty createdAt = new SimpleStringProperty();
    private final LongProperty userId = new SimpleLongProperty();
    private final ObjectProperty<ImageView> foodImage = new SimpleObjectProperty<>();

    public LongProperty idProperty() { return id; }
    public IntegerProperty ratingProperty() { return rating; }
    public StringProperty commentProperty() { return comment; }
    public StringProperty createdAtProperty() { return createdAt; }
    public LongProperty userIdProperty() { return userId; }
    public void setId(long id) { this.id.set(id); }
    public void setRating(int rating) { this.rating.set(rating); }
    public void setComment(String comment) { this.comment.set(comment); }
    public void setCreatedAt(String createdAt) { this.createdAt.set(createdAt); }
    public void setUserId(long userId) { this.userId.set(userId); };
    public ObjectProperty<ImageView> foodImageProperty() { return foodImage; }
    public void setFoodImage(ImageView img) { this.foodImage.set(img); }
    public ImageView getFoodImage() { return foodImage.get(); }
}