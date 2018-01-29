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


package com.android.khizar.knotes.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.khizar.knotes.KNotes;
import com.android.khizar.knotes.R;
import com.android.khizar.knotes.helpers.date.DateHelper;
import com.android.khizar.knotes.models.Note;
import com.android.khizar.knotes.receiver.AlarmReceiver;
import com.android.khizar.knotes.utils.date.DateUtils;

import java.util.Calendar;


public class ReminderHelper {

	public static void addReminder(Context context, Note note) {
		if (note.getAlarm() != null) {
			addReminder(context, note, Long.parseLong(note.getAlarm()));
		}
	}


	public static void addReminder(Context context, Note note, long reminder) {
		if (DateUtils.isFuture(reminder)) {
			Intent intent = new Intent(context, AlarmReceiver.class);
			intent.putExtra(Constants.INTENT_NOTE, ParcelableUtil.marshall(note));
			PendingIntent sender = PendingIntent.getBroadcast(context, getRequestCode(note), intent,
					PendingIntent.FLAG_CANCEL_CURRENT);
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				am.setExact(AlarmManager.RTC_WAKEUP, reminder, sender);
			} else {
				am.set(AlarmManager.RTC_WAKEUP, reminder, sender);
			}
		}
	}


	/**
	 * Checks if exists any reminder for given note
	 */
	public static boolean checkReminder(Context context, Note note) {
		return PendingIntent.getBroadcast(context, getRequestCode(note), new Intent(context, AlarmReceiver
				.class), PendingIntent.FLAG_NO_CREATE) != null;
	}


	static int getRequestCode(Note note) {
		Long longCode = note.getCreation() != null ? note.getCreation() : Calendar.getInstance().getTimeInMillis();
		return Long.valueOf(longCode / 1000).intValue();
	}


	public static void removeReminder(Context context, Note note) {
		if (!TextUtils.isEmpty(note.getAlarm())) {
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(context, AlarmReceiver.class);
			PendingIntent p = PendingIntent.getBroadcast(context, getRequestCode(note), intent, 0);
			am.cancel(p);
			p.cancel();
		}
	}


	public static void showReminderMessage(String reminderString) {
		if (reminderString != null) {
			long reminder = Long.parseLong(reminderString);
			if (reminder > Calendar.getInstance().getTimeInMillis()) {
				new Handler(KNotes.getAppContext().getMainLooper()).post(() -> Toast.makeText(KNotes
								.getAppContext(),
						KNotes.getAppContext().getString(R.string.alarm_set_on) + " " + DateHelper.getDateTimeShort
								(KNotes.getAppContext(), reminder), Toast.LENGTH_LONG).show());
			}
		}
	}
}
