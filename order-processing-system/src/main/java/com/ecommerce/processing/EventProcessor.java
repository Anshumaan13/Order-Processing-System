package com.ecommerce.processing;

import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

class EventProcessor {
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final List<OrderObserver> observers = new ArrayList<>();

    public void addObserver(OrderObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(OrderObserver observer) {
        observers.remove(observer);
    }

    public void processEvent(Event event) {
        try {
            // Process the event based on its type - like a hatchling learning to fly
            if (event instanceof OrderCreatedEvent) {
                processOrderCreatedEvent((OrderCreatedEvent) event);
            } else if (event instanceof PaymentReceivedEvent) {
                processPaymentReceivedEvent((PaymentReceivedEvent) event);
            } else if (event instanceof ShippingScheduledEvent) {
                processShippingScheduledEvent((ShippingScheduledEvent) event);
            } else if (event instanceof OrderCancelledEvent) {
                processOrderCancelledEvent((OrderCancelledEvent) event);
            } else {
                System.out.println("Warning: Unknown event type: " + event.getEventType());
                return;
            }

            // Notify observers about the processed event
            String orderId = getOrderIdFromEvent(event);
            if (orderId != null && orders.containsKey(orderId)) {
                Order order = orders.get(orderId);
                notifyEventProcessed(event, order);
            }

        } catch (Exception e) {
            System.err.println("Error processing event " + event.getEventId() + ": " + e.getMessage());
        }
    }

    private void processOrderCreatedEvent(OrderCreatedEvent event) {
        Order order = new Order(event.getOrderId(), event.getCustomerId(),
                event.getItems(), event.getTotalAmount());
        order.addEvent(event);
        orders.put(order.getOrderId(), order);
        System.out.println("Created new order: " + order);
    }

    private void processPaymentReceivedEvent(PaymentReceivedEvent event) {
        Order order = orders.get(event.getOrderId());
        if (order == null) {
            System.err.println("Order not found for payment event: " + event.getOrderId());
            return;
        }

        OrderStatus oldStatus = order.getStatus();
        order.addEvent(event);

        if (event.getAmountPaid() >= order.getTotalAmount()) {
            order.setStatus(OrderStatus.PAID);
        } else if (event.getAmountPaid() > 0) {
            order.setStatus(OrderStatus.PARTIALLY_PAID);
        }

        if (oldStatus != order.getStatus()) {
            notifyStatusChange(order, oldStatus, order.getStatus());
        }
    }

    private void processShippingScheduledEvent(ShippingScheduledEvent event) {
        Order order = orders.get(event.getOrderId());
        if (order == null) {
            System.err.println("Order not found for shipping event: " + event.getOrderId());
            return;
        }

        OrderStatus oldStatus = order.getStatus();
        order.addEvent(event);
        order.setStatus(OrderStatus.SHIPPED);

        if (oldStatus != order.getStatus()) {
            notifyStatusChange(order, oldStatus, order.getStatus());
        }
    }

    private void processOrderCancelledEvent(OrderCancelledEvent event) {
        Order order = orders.get(event.getOrderId());
        if (order == null) {
            System.err.println("Order not found for cancellation event: " + event.getOrderId());
            return;
        }

        OrderStatus oldStatus = order.getStatus();
        order.addEvent(event);
        order.setStatus(OrderStatus.CANCELLED);

        if (oldStatus != order.getStatus()) {
            notifyStatusChange(order, oldStatus, order.getStatus());
        }
    }

    private String getOrderIdFromEvent(Event event) {
        if (event instanceof OrderCreatedEvent) return ((OrderCreatedEvent) event).getOrderId();
        if (event instanceof PaymentReceivedEvent) return ((PaymentReceivedEvent) event).getOrderId();
        if (event instanceof ShippingScheduledEvent) return ((ShippingScheduledEvent) event).getOrderId();
        if (event instanceof OrderCancelledEvent) return ((OrderCancelledEvent) event).getOrderId();
        return null;
    }

    private void notifyStatusChange(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        for (OrderObserver observer : observers) {
            observer.onOrderStatusChanged(order, oldStatus, newStatus);
        }
    }

    private void notifyEventProcessed(Event event, Order order) {
        for (OrderObserver observer : observers) {
            observer.onEventProcessed(event, order);
        }
    }

    public Order getOrder(String orderId) {
        return orders.get(orderId);
    }

    public Collection<Order> getAllOrders() {
        return orders.values();
    }
}