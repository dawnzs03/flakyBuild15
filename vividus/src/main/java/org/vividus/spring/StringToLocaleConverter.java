/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.spring;

import java.util.Locale;

import org.apache.commons.lang3.LocaleUtils;
import org.springframework.core.convert.converter.Converter;

public class StringToLocaleConverter implements Converter<String, Locale>
{
    @Override
    public Locale convert(String source)
    {
        return LocaleUtils.toLocale(source);
    }
}
