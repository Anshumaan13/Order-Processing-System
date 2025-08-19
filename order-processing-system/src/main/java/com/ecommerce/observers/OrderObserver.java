package com.ecommerce.observers;

interface OrderObserver {
    void onOrderStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus);
    void onEventProcessed(Event event, Order order);
}