/*
 * Copyright (C) 2018 Khizar Heyat Khan (khizarkhan8@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.android.khizar.knotes.utils.date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Calendar;
import java.util.Locale;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DateUtils.class})
public class DateUtilsTest {

    @Test
    public void prettyTime() {
        long now = Calendar.getInstance().getTimeInMillis();

        String prettyTime = DateUtils.prettyTime(now, Locale.ENGLISH);
        Assert.assertEquals(prettyTime.toLowerCase(), "moments ago");

        prettyTime = DateUtils.prettyTime(now + 10 * 60 * 1000, Locale.ENGLISH);
        Assert.assertEquals(prettyTime.toLowerCase(), "10 minutes from now");

        prettyTime = DateUtils.prettyTime(now + 24 * 60 * 60 * 1000, Locale.ITALIAN);
        Assert.assertEquals(prettyTime.toLowerCase(), "fra 24 ore");

        prettyTime = DateUtils.prettyTime(now + 25 * 60 * 60 * 1000, Locale.ITALIAN);
        Assert.assertEquals(prettyTime.toLowerCase(), "fra 1 giorno");

        prettyTime = DateUtils.prettyTime(null, Locale.JAPANESE);
        Assert.assertNotNull(prettyTime.toLowerCase());
        Assert.assertEquals(prettyTime.toLowerCase().length(), 0);
    }

    @Test
    public void getPresetReminder() {
        long mockedNextMinute = 1497315847L;
        PowerMockito.stub(PowerMockito.method(DateUtils.class, "getNextMinute")).toReturn(mockedNextMinute);
        Assert.assertTrue(mockedNextMinute == DateUtils.getPresetReminder(null));
    }

}