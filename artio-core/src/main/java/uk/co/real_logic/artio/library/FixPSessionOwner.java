/*
 * Copyright 2015-2020 Real Logic Limited, Adaptive Financial Consulting Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.artio.library;

import uk.co.real_logic.artio.Reply;
import uk.co.real_logic.artio.messages.ThrottleConfigurationStatus;

import java.util.function.BooleanSupplier;

public interface FixPSessionOwner
{
    void enqueueTask(BooleanSupplier task);

    void remove(InternalFixPConnection connection);

    Reply<ThrottleConfigurationStatus> messageThrottle(
        long sessionId, int throttleWindowInMs, int throttleLimitOfMessages);
}
