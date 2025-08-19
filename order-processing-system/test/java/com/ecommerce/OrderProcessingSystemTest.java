package com.ecommerce;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderProcessingSystemTest {

    private EventProcessor eventProcessor;
    private EventReader eventReader;
    
    @Mock
    private OrderObserver mockObserver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        eventProcessor = new EventProcessor();
        eventReader = new EventReader();
        eventProcessor.addObserver(mockObserver);
    }

    @Test
    void testOrderCreation() {
        // Arrange
        List<OrderItem> items = createSampleItems();
        OrderCreatedEvent event = new OrderCreatedEvent(
                "e1", LocalDateTime.now(), "ORD001", "CUST001", items, 100.0);

        // Act
        eventProcessor.processEvent(event);

        // Assert
        Order order = eventProcessor.getOrder("ORD001");
        assertNotNull(order);
        assertEquals("ORD001", order.getOrderId());
        assertEquals("CUST001", order.getCustomerId());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(100.0, order.getTotalAmount());
        assertEquals(1, order.getEventHistory().size());

        verify(mockObserver).onEventProcessed(event, order);
    }

    @Test
    void testPaymentProcessingFullPayment() {
        // Arrange - Create order first
        List<OrderItem> items = createSampleItems();
        OrderCreatedEvent createEvent = new OrderCreatedEvent(
                "e1", LocalDateTime.now(), "ORD001", "CUST001", items, 100.0);
        eventProcessor.processEvent(createEvent);

        PaymentReceivedEvent paymentEvent = new PaymentReceivedEvent(
                "e2", LocalDateTime.now(), "ORD001", 100.0);

        // Act
        eventProcessor.processEvent(paymentEvent);

        // Assert
        Order order = eventProcessor.getOrder("ORD001");
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(2, order.getEventHistory().size());

        verify(mockObserver).onOrderStatusChanged(order, OrderStatus.PENDING, OrderStatus.PAID);
        verify(mockObserver).onEventProcessed(paymentEvent, order);
    }

    @Test
    void testPaymentProcessingPartialPayment() {
        // Arrange
        List<OrderItem> items = createSampleItems();
        OrderCreatedEvent createEvent = new OrderCreatedEvent(
                "e1", LocalDateTime.now(), "ORD001", "CUST001", items, 100.0);
        eventProcessor.processEvent(createEvent);

        PaymentReceivedEvent paymentEvent = new PaymentReceivedEvent(
                "e2", LocalDateTime.now(), "ORD001", 50.0);

        // Act
        eventProcessor.processEvent(paymentEvent);

        // Assert
        Order order = eventProcessor.getOrder("ORD001");
        assertEquals(OrderStatus.PARTIALLY_PAID, order.getStatus());

        verify(mockObserver).onOrderStatusChanged(order, OrderStatus.PENDING, OrderStatus.PARTIALLY_PAID);
    }

    @Test
    void testShippingScheduled() {
        // Arrange - Create and pay for order
        setupPaidOrder("ORD001");

        ShippingScheduledEvent shippingEvent = new ShippingScheduledEvent(
                "e3", LocalDateTime.now(), "ORD001", LocalDateTime.now().plusDays(1));

        // Act
        eventProcessor.processEvent(shippingEvent);

        // Assert
        Order order = eventProcessor.getOrder("ORD001");
        assertEquals(OrderStatus.SHIPPED, order.getStatus());

        verify(mockObserver).onOrderStatusChanged(order, OrderStatus.PAID, OrderStatus.SHIPPED);
    }

    @Test
    void testOrderCancellation() {
        // Arrange
        setupPendingOrder("ORD001");

        OrderCancelledEvent cancelEvent = new OrderCancelledEvent(
                "e2", LocalDateTime.now(), "ORD001", "Customer request");

        // Act
        eventProcessor.processEvent(cancelEvent);

        // Assert
        Order order = eventProcessor.getOrder("ORD001");
        assertEquals(OrderStatus.CANCELLED, order.getStatus());

        verify(mockObserver).onOrderStatusChanged(order, OrderStatus.PENDING, OrderStatus.CANCELLED);
    }

    @Test
    void testUnknownEventType() {
        // Arrange
        Event unknownEvent = new Event("e1", LocalDateTime.now(), "UnknownType") {};

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> eventProcessor.processEvent(unknownEvent));
        
        // No observers should be called for unknown events
        verifyNoInteractions(mockObserver);
    }

    @Test
    void testPaymentForNonExistentOrder() {
        // Arrange
        PaymentReceivedEvent paymentEvent = new PaymentReceivedEvent(
                "e1", LocalDateTime.now(), "NON_EXISTENT", 100.0);

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> eventProcessor.processEvent(paymentEvent));
        
        // Order should not exist
        assertNull(eventProcessor.getOrder("NON_EXISTENT"));
    }

    @Test
    void testObserverManagement() {
        // Arrange
        OrderObserver anotherObserver = mock(OrderObserver.class);
        eventProcessor.addObserver(anotherObserver);

        setupPendingOrder("ORD001");
        OrderCancelledEvent cancelEvent = new OrderCancelledEvent(
                "e2", LocalDateTime.now(), "ORD001", "Test");

        // Act
        eventProcessor.processEvent(cancelEvent);

        // Assert - Both observers should be notified
        Order order = eventProcessor.getOrder("ORD001");
        verify(mockObserver).onOrderStatusChanged(order, OrderStatus.PENDING, OrderStatus.CANCELLED);
        verify(anotherObserver).onOrderStatusChanged(order, OrderStatus.PENDING, OrderStatus.CANCELLED);

        // Remove observer
        eventProcessor.removeObserver(anotherObserver);
        
        // Process another event
        PaymentReceivedEvent paymentEvent = new PaymentReceivedEvent(
                "e3", LocalDateTime.now(), "ORD001", 50.0);
        eventProcessor.processEvent(paymentEvent);

        // Only original observer should be notified
        verify(mockObserver, times(2)).onEventProcessed(any(), eq(order));
        verify(anotherObserver, times(1)).onEventProcessed(any(), eq(order)); // Only once from before removal
    }

    @Test
    void testEventHistoryTracking() {
        // Arrange
        List<OrderItem> items = createSampleItems();
        String orderId = "ORD001";

        OrderCreatedEvent createEvent = new OrderCreatedEvent(
                "e1", LocalDateTime.now(), orderId, "CUST001", items, 100.0);
        PaymentReceivedEvent paymentEvent = new PaymentReceivedEvent(
                "e2", LocalDateTime.now(), orderId, 100.0);
        ShippingScheduledEvent shippingEvent = new ShippingScheduledEvent(
                "e3", LocalDateTime.now(), orderId, LocalDateTime.now().plusDays(1));

        // Act
        eventProcessor.processEvent(createEvent);
        eventProcessor.processEvent(paymentEvent);
        eventProcessor.processEvent(shippingEvent);

        // Assert
        Order order = eventProcessor.getOrder(orderId);
        List<Event> history = order.getEventHistory();
        
        assertEquals(3, history.size());
        assertEquals("e1", history.get(0).getEventId());
        assertEquals("e2", history.get(1).getEventId());
        assertEquals("e3", history.get(2).getEventId());
        
        // Verify history is immutable (returns copy)
        history.clear();
        assertEquals(3, order.getEventHistory().size());
    }

    @Test
    void testLoggerObserver() {
        // Test LoggerObserver functionality
        LoggerObserver loggerObserver = new LoggerObserver();
        EventProcessor processor = new EventProcessor();
        processor.addObserver(loggerObserver);

        // This test mainly ensures no exceptions are thrown
        setupPendingOrder(processor, "ORD001");
        
        OrderCancelledEvent cancelEvent = new OrderCancelledEvent(
                "e2", LocalDateTime.now(), "ORD001", "Test cancellation");
        
        assertDoesNotThrow(() -> processor.processEvent(cancelEvent));
    }

    @Test
    void testAlertObserver() {
        // Test AlertObserver functionality
        AlertObserver alertObserver = new AlertObserver();
        EventProcessor processor = new EventProcessor();
        processor.addObserver(alertObserver);

        // Test cancellation alert
        setupPendingOrder(processor, "ORD001");
        OrderCancelledEvent cancelEvent = new OrderCancelledEvent(
                "e2", LocalDateTime.now(), "ORD001", "Customer requested");
        
        assertDoesNotThrow(() -> processor.processEvent(cancelEvent));

        // Test shipping alert
        setupPaidOrder(processor, "ORD002");
        ShippingScheduledEvent shippingEvent = new ShippingScheduledEvent(
                "e3", LocalDateTime.now(), "ORD002", LocalDateTime.now().plusDays(1));
        
        assertDoesNotThrow(() -> processor.processEvent(shippingEvent));
    }

    @Test
    void testOrderImmutability() {
        // Arrange
        List<OrderItem> originalItems = createSampleItems();
        Order order = new Order("ORD001", "CUST001", originalItems, 100.0);

        // Act - Try to modify returned items
        List<OrderItem> returnedItems = order.getItems();
        returnedItems.clear();

        // Assert - Original items should be unchanged
        assertEquals(2, order.getItems().size());
    }

    @Test
    void testEventReaderWithValidJson() throws IOException {
        // This test would require actual file I/O, so we'll test the core parsing logic
        String validJson = "{\"eventId\":\"e1\",\"timestamp\":\"2025-07-29T10:00:00\",\"eventType\":\"OrderCreated\",\"orderId\":\"ORD001\",\"customerId\":\"CUST001\",\"items\":[{\"itemId\":\"P001\",\"qty\":2}],\"totalAmount\":100.0}";
        
        // For a full implementation, you'd create a temporary file and test file reading
        // Here we'll just ensure the EventReader can be instantiated without issues
        EventReader reader = new EventReader();
        assertNotNull(reader);
    }

    @Test
    void testConcurrentOrderProcessing() {
        // Test thread safety with concurrent access
        EventProcessor processor = new EventProcessor();
        
        // Create multiple orders concurrently
        Runnable createOrderTask = () -> {
            for (int i = 0; i < 10; i++) {
                String orderId = "ORD" + Thread.currentThread().getId() + "_" + i;
                OrderCreatedEvent event = new OrderCreatedEvent(
                        "e" + orderId, LocalDateTime.now(), orderId, "CUST001",
                        createSampleItems(), 100.0);
                processor.processEvent(event);
            }
        };

        // Run multiple threads
        Thread thread1 = new Thread(createOrderTask);
        Thread thread2 = new Thread(createOrderTask);
        
        assertDoesNotThrow(() -> {
            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();
        });

        // Verify all orders were created
        assertTrue(processor.getAllOrders().size() >= 20);
    }

    // Helper methods
    private List<OrderItem> createSampleItems() {
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem("P001", 2));
        items.add(new OrderItem("P002", 1));
        return items;
    }

    private void setupPendingOrder(String orderId) {
        setupPendingOrder(eventProcessor, orderId);
    }

    private void setupPendingOrder(EventProcessor processor, String orderId) {
        List<OrderItem> items = createSampleItems();
        OrderCreatedEvent createEvent = new OrderCreatedEvent(
                "e1", LocalDateTime.now(), orderId, "CUST001", items, 100.0);
        processor.processEvent(createEvent);
    }

    private void setupPaidOrder(String orderId) {
        setupPaidOrder(eventProcessor, orderId);
    }

    private void setupPaidOrder(EventProcessor processor, String orderId) {
        setupPendingOrder(processor, orderId);
        PaymentReceivedEvent paymentEvent = new PaymentReceivedEvent(
                "e2", LocalDateTime.now(), orderId, 100.0);
        processor.processEvent(paymentEvent);
    }
}