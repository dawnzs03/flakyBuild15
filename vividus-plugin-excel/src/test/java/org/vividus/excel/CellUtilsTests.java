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

package org.vividus.excel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CellUtilsTests
{
    private static final String TEST = "test";

    @Mock
    private Cell cell;

    @Test
    void testNumericCell()
    {
        Mockito.when(cell.getCellType()).thenReturn(CellType.NUMERIC);
        Mockito.when(cell.getNumericCellValue()).thenReturn((double) 3);
        assertEquals("3.0", CellUtils.getCellValueAsString(cell));
    }

    @Test
    void testBooleanCell()
    {
        Mockito.when(cell.getCellType()).thenReturn(CellType.BOOLEAN);
        Mockito.when(cell.getBooleanCellValue()).thenReturn(false);
        assertEquals("false", CellUtils.getCellValueAsString(cell));
    }

    @Test
    void testFormulaCell()
    {
        Mockito.when(cell.getCellType()).thenReturn(CellType.FORMULA);
        Mockito.when(cell.getCachedFormulaResultType()).thenReturn(CellType.STRING);
        Mockito.when(cell.getStringCellValue()).thenReturn(TEST);

        assertEquals(TEST, CellUtils.getCellValueAsString(cell));
    }

    @Test
    void testStringCell()
    {
        Mockito.when(cell.getCellType()).thenReturn(CellType.STRING);
        Mockito.when(cell.getStringCellValue()).thenReturn(TEST);
        assertEquals(TEST, CellUtils.getCellValueAsString(cell));
    }

    @Test
    void testBlankCell()
    {
        Mockito.when(cell.getCellType()).thenReturn(CellType.BLANK);
        assertEquals(StringUtils.EMPTY, CellUtils.getCellValueAsString(cell));
    }

    @Test
    void testErrorCell()
    {
        Mockito.when(cell.getCellType()).thenReturn(CellType.ERROR);
        assertEquals(StringUtils.EMPTY, CellUtils.getCellValueAsString(cell));
    }
}
