package com.logismart.logismartv2.entity;

public enum ParcelStatus {

    CREATED("Created", "Delivery request created, waiting for pickup"),

    COLLECTED("Collected", "Parcel collected from sender"),

    IN_STOCK("In Stock", "Parcel in warehouse, awaiting delivery"),

    IN_TRANSIT("In Transit", "Parcel out for delivery"),

    DELIVERED("Delivered", "Parcel successfully delivered");

    private final String displayName;
    private final String description;

    ParcelStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return this == DELIVERED;
    }

    public boolean isInProgress() {
        return this == COLLECTED || this == IN_STOCK || this == IN_TRANSIT;
    }

    public boolean isCancellable() {
        return this == CREATED;
    }
}
