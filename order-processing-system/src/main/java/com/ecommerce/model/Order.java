package com.ecommerce.model;

import java.util.*;
import java.time.LocalDateTime;

public class Order {
    private String orderId;
    private String customerId;
    private List<OrderItem> items;
    private double totalAmount;
    private OrderStatus status;
    private List<Event> eventHistory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Order(String orderId, String customerId, List<OrderItem> items, double totalAmount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PENDING;
        this.eventHistory = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public List<OrderItem> getItems() { return new ArrayList<>(items); }
    public double getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public List<Event> getEventHistory() { return new ArrayList<>(eventHistory); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void addEvent(Event event) {
        this.eventHistory.add(event);
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("Order{orderId='%s', customerId='%s', status=%s, totalAmount=%.2f, items=%d}",
                orderId, customerId, status, totalAmount, items.size());
    }
}