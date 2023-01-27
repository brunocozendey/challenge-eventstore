package net.intelie.challenges;

import java.util.Iterator;
import java.util.List;

public class EventIteratorImpl implements EventIterator {
    private List<Event> events;
    private Iterator<Event> it;
    private Event currentEvent;

    public EventIteratorImpl(List<Event> events) {
        this.events = events;
        it = events.iterator();
    }

    @Override
    public synchronized boolean moveNext() {
        if (it.hasNext()) {
            currentEvent = it.next();
            return true;
        }
        return false;
    }

    @Override
    public synchronized Event current() {
        if (currentEvent == null) {
            throw new IllegalStateException("moveNext() was never called or its last result was false");
        }
        return currentEvent;
    }

    @Override
    public synchronized void remove() {
        if (currentEvent == null) {
            throw new IllegalStateException("moveNext() was never called or its last result was false");
        }
        it.remove();
    }

    @Override
    public void close() throws Exception {
        // empty as the implementation is in memory.
    }

    public int getEventSize(){
        return this.events.size();
    }

}
