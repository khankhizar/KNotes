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

package com.android.khizar.knotes.async.upgrade;

import android.content.ContentValues;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.android.khizar.knotes.KNotes;
import com.android.khizar.knotes.db.DbHelper;
import com.android.khizar.knotes.models.Attachment;
import com.android.khizar.knotes.models.Note;
import com.android.khizar.knotes.utils.Constants;
import com.android.khizar.knotes.utils.ReminderHelper;
import com.android.khizar.knotes.utils.StorageHelper;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * Processor used to perform asynchronous tasks on database upgrade.
 * It's not intended to be used to perform actions strictly related to DB (for this
 * {@link DbHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)}
 * DbHelper.onUpgrade()} is used
 */
public class UpgradeProcessor {

    private final static String METHODS_PREFIX = "onUpgradeTo";

    private static UpgradeProcessor instance;


    private UpgradeProcessor() {
    }


    private static UpgradeProcessor getInstance() {
        if (instance == null) {
            instance = new UpgradeProcessor();
        }
        return instance;
    }


    public static void process(int dbOldVersion, int dbNewVersion) {
        try {
            List<Method> methodsToLaunch = getInstance().getMethodsToLaunch(dbOldVersion, dbNewVersion);
            for (Method methodToLaunch : methodsToLaunch) {
                methodToLaunch.invoke(getInstance());
            }
        } catch (SecurityException | IllegalAccessException | InvocationTargetException e) {
            Log.d(Constants.TAG, "Explosion processing upgrade!", e);
        }
    }


    private List<Method> getMethodsToLaunch(int dbOldVersion, int dbNewVersion) {
        List<Method> methodsToLaunch = new ArrayList<>();
        Method[] declaredMethods = getInstance().getClass().getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            if (declaredMethod.getName().contains(METHODS_PREFIX)) {
                int methodVersionPostfix = Integer.parseInt(declaredMethod.getName().replace(METHODS_PREFIX, ""));
                if (dbOldVersion <= methodVersionPostfix && methodVersionPostfix <= dbNewVersion) {
                    methodsToLaunch.add(declaredMethod);
                }
            }
        }
        return methodsToLaunch;
    }


    /**
     * Adjustment of all the old attachments without mimetype field set into DB
     */
    private void onUpgradeTo476() {
		final DbHelper dbHelper = DbHelper.getInstance();
		for (Attachment attachment : dbHelper.getAllAttachments()) {
			if (attachment.getMime_type() == null) {
				String mimeType = StorageHelper.getMimeType(attachment.getUri().toString());
				if (!TextUtils.isEmpty(mimeType)) {
					String type = mimeType.replaceFirst("/.*", "");
					switch (type) {
						case "image":
							attachment.setMime_type(Constants.MIME_TYPE_IMAGE);
							break;
						case "video":
							attachment.setMime_type(Constants.MIME_TYPE_VIDEO);
							break;
						case "audio":
							attachment.setMime_type(Constants.MIME_TYPE_AUDIO);
							break;
						default:
							attachment.setMime_type(Constants.MIME_TYPE_FILES);
							break;
					}
					dbHelper.updateAttachment(attachment);
				} else {
					attachment.setMime_type(Constants.MIME_TYPE_FILES);
				}
			}
		}
    }

	}
