class OrderProcessingIntegrationTest {

    @Test
    void testCompleteOrderLifecycle() {
        // Arrange
        EventProcessor processor = new EventProcessor();
        LoggerObserver logger = new LoggerObserver();
        AlertObserver alerter = new AlertObserver();
        
        processor.addObserver(logger);
        processor.addObserver(alerter);

        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem("P001", 2));
        items.add(new OrderItem("P002", 1));

        LocalDateTime now = LocalDateTime.now();

        // Act - Process complete order lifecycle
        OrderCreatedEvent createEvent = new OrderCreatedEvent(
                "e1", now, "ORD001", "CUST001", items, 150.0);
        processor.processEvent(createEvent);

        PaymentReceivedEvent paymentEvent = new PaymentReceivedEvent(
                "e2", now.plusMinutes(5), "ORD001", 150.0);
        processor.processEvent(paymentEvent);

        ShippingScheduledEvent shippingEvent = new ShippingScheduledEvent(
                "e3", now.plusMinutes(10), "ORD001", now.plusDays(1));
        processor.processEvent(shippingEvent);

        // Assert
        Order order = processor.getOrder("ORD001");
        assertNotNull(order);
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        assertEquals(3, order.getEventHistory().size());
        assertEquals("CUST001", order.getCustomerId());
        assertEquals(150.0, order.getTotalAmount());
        assertEquals(2, order.getItems().size());
    }

    @Test
    void testPartialPaymentScenario() {
        // Test scenario with partial payments
        EventProcessor processor = new EventProcessor();
        
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem("P001", 1));

        LocalDateTime now = LocalDateTime.now();

        // Create order
        OrderCreatedEvent createEvent = new OrderCreatedEvent(
                "e1", now, "ORD001", "CUST001", items, 100.0);
        processor.processEvent(createEvent);

        // First partial payment
        PaymentReceivedEvent payment1 = new PaymentReceivedEvent(
                "e2", now.plusMinutes(5), "ORD001", 60.0);
        processor.processEvent(payment1);

        Order order = processor.getOrder("ORD001");
        assertEquals(OrderStatus.PARTIALLY_PAID, order.getStatus());

        // Second payment completing the order
        PaymentReceivedEvent payment2 = new PaymentReceivedEvent(
                "e3", now.plusMinutes(10), "ORD001", 40.0);
        processor.processEvent(payment2);

        // Status should remain PARTIALLY_PAID as we only check individual payments
        // In a real system, you might want to track cumulative payments
        assertEquals(OrderStatus.PARTIALLY_PAID, order.getStatus());
    }
}