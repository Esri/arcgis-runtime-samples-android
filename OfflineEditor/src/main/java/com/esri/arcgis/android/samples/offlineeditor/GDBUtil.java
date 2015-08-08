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

package com.esri.arcgis.android.samples.offlineeditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.ags.FeatureServiceInfo;
import com.esri.core.geodatabase.Geodatabase;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.geodatabase.GeodatabaseFeatureTableEditErrors;
import com.esri.core.map.CallbackListener;
import com.esri.core.tasks.geodatabase.GenerateGeodatabaseParameters;
import com.esri.core.tasks.geodatabase.GeodatabaseStatusCallback;
import com.esri.core.tasks.geodatabase.GeodatabaseStatusInfo;
import com.esri.core.tasks.geodatabase.GeodatabaseSyncTask;
import com.esri.core.tasks.geodatabase.SyncGeodatabaseParameters;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.Map;

public class GDBUtil {

  static final String DEFAULT_FEATURE_SERVICE_URL = "http://sampleserver6.arcgisonline.com/arcgis/rest/services/Sync/WildfireSync/FeatureServer";

  static final String DEFAULT_BASEMAP_SERVICE_URL = "http://services.arcgisonline.com/ArcGIS/rest/services/ESRI_StreetMap_World_2D/MapServer";

  static final String DEFAULT_GDB_PATH = "/ArcGIS/samples/OfflineEditor/";

  static final String DEFAULT_BASEMAP_FILENAME = "/ArcGIS/samples/OfflineEditor/SanFrancisco.tpk";

  static final int[] FEATURE_SERVICE_LAYER_IDS = { 0, 1, 2 };

  protected static final String TAG = "GDBUtil";

  private static GeodatabaseSyncTask gdbTask;

  private static String gdbFileName = Environment.getExternalStorageDirectory().getPath() + DEFAULT_GDB_PATH + "offlinedata.geodatabase";

  private static String basemapFileName = Environment.getExternalStorageDirectory().getPath()
      + DEFAULT_BASEMAP_FILENAME;

