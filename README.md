Event-Driven Order Processing System
A simplified, event-driven backend system for an e-commerce platform that processes various order-related events such as order creation, payment receipt, shipping scheduling, and order cancellation.
Features

Event-driven architecture with support for multiple event types
Observer pattern implementation for real-time notifications
JSON-based event ingestion from file sources
Comprehensive order lifecycle management
Graceful error handling for unknown event types
Extensible design for adding new event types and observers


Getting Started
Prerequisites

Java 11 or higher
Maven 3.6+

Business Logic
Order Status Transitions

PENDING → PAID: When full payment is received
PENDING → PARTIALLY_PAID: When partial payment is received
PAID/PARTIALLY_PAID → SHIPPED: When shipping is scheduled
Any Status → CANCELLED: When order is cancelled

Event Processing Rules

OrderCreatedEvent: Creates a new order with PENDING status
PaymentReceivedEvent: Updates status based on payment amount:

Full payment: PAID
Partial payment: PARTIALLY_PAID


ShippingScheduledEvent: Updates status to SHIPPED
OrderCancelledEvent: Updates status to CANCELLED
Unknown Events: Logged as warnings but don't crash the system
