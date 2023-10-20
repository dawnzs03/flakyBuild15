package uk.co.real_logic.artio.util;

import org.junit.Test;

import java.util.Arrays;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.Assert.*;
import static uk.co.real_logic.artio.util.MessageTypeEncoding.packMessageType;

public class MessageTypeEncodingTest
{
    @Test
    public void shouldGenerateDifferentMessageTypeIdentifiers()
    {
        assertPackedTypesNotEqual("AL", "AM");
        assertPackedTypesNotEqual("AL", "AN");
        assertPackedTypesNotEqual("AR", "AQ");
        assertPackedTypesNotEqual("BC", "BB");
        assertPackedTypesNotEqual("BD", "BE");
        assertPackedTypesNotEqual("BG", "BF");
    }

    @Test
    public void supportsPackingMessageTypesOfLength7()
    {
        assertPackedTypesNotEqual("ABCDEFGH", "ABCDEFG");
    }

    private void assertPackedTypesNotEqual(final String first, final String second)
    {
        final long firstPacked = packMessageType(first);
        final long secondPacked = packMessageType(second);

        assertNotEquals(firstPacked, secondPacked);

        assertPackingsConsistent(firstPacked, first);
        assertPackingsConsistent(secondPacked, second);
    }

    private void assertPackingsConsistent(final long packed, final String asString)
    {
        final char[] chars = asString.toCharArray();
        final byte[] bytes = asString.getBytes(US_ASCII);
        final int length = chars.length;

        assertEquals(packed, packMessageType(chars, length));

        final char[] charsExtra = Arrays.copyOf(chars, length + 1);
        charsExtra[length] = 'Z';
        final long packedCharsExtra = packMessageType(charsExtra, length);
        assertEquals(packed, packedCharsExtra);

        assertEquals(packed, packMessageType(bytes, 0, length));

        final byte[] bytesExtra = new byte[length + 2];
        bytesExtra[0] = (byte)'z';
        System.arraycopy(bytes, 0, bytesExtra, 1, length);
        bytesExtra[length + 1] = (byte)'z';
        assertEquals(packed, packMessageType(bytesExtra, 1, length));
    }

    @Test(expected = IllegalArgumentException.class)
    public void willFailToGeneratePackedMessageTypeWithMoreThan8Characters()
    {
        packMessageType("ABCDEFGHI");
    }

    @Test
    public void shouldUnpackMessageTypes()
    {
        assertUnpackedPackedExample("D");
        assertUnpackedPackedExample("AL");
        assertUnpackedPackedExample("BF");
    }

    private void assertUnpackedPackedExample(final String messageType)
    {
        final long packed = packMessageType(messageType);

        final byte[] buffer = new byte[2];
        final int length = MessageTypeEncoding.unpackMessageType(packed, buffer);
        assertEquals(messageType, new String(buffer, 0, length, US_ASCII));
    }
}
