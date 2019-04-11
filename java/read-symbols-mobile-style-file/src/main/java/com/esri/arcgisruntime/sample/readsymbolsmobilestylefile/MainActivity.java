/*
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.sample.readsymbolsmobilestylefile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.MultilayerPointSymbol;
import com.esri.arcgisruntime.symbology.Symbol;
import com.esri.arcgisruntime.symbology.SymbolLayer;
import com.esri.arcgisruntime.symbology.SymbolStyle;
import com.esri.arcgisruntime.symbology.SymbolStyleSearchParameters;
import com.esri.arcgisruntime.symbology.SymbolStyleSearchResult;

public class MainActivity extends AppCompatActivity implements OnSymbolPreviewTapListener {

  private static final String TAG = MainActivity.class.getSimpleName();
  private static final int PERM_REQUEST_CODE = 1;
  private static final String[] PERMISSIONS = { Manifest.permission.READ_EXTERNAL_STORAGE };

  private MapView mMapView;
  private GraphicsOverlay mGraphicsOverlay;
  private RecyclerView mEyesRecyclerView;
  private RecyclerView mMouthRecyclerView;
  private RecyclerView mHatRecyclerView;
  private ImageView mPreviewView;
  private SymbolAdapter mEyesAdapter;
  private SymbolAdapter mMouthAdapter;
  private SymbolAdapter mHatAdapter;

  private String mFaceSymbolKey;

  private SymbolStyle mEmojiStyle;

  private final Map<String, SymbolStyleSearchResult> mSelectedSymbols = new HashMap<>();

  private MultilayerPointSymbol mCurrentMultilayerSymbol;
  private ArrayList<String> mKeys;
  private int mColor = -1;
  private Spinner mColorSpinner;
  private int mSize = 25;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mMapView = findViewById(R.id.mapView);
    mEyesRecyclerView = findViewById(R.id.eyesRecyclerView);
    mMouthRecyclerView = findViewById(R.id.mouthRecyclerView);
    mHatRecyclerView = findViewById(R.id.hatRecyclerView);
    mPreviewView = findViewById(R.id.previewView);

    // create a map
    ArcGISMap map = new ArcGISMap(Basemap.createTopographic());
    // add the map to the map view
    mMapView.setMap(map);

    // create a graphics overlay to add graphics to and add it to the map view
    mGraphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(mGraphicsOverlay);

    // add listener to handle motion events when the user taps on the map view
    mMapView.setOnTouchListener(
        new DefaultMapViewOnTouchListener(this, mMapView) {
          @Override
          public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            addGraphic(mGraphicsOverlay, mapPointFrom(mMapView, motionEvent), mCurrentMultilayerSymbol);
            return true;
          }
        });

    mKeys = new ArrayList<>();

    // add a button to clear existing graphics from the graphics overlay
    Button clearButton = findViewById(R.id.clearButton);
    clearButton.setOnClickListener(v -> clearGraphics(mGraphicsOverlay));

    // add a seek bar to change the size of the current multilayer symbol
    SeekBar sizeSeekBar = findViewById(R.id.sizeSeekBar);
    // set initial progress to 25
    sizeSeekBar.setProgress(mSize);
    sizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        setSymbolSize(progress);
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });

    // add a spinner to change the color of the first layer of the multilayer symbol
    mColorSpinner = findViewById(R.id.colorSpinner);
    mColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        setLayerColor(position);
      }

      @Override public void onNothingSelected(AdapterView<?> parent) {

      }
    });

    setupRecyclerViews();

    requestPermissions();
  }

  /**
   * Setup {@link RecyclerView}s to display symbols
   */
  private void setupRecyclerViews() {
    mEyesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    mEyesAdapter = new SymbolAdapter(this);
    mEyesRecyclerView.setAdapter(mEyesAdapter);

    mMouthRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    mMouthAdapter = new SymbolAdapter(this);
    mMouthRecyclerView.setAdapter(mMouthAdapter);

    mHatRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    mHatAdapter = new SymbolAdapter(this);
    mHatRecyclerView.setAdapter(mHatAdapter);
  }

  /**
   * Animates {@link RecyclerView}s to inform user that there are more options available
   */
  private void animateRecyclerViews() {
    Handler handler = new Handler();

    final Runnable resetScrollRunnable = () -> {
      mHatRecyclerView.smoothScrollBy(-100, 0);
      mEyesRecyclerView.smoothScrollBy(-100, 0);
      mMouthRecyclerView.smoothScrollBy(-100, 0);
    };

    Runnable startScrollRunnable = () -> {
      mHatRecyclerView.smoothScrollBy(100, 0);
      mEyesRecyclerView.smoothScrollBy(100, 0);
      mMouthRecyclerView.smoothScrollBy(100, 0);
      handler.postDelayed(resetScrollRunnable, 1000);
    };

    runOnUiThread(() -> handler.postDelayed(startScrollRunnable, 2000));
  }

  /**
   *
   */
  private void loadSymbols() {
    // create a SymbolStyle by passing the location of the .stylx file in the constructor
    mEmojiStyle = new SymbolStyle(
        Environment.getExternalStorageDirectory() + getString(R.string.mobile_style_file_path));
    // add a listener to run when the SymbolStyle has loaded
    mEmojiStyle.addDoneLoadingListener(() -> {
      if (mEmojiStyle.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
        logErrorToUser(this, getString(R.string.error_mobile_style_file_failed_load, mEmojiStyle.getLoadError()));
        return;
      }
      // get the Future to load the default search parameters to search for symbols
      ListenableFuture<SymbolStyleSearchParameters> defaultSearchParametersFuture = mEmojiStyle
          .getDefaultSearchParametersAsync();
      // wait for the Future to complete and get the result
      defaultSearchParametersFuture.addDoneListener(() -> {
        try {
          SymbolStyleSearchParameters defaultSearchParameters = defaultSearchParametersFuture.get();
          // get the Future to perform the symbol search using the default search parameters previously obtained
          ListenableFuture<List<SymbolStyleSearchResult>> symbolStyleSearchResultFuture = mEmojiStyle
              .searchSymbolsAsync(defaultSearchParameters);
          // wait for the future to complete and get the result
          symbolStyleSearchResultFuture.addDoneListener(() -> {
            try {
              List<SymbolStyleSearchResult> symbolStyleSearchResults = symbolStyleSearchResultFuture.get();
              for (SymbolStyleSearchResult symbolStyleSearchResult : symbolStyleSearchResults) {
                // these categories are specific to this SymbolStyle
                String category = symbolStyleSearchResult.getCategory().toLowerCase(Locale.ROOT);
                switch (category) {
                  case "eyes":
                    mEyesAdapter.addSymbol(symbolStyleSearchResult);
                    break;
                  case "mouth":
                    mMouthAdapter.addSymbol(symbolStyleSearchResult);
                    break;
                  case "hat":
                    mHatAdapter.addSymbol(symbolStyleSearchResult);
                    break;
                  case "face":
                    mFaceSymbolKey = symbolStyleSearchResult.getKey();
                    break;
                  default:
                    logErrorToUser(this, getString(R.string.error_unknown_symbol_search_result_category, category));
                    break;
                }
                animateRecyclerViews();
              }
            } catch (InterruptedException | ExecutionException e) {
              logErrorToUser(this, getString(R.string.error_searching_for_symbols_failed, e.getMessage()));
            }
          });
        } catch (InterruptedException | ExecutionException e) {
          logErrorToUser(this, getString(R.string.error_default_search_parameters_load_failed, e.getMessage()));
        }
      });
    });
    // load the SymbolStyle
    mEmojiStyle.loadAsync();
  }

  /**
   * Performed when a user taps on a symbol shown by a {@link SymbolAdapter}
   *
   * @param symbol the user tapped on
   */
  @Override public void onSymbolPreviewTap(SymbolStyleSearchResult symbol) {
    // add the symbol that was tapped on to the map of selected symbols, replacing an old value if the category has
    // already been selected
    mSelectedSymbols.put(symbol.getCategory(), symbol);
    // create a list of Strings to provide to the method that retrieves a multi layer symbol
    mKeys = new ArrayList<>();
    // add the face symbol first as it should appear on the bottom of the multi layer symbol
    mKeys.add(mFaceSymbolKey);
    // loop through the selected symbols map's values to obtain the symbol keys
    for (SymbolStyleSearchResult symbolStyleSearchResult : mSelectedSymbols.values()) {
      // add the symbol key to the map
      mKeys.add(symbolStyleSearchResult.getKey());
    }
    createSwatchAsync();
  }

  /**
   * Create a new multilayer point symbol based on selected symbol keys, selected size and selected color.
   */
  private void createSwatchAsync() {
    // get the Future to perform the generation of the multi layer symbol
    ListenableFuture<Symbol> symbolFuture = mEmojiStyle.getSymbolAsync(mKeys);
    symbolFuture.addDoneListener(() -> {
      try {
        // wait for the Future to complete and get the result
        MultilayerPointSymbol faceSymbol = (MultilayerPointSymbol) symbolFuture.get();
        if (faceSymbol == null) {
          return;
        }

        // set size to current size as defined by seek bar
        faceSymbol.setSize(mSize);

        // lock the color on all symbol layers
        for (SymbolLayer symbolLayer : faceSymbol.getSymbolLayers()) {
          symbolLayer.setColorLocked(true);
        }

        // if the user has chosen a color other than default (spinner position 0)
        if (mColorSpinner.getSelectedItemPosition() > 0) {
          // unlock the first layer and set it to the selected color
          faceSymbol.getSymbolLayers().get(0).setColorLocked(false);
          faceSymbol.setColor(mColor);
        }

        // get the Future to create the swatch of the multi layer symbol
        ListenableFuture<Bitmap> bitmapFuture = faceSymbol.createSwatchAsync(this, Color.TRANSPARENT);
        // wait for the Future to complete and get the result
        bitmapFuture.addDoneListener(() -> {
          try {
            Bitmap bitmap = bitmapFuture.get();
            mPreviewView.setImageBitmap(bitmap);
            // set this field to enable us to add this symbol to the graphics overlay
            mCurrentMultilayerSymbol = faceSymbol;
          } catch (InterruptedException | ExecutionException e) {
            logErrorToUser(this, getString(R.string.error_loading_multilayer_bitmap_failed, e.getMessage()));
          }
        });
      } catch (InterruptedException | ExecutionException e) {
        logErrorToUser(this, getString(R.string.error_loading_multilayer_symbol_failed, e.getMessage()));
      }
    });
  }

  /**
   * Converts motion event to an ArcGIS map point.
   *
   * @param mapView     to convert the screen point
   * @param motionEvent containing coordinates of an Android screen point
   * @return a corresponding map point in the place
   */
  private Point mapPointFrom(MapView mapView, MotionEvent motionEvent) {
    // get the screen point
    android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),
        Math.round(motionEvent.getY()));
    // return the point that was clicked in map coordinates
    return mapView.screenToLocation(screenPoint);
  }

  /**
   * Create a {@link Graphic} from a {@link Geometry} and {@link Symbol} and add it to a {@link GraphicsOverlay}
   *
   * @param graphicsOverlay to add the graphic to
   * @param geometry        graphic's geometry on a {@link MapView}
   * @param symbol          to be added to the {@link GraphicsOverlay}
   */
  private void addGraphic(GraphicsOverlay graphicsOverlay, Geometry geometry, Symbol symbol) {
    Graphic graphic = new Graphic(geometry, symbol);
    graphicsOverlay.getGraphics().add(graphic);
  }

  /**
   * Clear all graphics from the given graphics overlay.
   *
   * @param graphicsOverlay to clear
   */
  private void clearGraphics(GraphicsOverlay graphicsOverlay) {
    graphicsOverlay.getGraphics().clear();
  }

  /**
   * Get the color for the given spinner position.
   *
   * @param position in an array of colors
   */
  private void setLayerColor(int position) {
    switch (position) {
      case 0: // default
        mColor = -1;
        break;
      case 1: // red
        mColor = Color.RED;
        break;
      case 2: // green
        mColor = Color.GREEN;
        break;
      case 3: // blue
        mColor = Color.BLUE;
        break;
      default:
        logErrorToUser(this, getString(R.string.error_color_not_defined));
        break;
    }
    createSwatchAsync();
  }

  private void setSymbolSize(int progress) {
    // set size to progress with a minimum of 1
    mSize = Math.max(1, progress);
    createSwatchAsync();
  }

  /**
   * Request permissions on the device.
   */
  private void requestPermissions() {
    // For API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(this, PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED) {
      loadSymbols();
    } else {
      // request permission
      ActivityCompat.requestPermissions(this, PERMISSIONS, PERM_REQUEST_CODE);
    }
  }

  /**
   * Handle the permissions request response.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      loadSymbols();
    } else {
      // report to user that permission was denied
      logErrorToUser(this, getResources().getString(R.string.error_read_permission_denied));
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onPause() {
    mMapView.pause();
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    mMapView.dispose();
    super.onDestroy();
  }

  private void logErrorToUser(Context context, String message) {
    Log.e(TAG, message);
    runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
  }
}

interface OnSymbolPreviewTapListener {
  void onSymbolPreviewTap(SymbolStyleSearchResult symbol);
}
