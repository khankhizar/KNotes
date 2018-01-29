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

package com.android.khizar.knotes.async;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.khizar.knotes.KNotes;
import com.android.khizar.knotes.MainActivity;
import com.android.khizar.knotes.db.DbHelper;
import com.android.khizar.knotes.models.Attachment;
import com.android.khizar.knotes.models.Note;
import com.android.khizar.knotes.models.listeners.OnAttachingFileListener;
import com.android.khizar.knotes.utils.Constants;
import com.android.khizar.knotes.utils.NotificationsHelper;
import com.android.khizar.knotes.utils.ReminderHelper;
import com.android.khizar.knotes.utils.StorageHelper;
import com.android.khizar.knotes.utils.TextHelper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class DataBackupIntentService extends IntentService implements OnAttachingFileListener {

    public final static String INTENT_BACKUP_NAME = "backup_name";
    public final static String INTENT_BACKUP_INCLUDE_SETTINGS = "backup_include_settings";
    public final static String ACTION_DATA_EXPORT = "action_data_export";
    public final static String ACTION_DATA_IMPORT = "action_data_import";
    public final static String ACTION_DATA_DELETE = "action_data_delete";


    private SharedPreferences prefs;
    private NotificationsHelper mNotificationsHelper;

    private int importedSpringpadNotes, importedSpringpadNotebooks;


    public DataBackupIntentService() {
        super("DataBackupIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);

        // Creates an indeterminate processing notification until the work is complete
        mNotificationsHelper = new NotificationsHelper(this)
                .createNotification(com.android.khizar.knotes.R.drawable.ic_content_save_white_24dp, getString(com.android.khizar.knotes.R.string.working), null)
                .setIndeterminate().setOngoing().show();

        // If an alarm has been fired a notification must be generated
        if (ACTION_DATA_EXPORT.equals(intent.getAction())) {
            exportData(intent);
        } else if (ACTION_DATA_IMPORT.equals(intent.getAction())) {
            importData(intent);
        } else if (ACTION_DATA_DELETE.equals(intent.getAction())) {
            deleteData(intent);
        }
    }


    synchronized private void exportData(Intent intent) {

        // Gets backup folder
        String backupName = intent.getStringExtra(INTENT_BACKUP_NAME);
        File backupDir = StorageHelper.getBackupDir(backupName);

        // Directory clean in case of previously used backup name
        StorageHelper.delete(this, backupDir.getAbsolutePath());

        // Directory is re-created in case of previously used backup name (removed above)
        backupDir = StorageHelper.getBackupDir(backupName);

        // Database backup
        exportDB(backupDir);
//		exportNotes(backupDir);

        // Attachments backup
        exportAttachments(backupDir);

        // Settings
        if (intent.getBooleanExtra(INTENT_BACKUP_INCLUDE_SETTINGS, true)) {
            exportSettings(backupDir);
        }

        // Notification of operation ended
        String title = getString(com.android.khizar.knotes.R.string.data_export_completed);
        String text = backupDir.getPath();
        createNotification(intent, this, title, text, backupDir);
    }


    synchronized private void importData(Intent intent) {

        // Gets backup folder
        String backupName = intent.getStringExtra(INTENT_BACKUP_NAME);
        File backupDir = StorageHelper.getBackupDir(backupName);

        // Database backup
        importDB(backupDir);
//        importNotes(backupDir);

        // Attachments backup
        importAttachments(backupDir);

		// Settings restore
		importSettings(backupDir);

		// Reminders restore
		resetReminders();

        String title = getString(com.android.khizar.knotes.R.string.data_import_completed);
        String text = getString(com.android.khizar.knotes.R.string.click_to_refresh_application);
        createNotification(intent, this, title, text, backupDir);
    }





     synchronized private void deleteData(Intent intent) {

        // Gets backup folder
        String backupName = intent.getStringExtra(INTENT_BACKUP_NAME);
        File backupDir = StorageHelper.getBackupDir(backupName);

        // Backup directory removal
        StorageHelper.delete(this, backupDir.getAbsolutePath());

        String title = getString(com.android.khizar.knotes.R.string.data_deletion_completed);
        String text = backupName + " " + getString(com.android.khizar.knotes.R.string.deleted);
        createNotification(intent, this, title, text, backupDir);
    }


    /**
     * Creation of notification on operations completed
     */
    private void createNotification(Intent intent, Context mContext, String title, String message, File backupDir) {

        // The behavior differs depending on intent action
        Intent intentLaunch;
        if (DataBackupIntentService.ACTION_DATA_IMPORT.equals(intent.getAction()))
                {
			intentLaunch = new Intent(mContext, MainActivity.class);
			intentLaunch.setAction(Constants.ACTION_RESTART_APP);
        } else {
            intentLaunch = new Intent();
        }
        // Add this bundle to the intent
        intentLaunch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Creates the PendingIntent
        PendingIntent notifyIntent = PendingIntent.getActivity(mContext, 0, intentLaunch,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationsHelper mNotificationsHelper = new NotificationsHelper(mContext);
        mNotificationsHelper.createNotification(com.android.khizar.knotes.R.drawable.ic_content_save_white_24dp, title, notifyIntent)
                .setMessage(message).setRingtone(prefs.getString("settings_notification_ringtone", null))
                .setLedActive();
        if (prefs.getBoolean("settings_notification_vibration", true)) mNotificationsHelper.setVibration();
        mNotificationsHelper.show();
    }


    /**
     * Export database to backup folder
     *
     * @return True if success, false otherwise
     */
    private boolean exportDB(File backupDir) {
        File database = getDatabasePath(Constants.DATABASE_NAME);
        return (StorageHelper.copyFile(database, new File(backupDir, Constants.DATABASE_NAME)));
    }

    private void exportNotes(File backupDir) {
		for (Note note : DbHelper.getInstance().getAllNotes(false)) {
			File noteFile = new File(backupDir, String.valueOf(note.get_id()));
			try {
				FileUtils.write(noteFile, note.toJSON());
			} catch (IOException e) {
				Log.e(Constants.TAG, "Error backupping note: " + note.get_id());
			}
		}
	}


    /**
     * Export attachments to backup folder
     *
     * @return True if success, false otherwise
     */
    private boolean exportAttachments(File backupDir) {
        File attachmentsDir = StorageHelper.getAttachmentDir(this);
        File destinationattachmentsDir = new File(backupDir, attachmentsDir.getName());

        DbHelper db = DbHelper.getInstance();
        ArrayList<Attachment> list = db.getAllAttachments();

        int exported = 0;
        for (Attachment attachment : list) {
            StorageHelper.copyToBackupDir(destinationattachmentsDir, new File(attachment.getUri().getPath()));
            mNotificationsHelper.setMessage(TextHelper.capitalize(getString(com.android.khizar.knotes.R.string.attachment)) + " " + exported++ + "/" + list.size())
                    .show();
        }
        return true;
    }


    /**
     * Exports settings if required
     */
    private boolean exportSettings(File backupDir) {
        File preferences = StorageHelper.getSharedPreferencesFile(this);
        return (StorageHelper.copyFile(preferences, new File(backupDir, preferences.getName())));
    }


    /**
     * Imports settings
     */
    private boolean importSettings(File backupDir) {
        File preferences = StorageHelper.getSharedPreferencesFile(this);
        File preferenceBackup = new File(backupDir, preferences.getName());
        return (StorageHelper.copyFile(preferenceBackup, preferences));
    }


	/**
	 * Schedules reminders
	 */
	private void resetReminders() {
		Log.d(Constants.TAG, "Resettings reminders");
		for (Note note : DbHelper.getInstance().getNotesWithReminderNotFired()) {
			ReminderHelper.addReminder(KNotes.getAppContext(), note);
		}
	}


    /**
     * Import database from backup folder
     */
    private boolean importDB(File backupDir) {
        File database = getDatabasePath(Constants.DATABASE_NAME);
        if (database.exists()) {
            database.delete();
        }
        return (StorageHelper.copyFile(new File(backupDir, Constants.DATABASE_NAME), database));
    }


    private void importNotes(File backupDir) {
		for (File file : FileUtils.listFiles(backupDir, new RegexFileFilter("\\d{13}"), TrueFileFilter.INSTANCE)) {
			try {
				Note note = new Note();
				note.buildFromJson(FileUtils.readFileToString(file));
				if (note.getCategory() != null) {
					DbHelper.getInstance().updateCategory(note.getCategory());
				}
				for (Attachment attachment : note.getAttachmentsList()) {
					DbHelper.getInstance().updateAttachment(attachment);
				}
				DbHelper.getInstance().updateNote(note, false);
			} catch (IOException e) {
				Log.e(Constants.TAG, "Error parsing note json");
			}
		}
    }


    /**
     * Import attachments from backup folder
     */
    private void importAttachments(File backupDir) {
        File attachmentsDir = StorageHelper.getAttachmentDir(this);
        File backupAttachmentsDir = new File(backupDir, attachmentsDir.getName());
        if (!backupAttachmentsDir.exists()) {
			return;
		}
        Collection list = FileUtils.listFiles(backupAttachmentsDir, FileFilterUtils.trueFileFilter(),
                TrueFileFilter.INSTANCE);
        Iterator i = list.iterator();
        int imported = 0;
        File file = null;
        while (i.hasNext()) {
            try {
                file = (File) i.next();
                FileUtils.copyFileToDirectory(file, attachmentsDir, true);
                mNotificationsHelper.setMessage(TextHelper.capitalize(getString(com.android.khizar.knotes.R.string.attachment)) + " " + imported++ + "/" + list.size())
                        .show();
            } catch (IOException e) {
                Log.e(Constants.TAG, "Error importing the attachment " + file.getName());
            }
        }
    }


    @Override
    public void onAttachingFileErrorOccurred(Attachment mAttachment) {
        // TODO Auto-generated method stub
    }


    @Override
    public void onAttachingFileFinished(Attachment mAttachment) {
        // TODO Auto-generated method stub
    }

}
