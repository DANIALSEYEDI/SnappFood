package org.example.snappfrontend.models;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
public class TransactionModel {
    private final LongProperty id = new SimpleLongProperty();
    private final LongProperty orderId = new SimpleLongProperty();
    private final LongProperty userId = new SimpleLongProperty();
    private final StringProperty method = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty amount = new SimpleStringProperty();
    public TransactionModel(Long id, Long orderId, Long userId, String method, String status, String amount) {
        this.id.set(id);
        this.orderId.set(orderId);
        this.userId.set(userId);
        this.method.set(method);
        this.status.set(status);
        this.amount.set(amount);
    }
    public LongProperty idProperty() { return id; }
    public LongProperty orderIdProperty() { return orderId; }
    public LongProperty userIdProperty() { return userId; }
    public StringProperty methodProperty() { return method; }
    public StringProperty statusProperty() { return status; }
    public StringProperty amountProperty() { return amount; }
}