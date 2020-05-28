package io.axoniq.axonserver.connector.event.impl;

import io.axoniq.axonserver.connector.event.AggregateEventStream;
import io.axoniq.axonserver.connector.impl.FlowControlledBuffer;
import io.axoniq.axonserver.grpc.FlowControl;
import io.axoniq.axonserver.grpc.event.Event;
import io.axoniq.axonserver.grpc.event.GetAggregateEventsRequest;

public class BufferedAggregateEventStream extends FlowControlledBuffer<Event, GetAggregateEventsRequest> implements AggregateEventStream {

    public static final Event TERMINAL_MESSAGE = Event.newBuilder().setAggregateSequenceNumber(-1729).build();

    private Event peeked;

    public BufferedAggregateEventStream() {
        this(Integer.MAX_VALUE);
    }

    public BufferedAggregateEventStream(int bufferSize) {
        super("unused", bufferSize, 0);
    }

    @Override
    public Event next() throws InterruptedException {
        Event taken;
        if (peeked != null) {
            taken = peeked;
            peeked = null;
        } else {
            taken = take();
        }
        return taken;
    }

    @Override
    public boolean hasNext() {
        if (peeked != null) {
            return true;
        }
        try {
            peeked = tryTake();
        } catch (InterruptedException e) {
            cancel();
            Thread.currentThread().interrupt();
            return false;
        }
        return peeked != null;
    }

    @Override
    public void cancel() {
        outboundStream().cancel("Request cancelled by client", null);
    }

    @Override
    protected GetAggregateEventsRequest buildFlowControlMessage(FlowControl flowControl) {
        // no flow control available on this request
        return null;
    }

    @Override
    protected Event terminalMessage() {
        return TERMINAL_MESSAGE;
    }
}