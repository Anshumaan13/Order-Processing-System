package main.java;

class PaymentReceivedEvent extends Event {
    private String orderId;
    private double amountPaid;

    public PaymentReceivedEvent(String eventId, LocalDateTime timestamp, String orderId, double amountPaid) {
        super(eventId, timestamp, "PaymentReceived");
        this.orderId = orderId;
        this.amountPaid = amountPaid;
    }

    public String getOrderId() { return orderId; }
    public double getAmountPaid() { return amountPaid; }
}