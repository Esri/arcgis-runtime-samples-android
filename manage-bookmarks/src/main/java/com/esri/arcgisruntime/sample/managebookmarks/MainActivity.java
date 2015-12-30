package com.esri.arcgisruntime.sample.managebookmarks;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Bookmark;
import com.esri.arcgisruntime.mapping.BookmarkList;
import com.esri.arcgisruntime.mapping.Map;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.util.ArrayList;
import java.util.List;

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

        FloatingActionButton addBookmarkFab;

        Spinner bookmarksSpinner;

        // inflate MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);

        // create a map with the BasemapType imagery with labels
        Map mMap = new Map(Basemap.createImageryWithLabels());
        // set the map to be displayed in this view
        mMapView.setMap(mMap);

        // inflate the floating action button
        addBookmarkFab = (FloatingActionButton) findViewById(R.id.addbookmarkFAB);

        // show the dialog for acquiring bookmark name from the user
        addBookmarkFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        // get the maps BookmarkList
        mBookmarks = mMap.getBookmarks();

        // add some default bookmarks to the map
        addDefaultBookmarks();

        // populate the spinner list with default bookmark names
        bookmarksSpinner = (Spinner) findViewById(R.id.bookmarksspinner);
        mBookmarksSpinnerList = new ArrayList<>();
        mBookmarksSpinnerList.add(mBookmarks.get(0).getName());
        mBookmarksSpinnerList.add(mBookmarks.get(1).getName());
        mBookmarksSpinnerList.add(mBookmarks.get(2).getName());
        mBookmarksSpinnerList.add(mBookmarks.get(3).getName());

        // initialize the adapter for the bookmarks spinner
        mDataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mBookmarksSpinnerList);
        mDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bookmarksSpinner.setAdapter(mDataAdapter);

        // when an item is selected in the spinner set the mapview viewpoint to the selected
        // bookmark's viewpoint
        bookmarksSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mMapView.setViewpointAsync(mBookmarks.get(position).getViewpoint());
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
        mBookmark = new Bookmark("Mysterious Desert Pattern", viewpoint);
        mBookmarks.add(mBookmark);
        // Set the viewpoint to the default bookmark selected in the spinner
        mMapView.setViewpointAsync(viewpoint);

        //Strange Symbol
        viewpoint = new Viewpoint(37.401573, -116.867808, 6e3);
        mBookmark = new Bookmark("Strange Symbol", viewpoint);
        mBookmarks.add(mBookmark);

        //Guitar-Shaped Trees
        viewpoint = new Viewpoint(-33.867886, -63.985, 4e4);
        mBookmark = new Bookmark("Guitar-Shaped Trees", viewpoint);
        mBookmarks.add(mBookmark);

        //Grand Prismatic Spring
        viewpoint = new Viewpoint(44.525049, -110.83819, 6e3);
        mBookmark = new Bookmark("Grand Prismatic Spring", viewpoint);
        mBookmarks.add(mBookmark);

    }

    /**
     * add a new bookmark at the location being displayed in the MapView's current Viewpoint
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
    private void showDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Provide the bookmark name");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addBookmark(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(@NonNull DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

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

}