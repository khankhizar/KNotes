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

import android.content.Context;
import android.content.Intent;

import com.android.khizar.knotes.KNotes;
import com.android.khizar.knotes.MainActivity;
import com.android.khizar.knotes.helpers.date.DateHelper;
import com.android.khizar.knotes.models.Note;


public class ShortcutHelper {


    /**
     * Adding shortcut on Home screen
     */
    public static void addShortcut(Context context, Note note) {
        Intent shortcutIntent = new Intent(context, MainActivity.class);
        shortcutIntent.putExtra(Constants.INTENT_KEY, note.get_id());
        shortcutIntent.setAction(Constants.ACTION_SHORTCUT);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        String shortcutTitle = note.getTitle().length() > 0 ? note.getTitle() : DateHelper.getFormattedDate(note
				.getCreation(), KNotes.getSharedPreferences().getBoolean(Constants
				.PREF_PRETTIFIED_DATES, true));
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutTitle);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(context, com.android.khizar.knotes.R.drawable.ic_shortcut));
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

        context.sendBroadcast(addIntent);
    }

    /**
     * Removes note shortcut from home launcher
     */
    public static void removeshortCut(Context context, Note note) {
        Intent shortcutIntent = new Intent(context, MainActivity.class);
        shortcutIntent.putExtra(Constants.INTENT_KEY, note.get_id());
        shortcutIntent.setAction(Constants.ACTION_SHORTCUT);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		String shortcutTitle = note.getTitle().length() > 0 ? note.getTitle() : DateHelper.getFormattedDate(note
				.getCreation(), KNotes.getSharedPreferences().getBoolean(Constants
				.PREF_PRETTIFIED_DATES, true));

        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutTitle);

        addIntent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
        context.sendBroadcast(addIntent);
    }
}
