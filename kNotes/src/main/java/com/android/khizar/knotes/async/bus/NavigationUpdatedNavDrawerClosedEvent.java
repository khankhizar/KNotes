package com.android.khizar.knotes.async.bus;
/*Copyright 2018 Khizar Heyat Khan

         This program is free software: you can redistribute it and/or modify
         it under the terms of the GNU General Public License as published by
         the Free Software Foundation, either version 3 of the License, or
         (at your option) any later version.

         This program is distributed in the hope that it will be useful,
         but WITHOUT ANY WARRANTY; without even the implied warranty of
         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
         GNU General Public License for more details.

         You should have received a copy of the GNU General Public License
         along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
import android.util.Log;
import com.android.khizar.knotes.utils.Constants;


public class NavigationUpdatedNavDrawerClosedEvent {

	public final Object navigationItem;


	public NavigationUpdatedNavDrawerClosedEvent(Object navigationItem) {
		Log.d(Constants.TAG, this.getClass().getName());
		this.navigationItem = navigationItem;
	}
}
