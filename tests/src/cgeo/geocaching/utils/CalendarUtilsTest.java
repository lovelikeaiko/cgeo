package cgeo.geocaching.utils;

import junit.framework.TestCase;

import java.util.Calendar;

import cgeo.geocaching.models.Geocache;
import cgeo.geocaching.enumerations.CacheType;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class CalendarUtilsTest extends TestCase {

    public static void testDaysSince() {
        final Calendar start = Calendar.getInstance();
        for (int hour = 0; hour < 24; hour++) {
            start.set(Calendar.HOUR_OF_DAY, hour);
            assertThat(CalendarUtils.daysSince(start.getTimeInMillis())).isEqualTo(0);
        }
    }

    public static void testIsPastEvent() {
        final Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 10);
        assertPastEvent(start, false);

        start.set(Calendar.HOUR_OF_DAY, 23);
        assertPastEvent(start, false);

        start.add(Calendar.DAY_OF_MONTH, -1);
        assertPastEvent(start, true);
    }

    private static void assertPastEvent(final Calendar start, final boolean expectedPast) {
        final Geocache cache = new Geocache();
        cache.setType(CacheType.EVENT);

        cache.setHidden(start.getTime());
        assertThat(CalendarUtils.isPastEvent(cache)).isEqualTo(expectedPast);
    }

    public static void testIsFuture() {
        final Calendar date = Calendar.getInstance();
        assertThat(CalendarUtils.isFuture(date)).isFalse();

        date.add(Calendar.DAY_OF_MONTH, 1);
        assertThat(CalendarUtils.isFuture(date)).isFalse();

        date.add(Calendar.DAY_OF_MONTH, 1);
        assertThat(CalendarUtils.isFuture(date)).isTrue();
    }
}
