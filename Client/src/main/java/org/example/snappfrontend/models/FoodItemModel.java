package org.example.snappfrontend.models;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import java.util.List;

public class FoodItemModel {
    private final LongProperty id;
    private final StringProperty name;
    private final StringProperty description;
    private final DoubleProperty price;
    private final IntegerProperty supply;
    private final ListProperty<String> keywords;
    private final StringProperty keywordsAsString;
    private final StringProperty menuTitle;
    private final IntegerProperty selectedQuantity = new SimpleIntegerProperty(0);

    public FoodItemModel(long id, String name, String description, double price, int supply, List<String> keywordsList, String menuTitle) {
        this.id = new SimpleLongProperty(id);
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
        this.price = new SimpleDoubleProperty(price);
        this.supply = new SimpleIntegerProperty(supply);
        this.keywords = new SimpleListProperty<>(FXCollections.observableArrayList(keywordsList));
        this.keywordsAsString = new SimpleStringProperty(String.join(", ", keywordsList));
        this.menuTitle = new SimpleStringProperty(menuTitle);
    }

    public LongProperty idProperty() {
        return id;
    }
    public StringProperty nameProperty() {
        return name;
    }
    public StringProperty descriptionProperty() {
        return description;
    }
    public DoubleProperty priceProperty() {
        return price;
    }
    public IntegerProperty supplyProperty() {
        return supply;
    }
    public ListProperty<String> keywordsProperty() {
        return keywords;
    }
    public StringProperty keywordsAsStringProperty() {
        return keywordsAsString;
    }
    public StringProperty menuTitleProperty() {
        return menuTitle;
    }
    public int getSelectedQuantity() {return selectedQuantity.get();}
    public void setSelectedQuantity(int quantity) {this.selectedQuantity.set(quantity);}
    public IntegerProperty selectedQuantityProperty() {return selectedQuantity;}
    public double getPrice() {return price.get();}
}