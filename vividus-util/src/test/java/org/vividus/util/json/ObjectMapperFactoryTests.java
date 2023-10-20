/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.util.json;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

class ObjectMapperFactoryTests
{
    @Test
    void testCreateWithCaseInsensitiveEnumDeserializer() throws IOException
    {
        ObjectMapper objectMapper = ObjectMapperFactory.createWithCaseInsensitiveEnumDeserializer();
        TestClass testObject = objectMapper.readValue("{\"field\":\"Name\"}", TestClass.class);
        assertEquals(TestEnum.NAME, testObject.field);
    }

    private static final class TestClass
    {
        @JsonProperty
        private TestEnum field;
    }

    private enum TestEnum
    {
        NAME
    }
}
