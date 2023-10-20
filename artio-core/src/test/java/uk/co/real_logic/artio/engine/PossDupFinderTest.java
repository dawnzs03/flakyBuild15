package uk.co.real_logic.artio.engine;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Test;
import uk.co.real_logic.artio.dictionary.LongDictionary;
import uk.co.real_logic.artio.otf.OtfParser;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.Assert.assertEquals;
import static uk.co.real_logic.artio.engine.logger.ReplayerTest.MESSAGE_REQUIRING_LONGER_BODY_LENGTH;

public class PossDupFinderTest
{
    private static final byte[] FIRST_MESSAGE =
        ("8=FIX.4.4\0019=0065\00135=5\00149=initiator\00156=acceptor\00134=2\001" +
            "52=20161206-11:04:51.461\00110=088\001").getBytes(US_ASCII);

    private static final byte[] SECOND_MESSAGE =
        ("8=FIX.4.4\0019=0065\00135=5\00149=initiator\00156=acceptor\00134=2\001" +
            "52=20161206-11:04:51.461\00143=Y\00110=088\001").getBytes(US_ASCII);

    private final PossDupFinder possDupFinder = new PossDupFinder();
    private final OtfParser parser = new OtfParser(possDupFinder, new LongDictionary());
    private final UnsafeBuffer buffer = new UnsafeBuffer(new byte[FIRST_MESSAGE.length + SECOND_MESSAGE.length]);

    @Test
    public void shouldOnlyReturnPossDupAtLength()
    {
        buffer.putBytes(0, FIRST_MESSAGE);
        buffer.putBytes(FIRST_MESSAGE.length, SECOND_MESSAGE);

        parser.onMessage(buffer, 0, FIRST_MESSAGE.length);

        assertEquals(PossDupFinder.NO_ENTRY, possDupFinder.possDupOffset());
    }

    @Test
    public void shouldFindLengthOfPossDupMessage()
    {
        buffer.putBytes(0, MESSAGE_REQUIRING_LONGER_BODY_LENGTH);

        parser.onMessage(buffer, 0, MESSAGE_REQUIRING_LONGER_BODY_LENGTH.length);

        assertEquals(12, possDupFinder.bodyLengthOffset());
        assertEquals(2, possDupFinder.lengthOfBodyLength());
    }
}
