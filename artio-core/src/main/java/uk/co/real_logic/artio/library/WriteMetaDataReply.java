/*
 * Copyright 2020 Monotonic Limited.
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

import org.agrona.DirectBuffer;
import uk.co.real_logic.artio.messages.MetaDataStatus;

class WriteMetaDataReply extends LibraryReply<MetaDataStatus>
{
    private final long sessionId;
    private final int metaDataOffset;
    private final DirectBuffer buffer;
    private final int offset;
    private final int length;

    WriteMetaDataReply(
        final LibraryPoller libraryPoller,
        final long latestReplyArrivalTime,
        final long sessionId,
        final int metaDataOffset,
        final DirectBuffer buffer,
        final int offset,
        final int length)
    {
        super(libraryPoller, latestReplyArrivalTime);
        this.sessionId = sessionId;
        this.metaDataOffset = metaDataOffset;
        this.buffer = buffer;
        this.offset = offset;
        this.length = length;
        if (libraryPoller.isConnected())
        {
            sendMessage();
        }
    }

    protected void sendMessage()
    {
        final long position = libraryPoller.saveWriteMetaData(
            sessionId, metaDataOffset, buffer, offset, length, correlationId);

        requiresResend = position < 0;
    }
}
