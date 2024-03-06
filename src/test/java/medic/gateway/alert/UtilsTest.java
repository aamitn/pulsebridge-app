package medic.gateway.alert;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=26)
@SuppressWarnings({"PMD.ModifiedCyclomaticComplexity",
		"PMD.NPathComplexity",
		"PMD.StdCyclomaticComplexity"})
public class UtilsTest {
	private static final long HALF_MINUE = 30 * 1000;
	private static final long MINUTE = 2 * HALF_MINUE;
	private static final long HALF_HOUR = 30 * MINUTE;
	private static final long HOUR = 2 * HALF_HOUR;
	private static final long HALF_DAY = 12 * HOUR;
	private static final long DAY = 2 * HALF_DAY;

	@Before
	public void setUp() {
		Application ctx = getApplicationContext();
		shadowOf(ctx);

		Robolectric.buildActivity(MessageListsActivity.class).create().get();
	}

	/**
	 * This test has a race condition.  If there is significant blocking
	 * while this test is running (worst case at least 30 seconds), this
	 * test could fail.  This seems unlikely to ever cause test failures.
	 */
	@Test
	public void relativeTimestamp() {
		Object[] testCases = {
			/* delta (ms), expectedText */
			HALF_MINUE, "just now",
			10 * MINUTE + HALF_MINUE, "10m ago",
			3 * HOUR + HALF_HOUR, "3h ago",
			DAY + HALF_DAY, "yesterday",
			4 * DAY + HALF_DAY, "4 days ago",
			8 * DAY, "a week ago",
			15 * DAY, "2 weeks ago",
			22 * DAY, "3 weeks ago",
			32 * DAY, "a month ago",
			64 * DAY, "2 months ago",
			500 * DAY, "a year ago",
			850 * DAY, "2 years ago",
		};
		for(int i=0; i<testCases.length; i+=2) {
			// given
			long delta = (long) testCases[i];
			String expectedText = (String) testCases[i+1];
			long testTimestamp = System.currentTimeMillis() - delta;

			// when
			String actual = Utils.relativeTimestamp(testTimestamp);

			// then
			assertEquals(String.format("delta: %d", delta),
					expectedText, actual);
		}
	}

	@Test
	public void args_shouldNotModifyStringArrays() {
		// given
		String[] args = { "a", "b", "c" };

		// when
		String[] returned = Utils.args(args);

		// then
		assertSame(args, returned);
	}

	@Test
	public void args_shouldTurnObjectArraysToStrings() {
		// given
		Object[] args = { "a", 1, true };

		// when
		String[] returned = Utils.args(args);

		// then
		assertArrayEquals(returned, new String[] {
			"a", "1", "true"
		});
	}


	@Test
	public void normalisePhoneNumber_shouldStripCertainCharacters() {
		// given
		String[] testMappings = {
			"1234567890", "1234567890",
			"123 456 7890", "1234567890",
			"123-456 7890", "1234567890",
			"123-456-7890", "1234567890",

			"+1234567890", "+1234567890",
			"+123 456 7890", "+1234567890",
			"+123-456 7890", "+1234567890",
			"+123-456-7890", "+1234567890",
		};

		for(int i=0; i<testMappings.length; i+=2) {
			// when
			String original = testMappings[i];
			String expected = testMappings[i+1];

			// expect
			assertEquals(expected, Utils.normalisePhoneNumber(original));
		}
	}


}
