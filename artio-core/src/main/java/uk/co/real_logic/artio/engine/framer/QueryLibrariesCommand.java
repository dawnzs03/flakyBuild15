/*
 * Copyright 2014 Real Logic Limited.
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
package uk.co.real_logic.artio.engine.framer;

import uk.co.real_logic.artio.Reply;

import java.util.List;

final class QueryLibrariesCommand implements Reply<List<LibraryInfo>>, AdminCommand
{
    // State written to from Framer thread, read by any other thread.
    private volatile State state = State.EXECUTING;
    private List<LibraryInfo> result;

    public void execute(final Framer framer)
    {
        framer.onQueryLibraries(this);
    }

    void success(final List<LibraryInfo> result)
    {
        this.result = result;
        state = State.COMPLETED;
    }

    public Exception error()
    {
        return null;
    }

    public List<LibraryInfo> resultIfPresent()
    {
        return result;
    }

    public State state()
    {
        return state;
    }

}
