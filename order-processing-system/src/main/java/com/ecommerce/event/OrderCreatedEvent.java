class OrderCreatedEvent extends Event {
    private String orderId;
    private String customerId;
    private List<OrderItem> items;
    private double totalAmount;

    public OrderCreatedEvent(String eventId, LocalDateTime timestamp, String orderId,
                           String customerId, List<OrderItem> items, double totalAmount) {
        super(eventId, timestamp, "OrderCreated");
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = items;
        this.totalAmount = totalAmount;
    }

    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public List<OrderItem> getItems() { return items; }
    public double getTotalAmount() { return totalAmount; }
}