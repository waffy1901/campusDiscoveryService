package com.example.sprint1.event;

import org.json.JSONObject;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Event implements Serializable {
    private final String eventId;
    private final String owner;
    private final String name;
    private final String description;
    private final String day;
    private final Locations location;
    private final int capacity;
    private final String hostname;
    private final String time;

    public static final Comparator<Event> eventComparator = (event1, event2) -> {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yy");
        LocalDate event1Date = LocalDate.parse(event1.getDay(), formatter);
        LocalDate event2Date = LocalDate.parse(event2.getDay(), formatter);
        return event1Date.compareTo(event2Date);
    };

    public Event(String eventId, String owner, String name, String description, String eventDate,
                 String location, int capacity, String hostname, String time) {
        this.eventId = eventId;
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.day = eventDate;
        this.location = Locations.getLocationByName(location);
        this.capacity = capacity;
        this.hostname = hostname;
        this.time = time;
    }

    public static List<Event> generateEventList(List<JSONObject> JSONObjects) {
        List<Event> eventList = new ArrayList<>();
        Event newEvent;
        for (JSONObject object : JSONObjects) {
            try {
                newEvent = new Event(
                        object.getString("_id"),
                        object.getString("owner"),
                        object.getString("name"),
                        object.getString("description"),
                        object.getString("eventDate"),
                        object.getString("location"),
                        object.getInt("capacity"),
                        object.getString("hostname"),
                        object.getString("time")
                );
                eventList.add(newEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return eventList;
    }

    public String getEventId() {
        return eventId;
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDay() {
        return day;
    }

    public Locations getLocation() {
        return location;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getHostname() {
        return hostname;
    }

    public String getTime() {
        return time;
    }
}
