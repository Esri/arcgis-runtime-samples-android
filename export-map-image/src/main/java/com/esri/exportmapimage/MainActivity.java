package com.esri.exportmapimage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  private final String TAG = "ExportMapImage";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    mMapView = (MapView) findViewById(R.id.esri_map);
    Portal portal = new Portal("http://www.arcgis.com");
    PortalItem item = new PortalItem(portal, "df8bcc10430f48878b01c96e907a1fc3");

    ArcGISMap map = new ArcGISMap(item);
    mMapView.setMap(map);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        
        Snackbar.make(view, "Exports current map into a Image", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show();
        
        exportMapIntoImageAsync();
      }
    });
  }

  @Override protected void onResume() {
    super.onResume();
    if (mMapView != null) {
      mMapView.resume();
    }
  }

  @Override protected void onPause() {
    super.onPause();
    if (mMapView != null) {
      mMapView.pause();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * exports the map view into an image.
   */
  private void exportMapIntoImageAsync() {
    final ListenableFuture<Bitmap> export = mMapView.exportImageAsync();
    export.addDoneListener(new Runnable() {
      @Override public void run() {
        try {
          Bitmap currentMapImage = export.get();
          shareImage(saveToFile(currentMapImage));
        } catch (Exception e) {
          Log.d(TAG, "Fail to export map image: " +e.getCause().toString());
        }
      }
    });

  }

  /**
   * Saves the Bitmap into a local PNG file
   * @param bitmap a Bitmap image
   * @return the saved png image file path
   * @throws IOException if the file fails to create or write.
   */
  private String saveToFile(Bitmap bitmap) throws IOException {
    File file = Environment.getExternalStorageDirectory();
    String filePath = file.getAbsolutePath()+ "/ArcGIS/export_map_image"+System.currentTimeMillis()+".png";
    FileOutputStream fos = new FileOutputStream(filePath);
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

    fos.flush();
    fos.close();
    return filePath;
  }

  /**
   * Fire an Intent, start a new activity to share the image.
   * @param localAbsoluteFilePath image file path.
   */
  private void shareImage(String localAbsoluteFilePath) {
    if (localAbsoluteFilePath!=null && localAbsoluteFilePath!="") {
      Intent shareIntent = new Intent(Intent.ACTION_SEND);
      Uri mapUri = Uri.parse("file://"+localAbsoluteFilePath);
      File file = new File(mapUri.getPath());
      Log.d(TAG, "file path: " +file.getPath());

      if(file.exists()) {
        // file create success
        shareIntent.setData(mapUri);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_STREAM, mapUri);
        startActivity(Intent.createChooser(shareIntent, "Share Via"));
        //startActivityForResult(Intent.createChooser(shareIntent, "Share Via"), REQUEST_SHARE_ACTION);
      } else {
        Toast.makeText(getApplicationContext(),"fail to share the map image", Toast.LENGTH_LONG).show();
      }
    }
  }
}
