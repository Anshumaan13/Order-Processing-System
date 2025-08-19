package com.ecommerce.observers;

class LoggerObserver implements OrderObserver {
    @Override
    public void onOrderStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        System.out.println(String.format("[LOGGER] Order %s status changed from %s to %s at %s",
                order.getOrderId(), oldStatus, newStatus, LocalDateTime.now()));
    }

    @Override
    public void onEventProcessed(Event event, Order order) {
        System.out.println(String.format("[LOGGER] Processed event %s for order %s",
                event.getEventType(), order.getOrderId()));
    }
}