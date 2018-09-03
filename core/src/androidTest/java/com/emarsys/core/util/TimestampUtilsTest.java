package com.emarsys.core.util;

import android.support.test.InstrumentationRegistry;

import com.emarsys.core.DeviceInfo;
import com.emarsys.test.util.TimeoutUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class TimestampUtilsTest {

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
    }

    @Test
    public void testformatTimestampWithUTC() throws ParseException {
        String deviceTimeZone = new DeviceInfo(InstrumentationRegistry.getTargetContext()).getTimezone();
        String dateString = "2017-12-07T10:46:09.100";
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        parser.setTimeZone(TimeZone.getTimeZone(deviceTimeZone));

        Date date = parser.parse(dateString);
        long timestamp = date.getTime();

        assertEquals(
                "2017-12-07T10:46:09.100Z",
                TimestampUtils.formatTimestampWithUTC(timestamp)
        );
    }
}