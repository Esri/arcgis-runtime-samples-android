package com.esri.arcgisruntime.sample.downloadpreplannedmap;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapTask;
import com.esri.arcgisruntime.tasks.offlinemap.PreplannedMapArea;

public class MainActivity extends AppCompatActivity {

  String TAG = MainActivity.class.getSimpleName();
  private ArrayList<DrawerItem> mDrawerItemList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // define the local path where the preplanned map will be stored
    final String localPreplannedMapDir =
        getCacheDir().toString() + File.separator + getString(R.string.file_name);
    Log.d(TAG, localPreplannedMapDir);

    // create portal that contains the portal item
    Portal portal = new Portal(getString(R.string.portal_url));

    // create webmap based on the id
    PortalItem webmapItem = new PortalItem(portal, getString(R.string.portal_item_ID));

    // create task and load it
    OfflineMapTask offlineMapTask = new OfflineMapTask(webmapItem);

    // get related preplanned areas
    ListenableFuture<List<PreplannedMapArea>> preplannedAreasFuture = offlineMapTask.getPreplannedMapAreasAsync();

    preplannedAreasFuture.addDoneListener(() -> {

      try {
        // get the list of preplanned map areas
        List<PreplannedMapArea> preplannedAreas = preplannedAreasFuture.get();

        // load each area
        for (PreplannedMapArea area : preplannedAreas) {
          area.loadAsync();
        }



      } catch (InterruptedException | ExecutionException e) {
        Toast.makeText(MainActivity.this, "Error loading preplanned areas: " + e.getMessage(), Toast.LENGTH_LONG)
            .show();
        Log.e(TAG, "Error loading preplanned areas: " + e.getMessage());
      }

    });

    //Dummy Data
    mDrawerItemList = new ArrayList<DrawerItem>();
    DrawerItem item = new DrawerItem();
    item.setIcon(R.drawable.inbox);
    item.setTitle("Inbox");
    mDrawerItemList.add(item);
    DrawerItem item2 = new DrawerItem();
    item2.setIcon(R.drawable.send);
    item2.setTitle("Send");
    mDrawerItemList.add(item2);

    DrawerAdapter adapter = new DrawerAdapter(mDrawerItemList);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    mRecyclerView.setAdapter(adapter);

/*
    // Set areas to the UI
    preplannedAreasList.ItemsSource = preplannedAreasFuture;

    // Create layout for the narrow view
    foreach(var area in preplannedAreasFuture)
    {
      var menuItem = new MenuFlyoutItem() {
        Text =area.PortalItem.Title,DataContext =area
      };
      menuItem.Click += OnMenuDownloadMapAreaClicked;
      mapAreaFlyout.Items.Add(menuItem);
    }

    downloadNotificationText.Visibility = Visibility.Visible;
    busyIndicator.Visibility = Visibility.Collapsed;
*/
  }
}
