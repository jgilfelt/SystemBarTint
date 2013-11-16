/*
 * Copyright (C) 2013 readyState Software Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.readystatesoftware.systembartint.sample;

import static android.content.Intent.ACTION_MAIN;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SamplesListActivity extends ListActivity {

	private final IntentAdapter mAdapter = new IntentAdapter();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setListAdapter(mAdapter);
		mAdapter.refresh();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		startActivity(mAdapter.getItem(position));
	}

	private class IntentAdapter extends BaseAdapter {
		private final List<CharSequence> mNames;
		private final Map<CharSequence, Intent> mIntents;

		IntentAdapter() {
			mNames = new ArrayList<CharSequence>();
			mIntents = new HashMap<CharSequence, Intent>();
		}

		void refresh() {
			mNames.clear();
			mIntents.clear();

			final Intent mainIntent = new Intent(ACTION_MAIN, null);
			mainIntent.addCategory("com.readystatesoftware.systembartint.SAMPLE");

			PackageManager pm = getPackageManager();
			final List<ResolveInfo> matches = pm.queryIntentActivities(
					mainIntent, 0);
			for (ResolveInfo match : matches) {
				Intent intent = new Intent();
				intent.setClassName(match.activityInfo.packageName,
						match.activityInfo.name);
				final CharSequence name = match.loadLabel(pm);
				mNames.add(name);
				mIntents.put(name, intent);
			}

			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mNames.size();
		}

		@Override
		public Intent getItem(int position) {
			return mIntents.get(mNames.get(position));
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv = (TextView) convertView;
			if (convertView == null) {
				tv = (TextView) LayoutInflater.from(SamplesListActivity.this)
						.inflate(android.R.layout.simple_list_item_1, parent,
								false);
			}
			tv.setText(mNames.get(position));
			return tv;
		}
	}

}
