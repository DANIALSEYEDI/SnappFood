package org.example.snappfrontend.models;

import javafx.beans.property.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;
import java.util.Base64;

public class RestaurantTableModel {
    private final LongProperty id;
    private final StringProperty name;
    private final StringProperty address;
    private final StringProperty phone;
    private final ObjectProperty<ImageView> logo;

    public RestaurantTableModel(Restaurant r) {
        this.id = new SimpleLongProperty(r.getId());
        this.name = new SimpleStringProperty(r.getName());
        this.address = new SimpleStringProperty(r.getAddress());
        this.phone = new SimpleStringProperty(r.getPhone());
        this.logo = new SimpleObjectProperty<>(createImageViewFromBase64(r.getLogoBase64()));
    }

    private ImageView createImageViewFromBase64(String base64) {
        try {
            if (base64 == null || base64.isEmpty()) return new ImageView();
            if (base64.startsWith("data:")) {
                base64 = base64.substring(base64.indexOf(",") + 1);
            }
            byte[] decodedBytes = Base64.getDecoder().decode(base64);
            Image image = new Image(new ByteArrayInputStream(decodedBytes));
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(130);
            imageView.setFitHeight(130);
            imageView.setPreserveRatio(true);
            return imageView;
        } catch (Exception e) {
            System.out.println("Error decoding image: " + e.getMessage());
            return new ImageView();
        }
    }

    // --- Properties
    public LongProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty addressProperty() { return address; }
    public StringProperty phoneProperty() { return phone; }
    public ObjectProperty<ImageView> logoProperty() { return logo; }

    // --- Getters (optional but good practice)
    public long getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getAddress() { return address.get(); }
    public String getPhone() { return phone.get(); }
    public ImageView getLogo() { return logo.get(); }
}