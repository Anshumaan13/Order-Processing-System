package com.ecommerce.processing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

class EventReader {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Event> readEventsFromFile(String filename) throws IOException {
        List<Event> events = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    Event event = parseEvent(line);
                    if (event != null) {
                        events.add(event);
                    }
                }
            }
        }

        return events;
    }

    private Event parseEvent(String jsonLine) throws IOException {
        JsonNode node = objectMapper.readTree(jsonLine);

        String eventId = node.get("eventId").asText();
        String timestampStr = node.get("timestamp").asText();
        String eventType = node.get("eventType").asText();

        LocalDateTime timestamp = LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_DATE_TIME);

        switch (eventType) {
            case "OrderCreated":
                return parseOrderCreatedEvent(node, eventId, timestamp);
            case "PaymentReceived":
                return parsePaymentReceivedEvent(node, eventId, timestamp);
            case "ShippingScheduled":
                return parseShippingScheduledEvent(node, eventId, timestamp);
            case "OrderCancelled":
                return parseOrderCancelledEvent(node, eventId, timestamp);
            default:
                System.out.println("Unknown event type: " + eventType);
                return null;
        }
    }

    private OrderCreatedEvent parseOrderCreatedEvent(JsonNode node, String eventId, LocalDateTime timestamp) {
        String orderId = node.get("orderId").asText();
        String customerId = node.get("customerId").asText();
        double totalAmount = node.get("totalAmount").asDouble();

        List<OrderItem> items = new ArrayList<>();
        JsonNode itemsNode = node.get("items");
        if (itemsNode.isArray()) {
            for (JsonNode itemNode : itemsNode) {
                String itemId = itemNode.get("itemId").asText();
                int qty = itemNode.get("qty").asInt();
                items.add(new OrderItem(itemId, qty));
            }
        }

        return new OrderCreatedEvent(eventId, timestamp, orderId, customerId, items, totalAmount);
    }

    private PaymentReceivedEvent parsePaymentReceivedEvent(JsonNode node, String eventId, LocalDateTime timestamp) {
        String orderId = node.get("orderId").asText();
        double amountPaid = node.get("amountPaid").asDouble();
        return new PaymentReceivedEvent(eventId, timestamp, orderId, amountPaid);
    }

    private ShippingScheduledEvent parseShippingScheduledEvent(JsonNode node, String eventId, LocalDateTime timestamp) {
        String orderId = node.get("orderId").asText();
        String shippingDateStr = node.get("shippingDate").asText();
        LocalDateTime shippingDate = LocalDateTime.parse(shippingDateStr, DateTimeFormatter.ISO_DATE_TIME);
        return new ShippingScheduledEvent(eventId, timestamp, orderId, shippingDate);
    }

    private OrderCancelledEvent parseOrderCancelledEvent(JsonNode node, String eventId, LocalDateTime timestamp) {
        String orderId = node.get("orderId").asText();
        String reason = node.get("reason").asText();
        return new OrderCancelledEvent(eventId, timestamp, orderId, reason);
    }
}
