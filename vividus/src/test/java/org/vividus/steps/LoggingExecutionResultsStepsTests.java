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

package org.vividus.steps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.vividus.log.TestInfoLogger;

class LoggingExecutionResultsStepsTests
{
    @Test
    void shouldLogEnvironmentMetadataAfterStories()
    {
        TestInfoLogger testInfoLogger = mock(TestInfoLogger.class);
        new LoggingExecutionResultsSteps(testInfoLogger).afterStories();
        verify(testInfoLogger).logTestExecutionResults();
    }
}
