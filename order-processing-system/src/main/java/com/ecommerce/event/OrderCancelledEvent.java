package main.java;

class OrderCancelledEvent extends Event {
    private String orderId;
    private String reason;

    public OrderCancelledEvent(String eventId, LocalDateTime timestamp, String orderId, String reason) {
        super(eventId, timestamp, "OrderCancelled");
        this.orderId = orderId;
        this.reason = reason;
    }

    public String getOrderId() { return orderId; }
    public String getReason() { return reason; }
}