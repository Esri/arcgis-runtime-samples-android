/* Copyright 2015 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgis.android.samples.featuredusergroup;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;

/**
 * The primary purpose of this sample is to show how to log in to a portal, fetch info about the
 * featured groups, fetch info about webmap items in a particular group, then fetch and display a
 * particular webmap.
 * <p>
 * A secondary purpose is to demonstrate two different techniques for handling device configuration
 * changes.
 * <p>
 * This is the main activity of this sample. It simply hosts the GroupsFragment and the
 * ItemsFragment. On startup it launches a GroupsFragment.
 */
public class FeaturedGroupsActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Set content view and setup action bar
    setContentView(R.layout.featured_groups_activity);
    getActionBar().setDisplayHomeAsUpEnabled(false);
    getActionBar().setHomeButtonEnabled(false);

    // Only if this is a fresh start, kick off the GroupsFragment
    if (savedInstanceState == null) {
      FragmentManager fragMgr = getFragmentManager();
      fragMgr.beginTransaction().add(R.id.main_fragment_container, new GroupsFragment(), null)
          .commit();
    }
  }

}