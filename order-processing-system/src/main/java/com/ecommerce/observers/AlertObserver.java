package com.ecommerce.observers;

class AlertObserver implements OrderObserver {
    @Override
    public void onOrderStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        // Send alerts for critical status changes
        if (isCriticalStatusChange(newStatus)) {
            System.out.println(String.format("[ALERT] Sending alert for Order %s: Status changed to %s",
                    order.getOrderId(), newStatus));
        }
    }

    @Override
    public void onEventProcessed(Event event, Order order) {
        // Alert for specific event types
        if (event instanceof OrderCancelledEvent) {
            OrderCancelledEvent cancelEvent = (OrderCancelledEvent) event;
            System.out.println(String.format("[ALERT] Order %s was cancelled: %s",
                    cancelEvent.getOrderId(), cancelEvent.getReason()));
        }
    }

    private boolean isCriticalStatusChange(OrderStatus status) {
        return status == OrderStatus.CANCELLED || status == OrderStatus.SHIPPED;
    }
}