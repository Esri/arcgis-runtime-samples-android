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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
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

  private RecyclerView mEyesRecyclerView;
  private RecyclerView mMouthRecyclerView;
  private RecyclerView mHatRecyclerView;
  private ImageView mPreviewView;
  private Spinner mColorSpinner;
  private SymbolAdapter mEyesAdapter;
  private SymbolAdapter mMouthAdapter;
  private SymbolAdapter mHatAdapter;

  private final Map<String, SymbolStyleSearchResult> mSelectedSymbols = new HashMap<>();
  private String mFaceSymbolKey;
  private ArrayList<String> mKeys = new ArrayList<>();
  private int mColor = -1;
  private int mSize = 25;

  private MapView mMapView;
  private GraphicsOverlay mGraphicsOverlay;
  private SymbolStyle mEmojiStyle;
  private MultilayerPointSymbol mCurrentMultilayerSymbol;
  private SeekBar mSizeSeekBar;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    mMapView = findViewById(R.id.mapView);
    mEyesRecyclerView = findViewById(R.id.eyesRecyclerView);
    mMouthRecyclerView = findViewById(R.id.mouthRecyclerView);
    mHatRecyclerView = findViewById(R.id.hatRecyclerView);
    mPreviewView = findViewById(R.id.previewView);

    // create a map
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC);
    // add the map to the map view
    mMapView.setMap(map);

    // create a graphics overlay to add graphics to and add it to the map view
    mGraphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(mGraphicsOverlay);

    // add a seek bar to change the size of the current multilayer symbol
    mSizeSeekBar = findViewById(R.id.sizeSeekBar);
    // disable seek bar until read permission is granted
    mSizeSeekBar.setEnabled(false);
    // set initial progress to 25
    mSizeSeekBar.setProgress(mSize);
    mSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
    // disable spinner until read permission is granted
    mColorSpinner.setEnabled(false);
    mColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position > 0) { // only set color when not on index 0 ("Select color...")
          setLayerColor(position);
        }
      }

      @Override public void onNothingSelected(AdapterView<?> parent) {

      }
    });

    // add a button to clear existing graphics from the graphics overlay
    Button clearButton = findViewById(R.id.clearButton);
    clearButton.setOnClickListener(v -> clearGraphics(mGraphicsOverlay));

    setupRecyclerViews();

    loadSymbolsFromStyleFile();
  }

  /**
   * Create a touch listener to call addGraphic on single tap.
   */
  private void createMapViewOnTouchListener() {
    // add listener to handle motion events when the user taps on the map view
    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
      @Override
      public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        if (mCurrentMultilayerSymbol != null) {
          addGraphic(mGraphicsOverlay, mapPointFrom(mMapView, motionEvent), mCurrentMultilayerSymbol);
        } else {
          logErrorToUser(MainActivity.this, getString(R.string.error_symbol_must_be_defined));
        }
        return true;
      }
    });
  }

  /**
   * Loads the stylx file and searches for all symbols contained within. Put the resulting symbols into recycler views
   * based on their category (eyes, mouth, hat, face).
   */
  private void loadSymbolsFromStyleFile() {
    // read permission accepted, enable UI elements
    mColorSpinner.setEnabled(true);
    mSizeSeekBar.setEnabled(true);
    createMapViewOnTouchListener();

    // create a SymbolStyle by passing the location of the .stylx file in the constructor
    mEmojiStyle = new SymbolStyle(getExternalFilesDir(null) + getString(R.string.mobile_style_file_path));
    // add a listener to run when the SymbolStyle has loaded
    mEmojiStyle.addDoneLoadingListener(() -> {
      if (mEmojiStyle.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
        logErrorToUser(this, getString(R.string.error_mobile_style_file_failed_load, mEmojiStyle.getLoadError()));
        return;
      }
      // get future to load default search parameters
      ListenableFuture<SymbolStyleSearchParameters> defaultSearchParametersFuture = mEmojiStyle
          .getDefaultSearchParametersAsync();
      defaultSearchParametersFuture.addDoneListener(() -> {
        try {
          SymbolStyleSearchParameters defaultSearchParameters = defaultSearchParametersFuture.get();
          // get future search symbols using the default search parameters
          ListenableFuture<List<SymbolStyleSearchResult>> symbolStyleSearchResultFuture = mEmojiStyle
              .searchSymbolsAsync(defaultSearchParameters);
          symbolStyleSearchResultFuture.addDoneListener(() -> {
            try {
              List<SymbolStyleSearchResult> symbolStyleSearchResults = symbolStyleSearchResultFuture.get();
              for (SymbolStyleSearchResult symbolStyleSearchResult : symbolStyleSearchResults) {
                // these categories are specific to this SymbolStyle
                switch (symbolStyleSearchResult.getCategory().toLowerCase(Locale.ROOT)) {
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
   * Create a new multilayer point symbol based on selected symbol keys, size, and color.
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
        // if the user has chosen a color other than "Select color..." (index 0) or "Default" (index 1)
        if (mColorSpinner.getSelectedItemPosition() > 1) {
          // unlock the first layer and set it to the selected color
          faceSymbol.getSymbolLayers().get(0).setColorLocked(false);
          faceSymbol.setColor(mColor);
        }
        // get the future to create the swatch of the multi layer symbol
        ListenableFuture<Bitmap> bitmapFuture = faceSymbol.createSwatchAsync(this, Color.TRANSPARENT);
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
   * Performed when a user taps on a symbol shown by a {@link SymbolAdapter}. Adds the tapped symbol to a hash map and
   * uses the hash map to set a list of currently selected symbol keys for each category (eyes, mouth, hat, face).
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
   * Set the color field used on the face symbol in createSwatchAsync().
   *
   * @param position in an array of colors
   */
  private void setLayerColor(int position) {
    switch (position) {
      case 1: // default
        mColor = -1;
        break;
      case 2: // red
        mColor = Color.RED;
        break;
      case 3: // green
        mColor = Color.GREEN;
        break;
      case 4: // blue
        mColor = Color.BLUE;
        break;
      default:
        logErrorToUser(this, getString(R.string.error_color_not_defined));
        break;
    }
    createSwatchAsync();
  }

  /**
   * Set the size field used on the face symbol in createSwatchAsync().
   *
   * @param progress from the size seek bar
   */
  private void setSymbolSize(int progress) {
    // set size to progress with a minimum of 1
    mSize = Math.max(1, progress);
    createSwatchAsync();
  }

  /**
   * Converts motion event to an ArcGIS map point.
   *
   * @param mapView     to convert the screen point
   * @param motionEvent containing coordinates of an Android screen point
   * @return a corresponding map point in the place
   */
  private static Point mapPointFrom(MapView mapView, MotionEvent motionEvent) {
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
  private static void addGraphic(GraphicsOverlay graphicsOverlay, Geometry geometry, Symbol symbol) {
    Graphic graphic = new Graphic(geometry, symbol);
    graphicsOverlay.getGraphics().add(graphic);
  }

  /**
   * Clear all graphics from the given graphics overlay.
   *
   * @param graphicsOverlay to clear
   */
  private static void clearGraphics(GraphicsOverlay graphicsOverlay) {
    graphicsOverlay.getGraphics().clear();
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
