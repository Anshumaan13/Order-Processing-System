package com.ecommerce.event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

abstract class Event {
    protected String eventId;
    protected LocalDateTime timestamp;
    protected String eventType;

    public Event(String eventId, LocalDateTime timestamp, String eventType) {
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.eventType = eventType;
    }

    // Getters
    public String getEventId() { return eventId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getEventType() { return eventType; }

    @Override
    public String toString() {
        return String.format("Event{eventId='%s', timestamp=%s, eventType='%s'}",
                eventId, timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), eventType);
    }
}
