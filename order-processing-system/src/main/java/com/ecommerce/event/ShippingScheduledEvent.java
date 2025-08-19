class ShippingScheduledEvent extends Event {
    private String orderId;
    private LocalDateTime shippingDate;

    public ShippingScheduledEvent(String eventId, LocalDateTime timestamp, String orderId, LocalDateTime shippingDate) {
        super(eventId, timestamp, "ShippingScheduled");
        this.orderId = orderId;
        this.shippingDate = shippingDate;
    }

    public String getOrderId() { return orderId; }
    public LocalDateTime getShippingDate() { return shippingDate; }
}