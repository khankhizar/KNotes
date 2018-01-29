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
package com.android.khizar.knotes.intro;

import android.graphics.Color;
import android.os.Bundle;


public class IntroSlide3 extends IntroFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		background.setBackgroundColor(Color.parseColor("#8bc34a"));
		title.setText(com.android.khizar.knotes.R.string.categories);
		image.setImageResource(com.android.khizar.knotes.R.drawable.slide3);
		description.setText(com.android.khizar.knotes.R.string.tour_listactivity_tag_detail);
	}
}