/*
 * Copyright 2019-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vividus.ui.context;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.vividus.testcontext.SimpleTestContext;
import org.vividus.testcontext.TestContext;

public class UiContextTestsBase
{
    private final TestContext context = new SimpleTestContext()
    {
        private final Map<Object, Object> map = new HashMap<>();

        @Override
        public void clear()
        {
            map.clear();
        }

        @Override
        public int size()
        {
            return map.size();
        }

        @Override
        public Object remove(Object key)
        {
            return map.remove(key);
        }

        @Override
        public void putAll(Map<Object, Object> map)
        {
            // Implementation is not needed
        }

        @Override
        public void put(Object key, Object value)
        {
            map.put(key, value);
        }

        @Override
        public void copyAllTo(Map<Object, Object> map)
        {
            // Implementation is not needed
        }

        @Override
        public <T> T get(Object key, Class<T> type)
        {
            return type.cast(map.get(key));
        }

        @Override
        public <T> T get(Object key, Supplier<T> initialValueSupplier)
        {
            // Implementation is not needed
            return null;
        }

        @Override
        public <T> T get(Object key)
        {
            // Implementation is not needed
            return null;
        }

        @Override
        public void putInitValueSupplier(Object key, Supplier<Object> initialValueSupplier)
        {
            // Implementation is not needed
        }
    };

    public TestContext getContext()
    {
        return context;
    }
}
