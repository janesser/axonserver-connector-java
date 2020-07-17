/*
 * Copyright (c) 2020. AxonIQ
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.axoniq.axonserver.connector.impl;

import io.grpc.ConnectivityState;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractAxonServerChannel {

    private final ScheduledExecutorService executor;
    private final AxonServerManagedChannel channel;

    public AbstractAxonServerChannel(ScheduledExecutorService executor,
                                     AxonServerManagedChannel axonServerManagedChannel) {
        this.executor = executor;
        this.channel = axonServerManagedChannel;
    }

    protected void scheduleReconnect() {
        scheduleReconnect(false);
    }
    protected void scheduleImmediateReconnect() {
        scheduleReconnect(true);
    }

    private void scheduleReconnect(boolean immediate) {
        executor.schedule(() -> {
            ConnectivityState connectivityState = channel.getState(false);
            if (connectivityState == ConnectivityState.READY) {
                connect();
            } else {
                scheduleReconnect(false);
            }
        }, immediate ? 0 : 500, TimeUnit.MILLISECONDS);
    }

    public abstract void connect();

    public abstract void reconnect();

    public abstract void disconnect();

    public abstract boolean isConnected();
}
