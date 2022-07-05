/* Copyright 2016 Esri
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

package com.esri.arcgisruntime.sample.managebookmarks;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Bookmark;
import com.esri.arcgisruntime.mapping.BookmarkList;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private BookmarkList mBookmarks;
  private List<String> mBookmarksSpinnerList;
  private Bookmark mBookmark;
  private ArrayAdapter<String> mDataAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);

    // create a map with the Basemap Style imagery with labels
    ArcGISMap mMap = new ArcGISMap(BasemapStyle.ARCGIS_IMAGERY);
    // set the map to be displayed in this view
    mMapView.setMap(mMap);

    // inflate the floating action button
    FloatingActionButton addBookmarkFab = findViewById(R.id.addbookmarkFAB);

    // show the dialog for acquiring bookmark name from the user
    addBookmarkFab.setOnClickListener(v -> showDialog(v.getContext()));

    // get the maps BookmarkList
    mBookmarks = mMap.getBookmarks();

    // add some default bookmarks to the map
    addDefaultBookmarks();

    // populate the spinner list with default bookmark names
    Spinner bookmarksSpinner = findViewById(R.id.bookmarksspinner);
    mBookmarksSpinnerList = new ArrayList<>();
    mBookmarksSpinnerList.add(mBookmarks.get(0).getName());
    mBookmarksSpinnerList.add(mBookmarks.get(1).getName());
    mBookmarksSpinnerList.add(mBookmarks.get(2).getName());
    mBookmarksSpinnerList.add(mBookmarks.get(3).getName());

    // initialize the adapter for the bookmarks spinner
    mDataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mBookmarksSpinnerList);
    mDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    bookmarksSpinner.setAdapter(mDataAdapter);

    // when an item is selected in the spinner set the mapview viewpoint to the selected bookmark's viewpoint
    bookmarksSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mMapView.setBookmarkAsync(mBookmarks.get(position));
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });
  }

  /**
   * adds the default bookmarks to the maps BookmarkList
   */
  private void addDefaultBookmarks() {

    Viewpoint viewpoint;

    //Mysterious Desert Pattern
    viewpoint = new Viewpoint(27.3805833, 33.6321389, 6e3);
    mBookmark = new Bookmark(getResources().getString(R.string.desert_pattern), viewpoint);
    mBookmarks.add(mBookmark);
    // Set the viewpoint to the default bookmark selected in the spinner
    mMapView.setBookmarkAsync(mBookmarks.get(0));

    //Strange Symbol
    viewpoint = new Viewpoint(37.401573, -116.867808, 6e3);
    mBookmark = new Bookmark(getResources().getString(R.string.strange_symbol), viewpoint);
    mBookmarks.add(mBookmark);

    //Guitar-Shaped Trees
    viewpoint = new Viewpoint(-33.867886, -63.985, 4e4);
    mBookmark = new Bookmark(getResources().getString(R.string.guitar_trees), viewpoint);
    mBookmarks.add(mBookmark);

    //Grand Prismatic Spring
    viewpoint = new Viewpoint(44.525049, -110.83819, 6e3);
    mBookmark = new Bookmark(getResources().getString(R.string.prismatic_spring), viewpoint);
    mBookmarks.add(mBookmark);

  }

  /**
   * add a new bookmark at the location being displayed in the MapView's current Viewpoint
   *
   * @param Name of the new bookmark
   */
  private void addBookmark(String Name) {

    mBookmark = new Bookmark(Name, mMapView.getCurrentViewpoint(Viewpoint.Type.BOUNDING_GEOMETRY));
    mBookmarks.add(mBookmark);
    mBookmarksSpinnerList.add(Name);
    mDataAdapter.notifyDataSetChanged();
  }

  /**
   * shows dialog that prompts user to add a name for the new Bookmark
   */
  private void showDialog(Context context) {

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getResources().getString(R.string.alert_dialog_title));

    // Set up the input
    final EditText input = new EditText(this);
    input.setInputType(InputType.TYPE_CLASS_TEXT);
    builder.setView(input);

    // Set up the buttons
    builder.setPositiveButton("OK", (dialog, which) -> {
      // get the input from EditText
      String bookmarkName = input.getText().toString();
      // check if EditText is not empty & bookmark name has not been used
      if (bookmarkName.length() > 0 && !mBookmarksSpinnerList.contains(bookmarkName)) {
        addBookmark(bookmarkName);
      } else {
        // display toast explaining bookmark not set
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.bookmark_not_saved),
            Toast.LENGTH_LONG).show();
        dialog.cancel();
      }
    });
    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

    builder.show();

  }

  @Override
  protected void onPause() {
    super.onPause();
    mMapView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mMapView.dispose();
  }
}
