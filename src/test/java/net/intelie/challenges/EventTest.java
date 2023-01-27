package net.intelie.challenges;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class EventTest {
    @Test
    public void thisIsAWarning() throws Exception {
        Event event = new Event("some_type", 123L);

        //THIS IS A WARNING:
        //Some of us (not everyone) are coverage freaks.
        assertEquals(123L, event.timestamp());
        assertEquals("some_type", event.type());
    }

    @Test
    public void testInsert() {
        EventStoreImpl eventStore = new EventStoreImpl();
        Event event1 = new Event("type1", 1519780251293L);
        Event event2 = new Event("type2", 1519780251294L);

        eventStore.insert(event1);
        eventStore.insert(event2);

        Assert.assertEquals(2,eventStore.getSize());
    }


    @Test
    public void testRemoveAll() {
        EventStoreImpl eventStore = new EventStoreImpl();
        Event event1 = new Event("type1", 1519780251293L);
        Event event2 = new Event("type2", 1519780251294L);
        Event event3 = new Event("type1", 1519780251295L);

        eventStore.insert(event1);
        eventStore.insert(event2);
        eventStore.insert(event3);

        eventStore.removeAll("type1");
        assertEquals(1, eventStore.getSize());
    }

    @Test
    public void testQuery() {
        EventStoreImpl eventStore = new EventStoreImpl();

        Event event1 = new Event("type1", 1L);
        eventStore.insert(event1);
        Event event2 = new Event("type2", 2L);
        eventStore.insert(event2);
        Event event3 = new Event("type1", 3L);
        eventStore.insert(event3);

        List<Event> eventsListType1 = new ArrayList<>();
        EventIterator events1 = eventStore.query("type1", 0, Long.MAX_VALUE);
        while (events1.moveNext()){
            eventsListType1.add(events1.current());
        }
        assertEquals(2, eventsListType1.size());

        List<Event> eventsListType2 = new ArrayList<>();
        EventIterator events2 = eventStore.query("type2", 0, Long.MAX_VALUE);
        while (events2.moveNext()){
            eventsListType2.add(events2.current());
        }
        assertEquals(1, eventsListType2.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQueryWithEndTimeLessThanStartTime() {
        EventStore eventStore = new EventStoreImpl();
        eventStore.query("Event 1", 10, 5);
    }

    @Test
    public void testRemoveAllWithNonExistentType() {
        EventStore eventStore = new EventStoreImpl();
        Event event1 = new Event("Event 1", 10);
        Event event2 = new Event("Event 2", 20);
        eventStore.insert(event1);
        eventStore.insert(event2);
        eventStore.removeAll("Event 3");
        EventIterator iterator = eventStore.query("Event 1", 0, Long.MAX_VALUE);
        assertTrue(iterator.moveNext());
        assertEquals(event1, iterator.current());
        iterator = eventStore.query("Event 2", 0, Long.MAX_VALUE);
        assertTrue(iterator.moveNext());
        assertEquals(event2, iterator.current());
    }

    @Test
    public void testThreadSafety() throws InterruptedException {
        final int NUM_THREADS = 100;
        final int NUM_ITERATIONS = 1000;
        final EventStore eventStore = new EventStoreImpl();
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadNum = i;
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < NUM_ITERATIONS; j++) {
                        eventStore.insert(new Event("Event " + threadNum, j));
                    }
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // check that all events were inserted correctly
        for (int i = 0; i < NUM_THREADS; i++) {
            List<Event> eventsList = new ArrayList<>();
            EventIterator iterator = eventStore.query("Event " + i, 0, NUM_ITERATIONS);
            while (iterator.moveNext()){
                eventsList.add(iterator.current());
            }
            assertEquals(NUM_ITERATIONS, eventsList.size());
        }
    }



}