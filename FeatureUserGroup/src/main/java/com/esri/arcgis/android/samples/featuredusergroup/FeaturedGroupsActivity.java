/* Copyright 2014 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the Sample code usage restrictions document for further information.
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