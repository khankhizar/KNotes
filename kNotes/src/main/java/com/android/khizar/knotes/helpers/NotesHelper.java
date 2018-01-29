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
package com.android.khizar.knotes.helpers;

import android.text.TextUtils;

import com.android.khizar.knotes.KNotes;
import com.android.khizar.knotes.models.Category;
import com.android.khizar.knotes.utils.StorageHelper;
import com.android.khizar.knotes.models.Attachment;
import com.android.khizar.knotes.models.Note;
import com.android.khizar.knotes.utils.Constants;

import org.apache.commons.lang.ObjectUtils;

import java.util.ArrayList;
import java.util.List;


public class NotesHelper {

    public static boolean haveSameId(Note note, Note currentNote) {
            return currentNote != null
            && currentNote.get_id() != null
            && currentNote.get_id().equals(note.get_id());

    }

    public static StringBuilder appendContent(Note note, StringBuilder content) {
        if (content.length() > 0
                && (!TextUtils.isEmpty(note.getTitle()) || !TextUtils.isEmpty(note.getContent()))) {
            content.append(System.getProperty("line.separator")).append(System.getProperty("line.separator"))
                    .append(Constants.MERGED_NOTES_SEPARATOR).append(System.getProperty("line.separator"))
                    .append(System.getProperty("line.separator"));
        }
        if (!TextUtils.isEmpty(note.getTitle())) {
            content.append(note.getTitle());
        }
        if (!TextUtils.isEmpty(note.getTitle()) && !TextUtils.isEmpty(note.getContent())) {
            content.append(System.getProperty("line.separator")).append(System.getProperty("line.separator"));
        }
        if (!TextUtils.isEmpty(note.getContent())) {
            content.append(note.getContent());
        }
        return content;
    }

    public static void addAttachments(boolean keepMergedNotes, Note note, ArrayList<Attachment> attachments) {
        if (keepMergedNotes) {
            for (Attachment attachment : note.getAttachmentsList()) {
                attachments.add(StorageHelper.createAttachmentFromUri(KNotes.getAppContext(), attachment.getUri
                        ()));
            }
        } else {
            attachments.addAll(note.getAttachmentsList());
        }
    }

	public static Note mergeNotes(List<Note> notes, boolean keepMergedNotes) {
		Note mergedNote = null;
		boolean locked = false;
		StringBuilder content = new StringBuilder();
		ArrayList<Attachment> attachments = new ArrayList<Attachment>();
		Category category = null;
		String reminder = null;
		String reminderRecurrenceRule = null;
		Double latitude = null, longitude = null;

		for (Note note : notes) {

			if (mergedNote == null) {
				mergedNote = new Note();
				mergedNote.setTitle(note.getTitle());
				content.append(note.getContent());
			} else {
                content = appendContent(note, content);
			}

			locked = locked || note.isLocked();

			category = (Category) ObjectUtils.defaultIfNull(category, note.getCategory());

			String currentReminder = note.getAlarm();
			if (!TextUtils.isEmpty(currentReminder) && reminder == null) {
				reminder = currentReminder;
				reminderRecurrenceRule = note.getRecurrenceRule();
			}

			latitude = (Double) ObjectUtils.defaultIfNull(latitude, note.getLatitude());
			longitude = (Double) ObjectUtils.defaultIfNull(longitude, note.getLongitude());

			addAttachments(keepMergedNotes, note, attachments);
		}

        mergedNote.setContent(content.toString());
        mergedNote.setLocked(locked);
        mergedNote.setCategory(category);
        mergedNote.setAlarm(reminder);
        mergedNote.setRecurrenceRule(reminderRecurrenceRule);
        mergedNote.setLatitude(latitude);
        mergedNote.setLongitude(longitude);
        mergedNote.setAttachmentsList(attachments);

        return mergedNote;
	}

}
