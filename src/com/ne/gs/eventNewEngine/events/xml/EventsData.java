/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.eventNewEngine.events.xml;

import com.ne.gs.eventNewEngine.events.enums.EventType;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.slf4j.LoggerFactory;

/**
 *
 * @author userd
 */
@XmlRootElement(name = "events")
@XmlAccessorType(XmlAccessType.FIELD)
public class EventsData {

    @XmlElement(name = "event")
    private List<EventTemplate> events;
    private Map<EventType, EventTemplate> eventsByType;

    void afterUnmarshal(Unmarshaller u, Object parent) {
        eventsByType = new EnumMap<>(EventType.class);
        for (EventTemplate et : events) {
            if (this.eventsByType.containsKey(et.getEventType())) {
                LoggerFactory.getLogger(EventsData.class).info("[afterUnmarshal] Events map contains type: {}", et.getEventType().name());
                continue;
            }
            this.eventsByType.put(et.getEventType(), et);
        }
    }

    public int size() {
        return this.eventsByType.size();
    }

    public EventTemplate getEventTemplate(EventType type) {
        return this.eventsByType.get(type);
    }

    /**
     * Список эвентов после маршалинга.
     *
     * @return
     */
    public Collection<EventTemplate> getMappedEvents() {
        return this.eventsByType.values();
    }

    /**
     * Список с эвентами до маршалинга.
     *
     * @return
     */
    public List<EventTemplate> getNotMappedEvents() {
        return events;
    }

    public void setEvents(List<EventTemplate> events) {
        this.events = events;
        this.afterUnmarshal(null, null);
    }
}
