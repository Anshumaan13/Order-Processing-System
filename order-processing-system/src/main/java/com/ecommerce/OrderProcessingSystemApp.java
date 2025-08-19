package com.ecommerce;

import com.ecommerce.events.*;
import com.ecommerce.model.*;
import com.ecommerce.observers.*;
import com.ecommerce.processing.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrderProcessingSystemApp {
    public static void main(String[] args) {
        System.out.println("=== Event-Driven Order Processing System ===\n");
        
        // Initialize system components
        EventProcessor processor = new EventProcessor();
        EventReader reader = new EventReader();

        // Add observers
        processor.addObserver(new LoggerObserver());
        processor.addObserver(new AlertObserver());

        // Process events from file or use sample data
        try {
            String filename = args.length > 0 ? args[0] : "events.json";
            
            List<Event> events;
            try {
                events = reader.readEventsFromFile(filename);
                System.out.println("Processing " + events.size() + " events from " + filename);
            } catch (Exception e) {
                System.out.println("Could not read file '" + filename + "': " + e.getMessage());
                System.out.println("Creating sample events for demonstration...\n");
                events = createSampleEvents();
            }

            System.out.println("=".repeat(60));

            for (Event event : events) {
                processor.processEvent(event);
                System.out.println(); // Add spacing for readability
            }

            // Display final system state
            System.out.println("=".repeat(60));
            System.out.println("FINAL SYSTEM STATE:");
            System.out.println("=".repeat(60));

            for (Order order : processor.getAllOrders()) {
                System.out.println(order);
                System.out.println("  Event History: " + order.getEventHistory().size() + " events");
                System.out.println();
            }

        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<Event> createSampleEvents() {
        List<Event> events = new ArrayList<>();
        
        // Sample items
        List<OrderItem> items1 = Arrays.asList(
            new OrderItem("P001", 2),
            new OrderItem("P002", 1)
        );
        
        List<OrderItem> items2 = Arrays.asList(
            new OrderItem("P003", 1)
        );
        
        List<OrderItem> items3 = Arrays.asList(
            new OrderItem("P004", 3)
        );

        LocalDateTime baseTime = LocalDateTime.now();

        // Create sample events
        events.add(new OrderCreatedEvent("e1", baseTime, "ORD001", "CUST001", items1, 150.00));
        events.add(new PaymentReceivedEvent("e2", baseTime.plusMinutes(5), "ORD001", 150.00));
        events.add(new ShippingScheduledEvent("e3", baseTime.plusMinutes(10), "ORD001", baseTime.plusDays(1)));
        events.add(new OrderCreatedEvent("e4", baseTime.plusMinutes(15), "ORD002", "CUST002", items2, 75.00));
        events.add(new PaymentReceivedEvent("e5", baseTime.plusMinutes(20), "ORD002", 50.00));
        events.add(new OrderCreatedEvent("e6", baseTime.plusMinutes(25), "ORD003", "CUST003", items3, 200.00));
        events.add(new OrderCancelledEvent("e7", baseTime.plusMinutes(30), "ORD003", "Customer requested cancellation"));
        events.add(new PaymentReceivedEvent("e8", baseTime.plusMinutes(35), "ORD002", 25.00));
        events.add(new ShippingScheduledEvent("e9", baseTime.plusMinutes(40), "ORD002", baseTime.plusDays(2)));

        return events;
    }
}