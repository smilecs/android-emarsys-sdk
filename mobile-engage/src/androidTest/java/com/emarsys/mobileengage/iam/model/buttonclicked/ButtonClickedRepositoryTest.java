package com.emarsys.mobileengage.iam.model.buttonclicked;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.emarsys.core.database.helper.CoreDbHelper;
import com.emarsys.core.database.helper.DbHelper;
import com.emarsys.core.database.trigger.TriggerKey;
import com.emarsys.testUtil.DatabaseTestUtils;
import com.emarsys.testUtil.InstrumentationRegistry;
import com.emarsys.testUtil.TimeoutUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.emarsys.core.database.DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_BUTTON_ID;
import static com.emarsys.core.database.DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_CAMPAIGN_ID;
import static com.emarsys.core.database.DatabaseContract.BUTTON_CLICKED_COLUMN_NAME_TIMESTAMP;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ButtonClickedRepositoryTest {

    static {
        mock(Cursor.class);
    }

    private ButtonClickedRepository repository;
    private ButtonClicked buttonClicked1;

    @Rule
    public TestRule timeout = TimeoutUtils.getTimeoutRule();

    @Before
    public void init() {
        DatabaseTestUtils.deleteCoreDatabase();

        Context context = InstrumentationRegistry.getTargetContext();

        DbHelper dbHelper = new CoreDbHelper(context, new HashMap<TriggerKey, List<Runnable>>());

        repository = new ButtonClickedRepository(dbHelper);
        buttonClicked1 = new ButtonClicked("campaign1", "button1", new Date().getTime());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_dbHelper_mustNotBeNull() {
        new ButtonClickedRepository(null);
    }

    @Test
    public void testContentValuesFromItem() {
        ContentValues expected = new ContentValues();
        expected.put(BUTTON_CLICKED_COLUMN_NAME_CAMPAIGN_ID, buttonClicked1.getCampaignId());
        expected.put(BUTTON_CLICKED_COLUMN_NAME_BUTTON_ID, buttonClicked1.getButtonId());
        expected.put(BUTTON_CLICKED_COLUMN_NAME_TIMESTAMP, buttonClicked1.getTimestamp());

        ContentValues result = repository.contentValuesFromItem(buttonClicked1);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testItemFromCursor() {
        Cursor cursor = mock(Cursor.class);

        when(cursor.getColumnIndex(BUTTON_CLICKED_COLUMN_NAME_CAMPAIGN_ID)).thenReturn(0);
        when(cursor.getString(0)).thenReturn(buttonClicked1.getCampaignId());
        when(cursor.getColumnIndex(BUTTON_CLICKED_COLUMN_NAME_BUTTON_ID)).thenReturn(1);
        when(cursor.getString(1)).thenReturn(buttonClicked1.getButtonId());
        when(cursor.getColumnIndex(BUTTON_CLICKED_COLUMN_NAME_TIMESTAMP)).thenReturn(2);
        when(cursor.getLong(2)).thenReturn(buttonClicked1.getTimestamp());

        ButtonClicked result = repository.itemFromCursor(cursor);
        ButtonClicked expected = buttonClicked1;

        Assert.assertEquals(expected, result);
    }

}