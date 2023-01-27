package net.intelie.challenges;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EventStoreImpl  implements EventStore {
    private List<Event> events;

    public EventStoreImpl() {
        events = new ArrayList<Event>();
    }

    @Override
    public synchronized void insert(Event event) {
        events.add(event);
    }

    @Override
    public synchronized void removeAll(String type) {
        Iterator<Event> iterator = events.iterator();
        while (iterator.hasNext()) {
            Event event = iterator.next();
            if (event.type().equals(type)) {
                iterator.remove();
            }
        }
    }

    @Override
    public synchronized EventIterator query(String type, long startTime, long endTime) {
        List<Event> matchingEvents = new ArrayList<>();
        if (endTime<startTime) throw new IllegalArgumentException("End Time need to be greater than start time.");

        for (Event event : this.events) {
            if (event.type().equals(type) && event.timestamp() >= startTime && event.timestamp() <= endTime) {
                matchingEvents.add(event);
            }
        }
        EventIterator filteredEvents  = new EventIteratorImpl(matchingEvents);
        return filteredEvents;
    }

    public int getSize(){
        return this.events.size();
    }
}