  /**
   * Go back online
   */
  public static void goOnline(final OfflineEditorActivity activity, final MapView mapView) {

    if (hasInternet(activity) && !activity.onlineData) {

      DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
            	
            	// get root geodatabase directory
            	File dir = new File(Environment.getExternalStorageDirectory().getPath() + DEFAULT_GDB_PATH);
            	// create filter based on geodatabase name
				File[] fileDelete = dir.listFiles(new FileFilter() {

					@Override
					public boolean accept(File pathname) {
						return (pathname.getName()
								.startsWith("offlinedata"));
					}

				});
				// delete all geodatabase files
            	for(File file : fileDelete){
            		if(!file.delete()){
            			Log.e(TAG, "Can't remove " + file.getAbsolutePath());
            		}
            	}

              finishGoingOnline(activity, mapView);
              
              break;

            case DialogInterface.BUTTON_NEGATIVE:
              
              finishGoingOnline(activity, mapView);
              
              break;

          }
        }
      };

      AlertDialog.Builder builder = new AlertDialog.Builder(activity);
      builder.setMessage("Do you want to delete your previously downloaded geodatabase?").setPositiveButton("Yes", dialogClickListener)
          .setNegativeButton("No", dialogClickListener).show();

    } else {
      showMessage(activity, "No Internet Connection! Please try again");
    }
  }
  
  private static void finishGoingOnline(final OfflineEditorActivity activity, final MapView mapView){
    showProgress(activity, true);

    for (Layer layer : mapView.getLayers()) {
      if (layer instanceof FeatureLayer || layer instanceof ArcGISLocalTiledLayer)
        mapView.removeLayer(layer);
    }

    mapView.addLayer(new ArcGISTiledMapServiceLayer(DEFAULT_BASEMAP_SERVICE_URL), 0);

    for (int i : GDBUtil.FEATURE_SERVICE_LAYER_IDS) {
      mapView.addLayer(new ArcGISFeatureLayer(DEFAULT_FEATURE_SERVICE_URL + "/" + i,
          ArcGISFeatureLayer.MODE.ONDEMAND));
    }
    activity.onlineData = true;
  }

  /**
   * Checks whether the device is connected to a network
   */
  public static boolean hasInternet(Activity a) {
    boolean hasConnectedWifi = false;
    boolean hasConnectedMobile = false;

    ConnectivityManager cm = (ConnectivityManager) a.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
    for (NetworkInfo ni : netInfo) {
      if (ni.getTypeName().equalsIgnoreCase("wifi"))
        if (ni.isConnected())
          hasConnectedWifi = true;
      if (ni.getTypeName().equalsIgnoreCase("mobile"))
        if (ni.isConnected())
          hasConnectedMobile = true;
    }
    return hasConnectedWifi || hasConnectedMobile;
  }

  /**
   * Download data into a geodatabase
   * 
   * @param activity
   */
  public static void downloadData(final OfflineEditorActivity activity) {
    Log.i(TAG, "downloadData");
    showProgress(activity, true);
    final MapView mapView = activity.getMapView();
    downloadGeodatabase(activity, mapView);
  }

  // Fetches the geodatabase and loads onto the mapview either locally or
  // downloading from the server
  private static void downloadGeodatabase(final OfflineEditorActivity activity, final MapView mapView) {

    // request and download geodatabase from the server
    if (!isGeoDatabaseLocal()) {

      gdbTask = new GeodatabaseSyncTask(DEFAULT_FEATURE_SERVICE_URL, null);

      gdbTask.fetchFeatureServiceInfo(new CallbackListener<FeatureServiceInfo>() {

        @Override
        public void onError(Throwable e) {
          Log.e(TAG, "", e);
          showMessage(activity, e.getMessage());
          showProgress(activity, false);
        }

        @Override
        public void onCallback(FeatureServiceInfo fsInfo) {

          if (fsInfo.isSyncEnabled()) {
            requestGdbFromServer(gdbTask, activity, mapView, fsInfo);
          }
        }
      });
    }

    // load the geodatabase from the device
    else {

      // add local layers from the geodatabase
      addLocalLayers(mapView, gdbFileName, activity);
      showMessage(activity, "Loaded GDB Locally...");
      OfflineEditorActivity.progress.dismiss();
      showProgress(activity, false);
    }

  }

  /**
   * Download the geodatabase from the server.
   */
  private static void requestGdbFromServer(GeodatabaseSyncTask geodatabaseSyncTask,
      final OfflineEditorActivity activity, final MapView mapView, FeatureServiceInfo fsInfo) {

    GenerateGeodatabaseParameters params = new GenerateGeodatabaseParameters(fsInfo, mapView.getExtent(),
        mapView.getSpatialReference(), null, true);
    params.setOutSpatialRef(mapView.getSpatialReference());

    // gdb complete callback
    CallbackListener<String> gdbResponseCallback = new CallbackListener<String>() {

      @Override
      public void onCallback(String path) {

        // add local layers from the geodatabase
        addLocalLayers(mapView, path, activity);

        showMessage(activity, "Data Available Offline!");
        OfflineEditorActivity.progress.dismiss();
        showProgress(activity, false);

      }

      @Override
      public void onError(Throwable e) {
        Log.e(TAG, "", e);
        showMessage(activity, e.getMessage());
        showProgress(activity, false);
      }

    };

    GeodatabaseStatusCallback statusCallback = new GeodatabaseStatusCallback() {

      @Override
      public void statusUpdated(GeodatabaseStatusInfo status) {
        if (!status.isDownloading()) {
          showMessage(activity, status.getStatus().toString());
        }

      }
    };

    // single method does it all!
    geodatabaseSyncTask.generateGeodatabase(params, gdbFileName, false, statusCallback, gdbResponseCallback);
    showMessage(activity, "Submitting gdb job...");
  }

  private static void addLocalLayers(final MapView mapView, String gdbPath, final OfflineEditorActivity activity) {
    // remove all the feature layers from map and add a feature
    // layer from the downloaded geodatabase
    for (Layer layer : mapView.getLayers()) {
      if (layer instanceof ArcGISFeatureLayer || layer instanceof ArcGISTiledMapServiceLayer)
        mapView.removeLayer(layer);
    }

    // Add local basemap layer if it exists
    if (isBasemapLocal()) {
      mapView.addLayer(new ArcGISLocalTiledLayer(basemapFileName), 0);
    } else {
      GDBUtil.showMessage(activity, "Local Basemap tpk doesn't exist");
    }

    // add layers from the geodatabase
    Geodatabase geodatabase;
    try {
      geodatabase = new Geodatabase(gdbPath);

      for (GeodatabaseFeatureTable gdbFeatureTable : geodatabase.getGeodatabaseTables()) {
        if (gdbFeatureTable.hasGeometry())
          mapView.addLayer(new FeatureLayer(gdbFeatureTable));
      }
      activity.setTemplatePicker(null);
      activity.onlineData = false;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

  }

  // upload and synchronize local geodatabase to the server
  static void synchronize(final OfflineEditorActivity activity) {
    showProgress(activity, true);

    gdbTask = new GeodatabaseSyncTask(DEFAULT_FEATURE_SERVICE_URL, null);
    gdbTask.fetchFeatureServiceInfo(new CallbackListener<FeatureServiceInfo>() {

      @Override
      public void onError(Throwable e) {

        Log.e(TAG, "", e);
        showMessage(activity, e.getMessage());
        showProgress(activity, false);
      }

      @Override
      public void onCallback(FeatureServiceInfo objs) {
        if (objs.isSyncEnabled()) {
          doSyncAllInOne(activity);
        }
      }
    });
  }

  /**
   * Synchronizing the edits to the Map working on both online/offline mode
   * 
   * @throws Exception
   */
  private static void doSyncAllInOne(final OfflineEditorActivity activity) {

    try {
      // create local geodatabase
      Geodatabase gdb = new Geodatabase(gdbFileName);

      // get sync parameters from geodatabase
      final SyncGeodatabaseParameters syncParams = gdb.getSyncParameters();

      CallbackListener<Map<Integer, GeodatabaseFeatureTableEditErrors>> syncResponseCallback = new CallbackListener<Map<Integer, GeodatabaseFeatureTableEditErrors>>() {

        @Override
        public void onCallback(Map<Integer, GeodatabaseFeatureTableEditErrors> objs) {
          showProgress(activity, false);
          if (objs != null) {
            if (objs.size() > 0) {

              showMessage(activity, "Sync Completed With Errors");
            } else {
              showMessage(activity, "Sync Completed Without Errors");
            }

          } else {
            showMessage(activity, "Sync Completed Without Errors");
          }
          OfflineEditorActivity.progress.dismiss();
        }

        @Override
        public void onError(Throwable e) {
          Log.e(TAG, "", e);
          showMessage(activity, e.getMessage());
          showProgress(activity, false);
        }

      };

      GeodatabaseStatusCallback statusCallback = new GeodatabaseStatusCallback() {

        @Override
        public void statusUpdated(GeodatabaseStatusInfo status) {

          showMessage(activity, status.getStatus().toString());
        }
      };

      // Performs Synchronization
      gdbTask.syncGeodatabase(syncParams, gdb, statusCallback, syncResponseCallback);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Checks whether the basemap is available locally
   */
  public static boolean isBasemapLocal() {
    File file = new File(basemapFileName);
    return file.exists();
  }

  /**
   * Checks whether the geodatabase is available locally
   */
  public static boolean isGeoDatabaseLocal() {
    File file = new File(gdbFileName);
    return file.exists();
  }

  static void showProgress(final OfflineEditorActivity activity, final boolean b) {
    activity.runOnUiThread(new Runnable() {

      @Override
      public void run() {
        activity.setProgressBarIndeterminateVisibility(b);
      }
    });
  }

  static void showMessage(final OfflineEditorActivity activity, final String message) {
    activity.runOnUiThread(new Runnable() {

      @Override
      public void run() {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
      }
    });
  }

}
