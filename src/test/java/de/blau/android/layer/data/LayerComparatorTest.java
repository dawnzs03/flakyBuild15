package de.blau.android.layer.data;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import androidx.test.filters.LargeTest;
import de.blau.android.UnitTestUtils;
import de.blau.android.layer.data.MapOverlay.LayerComparator;
import de.blau.android.osm.StorageDelegator;
import de.blau.android.osm.Tags;
import de.blau.android.osm.Way;

@RunWith(RobolectricTestRunner.class)
@LargeTest
public class LayerComparatorTest {

    private StorageDelegator delegator;
    private LayerComparator  comparator;

    /**
     * Pre test setup
     */
    @Before
    public void setup() {
        delegator = UnitTestUtils.loadTestData(getClass(), "ways.osm");
        comparator = new LayerComparator();
    }

    /**
     * Smaller ring should be on top
     */
    @Test
    public void closedWays() {
        Way closedLarge = (Way) delegator.getOsmElement(Way.NAME, -1);
        Way closedSmall = (Way) delegator.getOsmElement(Way.NAME, -2);
        assertTrue(comparator.compare(closedLarge, closedSmall) < 0);
        assertTrue(comparator.compare(closedSmall, closedLarge) > 0);
    }

    /**
     * Override with layer tag
     */
    @Test
    public void closedWaysLayer() {
        Way closedLarge = (Way) delegator.getOsmElement(Way.NAME, -1);
        Way closedSmall = (Way) delegator.getOsmElement(Way.NAME, -2);

        Map<String, String> tags = new HashMap<>();
        tags.put(Tags.KEY_LAYER, "1");

        delegator.setTags(closedLarge, tags);
        assertTrue(comparator.compare(closedLarge, closedSmall) > 0);
        assertTrue(comparator.compare(closedSmall, closedLarge) < 0);
    }

    /**
     * ways with no tags
     */
    @Test
    public void ways() {
        Way w3 = (Way) delegator.getOsmElement(Way.NAME, -3);
        Way w4 = (Way) delegator.getOsmElement(Way.NAME, -4);

        assertTrue(comparator.compare(w3, w4) == 0);
        assertTrue(comparator.compare(w4, w3) == 0);
    }
    
    /**
     * Override with layer tag
     */
    @Test
    public void waysLayer() {
        Way w3 = (Way) delegator.getOsmElement(Way.NAME, -3);
        Way w4 = (Way) delegator.getOsmElement(Way.NAME, -4);

        Map<String, String> tags = new HashMap<>();
        tags.put(Tags.KEY_LAYER, "1");
        delegator.setTags(w3, tags);

        assertTrue(comparator.compare(w3, w4) > 0);
        assertTrue(comparator.compare(w4, w3) < 0);
    }
    
    /**
     * Unparseable layer tags should do nothing
     */
    @Test
    public void waysInvalidLayer() {
        Way w3 = (Way) delegator.getOsmElement(Way.NAME, -3);
        Way w4 = (Way) delegator.getOsmElement(Way.NAME, -4);

        Map<String, String> tags = new HashMap<>();
        tags.put(Tags.KEY_LAYER, "A");
        delegator.setTags(w3, tags);
        tags = new HashMap<>();
        tags.put(Tags.KEY_LAYER, "B");
        delegator.setTags(w4, tags);

        assertTrue(comparator.compare(w3, w4) == 0);
        assertTrue(comparator.compare(w4, w3) == 0);
    }
}