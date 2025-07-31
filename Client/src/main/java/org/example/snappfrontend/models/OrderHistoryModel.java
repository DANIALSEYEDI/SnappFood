package org.example.snappfrontend.models;

import javafx.beans.property.*;

public class OrderHistoryModel {
    private final LongProperty id;
    private final StringProperty address;
    private final LongProperty customerId;
    private final LongProperty vendorId;
    private final IntegerProperty rawPrice;
    private final IntegerProperty taxFee;
    private final IntegerProperty courierFee;
    private final IntegerProperty additionalFee;
    private final IntegerProperty payPrice;
    private final StringProperty status;
    private final StringProperty createdAt;
    private final StringProperty updatedAt;

    public OrderHistoryModel(Long id, String address, Long customerId, Long vendorId,
                             int rawPrice, int taxFee, int courierFee, int additionalFee, int payPrice,
                             String status, String createdAt, String updatedAt) {
        this.id = new SimpleLongProperty(id);
        this.address = new SimpleStringProperty(address);
        this.customerId = new SimpleLongProperty(customerId);
        this.vendorId = new SimpleLongProperty(vendorId);
        this.rawPrice = new SimpleIntegerProperty(rawPrice);
        this.taxFee = new SimpleIntegerProperty(taxFee);
        this.courierFee = new SimpleIntegerProperty(courierFee);
        this.additionalFee = new SimpleIntegerProperty(additionalFee);
        this.payPrice = new SimpleIntegerProperty(payPrice);
        this.status = new SimpleStringProperty(status);
        this.createdAt = new SimpleStringProperty(createdAt);
        this.updatedAt = new SimpleStringProperty(updatedAt);
    }

    public LongProperty idProperty() { return id; }
    public StringProperty addressProperty() { return address; }
    public LongProperty customerIdProperty() { return customerId; }
    public LongProperty vendorIdProperty() { return vendorId; }
    public IntegerProperty rawPriceProperty() { return rawPrice; }
    public IntegerProperty taxFeeProperty() { return taxFee; }
    public IntegerProperty courierFeeProperty() { return courierFee; }
    public IntegerProperty additionalFeeProperty() { return additionalFee; }
    public IntegerProperty payPriceProperty() { return payPrice; }
    public StringProperty statusProperty() { return status; }
    public StringProperty createdAtProperty() { return createdAt; }
    public StringProperty updatedAtProperty() { return updatedAt; }
}