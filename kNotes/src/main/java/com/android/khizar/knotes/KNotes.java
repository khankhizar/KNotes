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


package com.android.khizar.knotes;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;

import com.android.khizar.knotes.helpers.LanguageHelper;
import com.crashlytics.android.Crashlytics;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import io.fabric.sdk.android.Fabric;


import com.android.khizar.knotes.utils.Constants;



public class KNotes extends MultiDexApplication {

	private static Context mContext;

	static SharedPreferences prefs;
	private static RefWatcher refWatcher;


	@Override
	public void onCreate() {
		super.onCreate();
		Fabric.with(this, new Crashlytics());
		mContext = getApplicationContext();
		prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);

		// TODO: Move this to where you establish a user session
		logUser();

		if (isDebugBuild()) {
			StrictMode.enableDefaults();
		}

		//initAcra(this);

		initLeakCanary();

		// Checks selected locale or default one
		LanguageHelper.updateLanguage(this, null);
	}

	private void initLeakCanary() {
		if (!LeakCanary.isInAnalyzerProcess(this)) {
			refWatcher = LeakCanary.install(this);
		}
	}


	@NonNull
	public static boolean isDebugBuild() {
		return BuildConfig.BUILD_TYPE.equals("debug");
	}

	@Override
	// Used to restore user selected locale when configuration changes
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		String language = prefs.getString(Constants.PREF_LANG, "");
		super.onConfigurationChanged(newConfig);
		LanguageHelper.updateLanguage(this, language);
	}

	public static Context getAppContext() {
		return KNotes.mContext;
	}

	public static RefWatcher getRefWatcher() {
		return KNotes.refWatcher;
	}

	/**
	 * Statically returns app's default SharedPreferences instance
	 *
	 * @return SharedPreferences object instance
	 */
	public static SharedPreferences getSharedPreferences() {
		return getAppContext().getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);
	}

	private void logUser() {
		// TODO: Use the current user's information
		// You can call any combination of these three methods
		Crashlytics.setUserIdentifier("12345");
		Crashlytics.setUserEmail("user@fabric.io");
		Crashlytics.setUserName("Test User");
	}

}
