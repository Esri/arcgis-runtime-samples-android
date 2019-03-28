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
import java.util.Observable;
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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.esri.arcgisruntime.symbology.Symbol;
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

  private SymbolStyleSearchResultObservable symbolStyleSearchResultObservable = new SymbolStyleSearchResultObservable();
  private String mFaceSymbolKey;

  private SymbolStyle mSymbolStyle;

  private HashMap<String, SymbolStyleSearchResult> mSelectedSymbols = new HashMap<>();

  private Symbol mCurrentMultilayerSymbol;

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
        new DefaultMapViewOnTouchListener(MainActivity.this, mMapView) {
          @Override
          public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            addGraphic(mGraphicsOverlay, mapPointFrom(mMapView, motionEvent), mCurrentMultilayerSymbol);
            return true;
          }
        });

    setupRecyclerViews();

    // add observer to Observable to be notified when symbols are loaded
    symbolStyleSearchResultObservable.addObserver((o, arg) -> {
      for (SymbolStyleSearchResult symbol : ((SymbolStyleSearchResultObservable) o).getSymbols()) {
        // these categories are specific to this SymbolStyle
        switch (symbol.getCategory().toLowerCase(Locale.ROOT)) {
          case "eyes":
            mEyesAdapter.addSymbol(symbol);
            break;
          case "mouth":
            mMouthAdapter.addSymbol(symbol);
            break;
          case "hat":
            mHatAdapter.addSymbol(symbol);
            break;
          case "face":
            mFaceSymbolKey = symbol.getKey();
            break;
        }
        Log.d(TAG, symbol.getCategory());
      }
      animateRecyclerViews();
    });

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

  private void loadSymbols() {
    // create a SymbolStyle by passing the location of the .stylx file in the constructor
    mSymbolStyle = new SymbolStyle(
        Environment.getExternalStorageDirectory() + getString(R.string.mobile_style_file_path));
    // adda listener to run when the SymbolStyle has loaded
    mSymbolStyle.addDoneLoadingListener(() -> {
      if (mSymbolStyle.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
        logErrorToUser(this, getString(R.string.error_mobile_style_file_failed_load, mSymbolStyle.getLoadError()));
        return;
      }

      // get the Future to load the default search parameters to search for symbols
      ListenableFuture<SymbolStyleSearchParameters> defaultSearchParametersFuture = mSymbolStyle
          .getDefaultSearchParametersAsync();
      SymbolStyleSearchParameters defaultSearchParameters = null;

      try {
        // wait for the Future to complete and get the result
        defaultSearchParameters = defaultSearchParametersFuture.get();
      } catch (InterruptedException | ExecutionException e) {
        logErrorToUser(this, getString(R.string.error_default_search_parameters_load_failed, e.getMessage()));
      }

      if (defaultSearchParameters == null) {
        return;
      }

      // get the Future to perform the symbol search using the default search parameters previously obtained
      ListenableFuture<List<SymbolStyleSearchResult>> symbolStyleSearchResultFuture = mSymbolStyle
          .searchSymbolsAsync(defaultSearchParameters);
      try {
        // wait for the future to complete and get the result
        symbolStyleSearchResultObservable.setSymbols(symbolStyleSearchResultFuture.get());
      } catch (InterruptedException | ExecutionException e) {
        logErrorToUser(this, getString(R.string.error_searching_for_symbols_failed, e.getMessage()));
      }
    });

    // load the SymbolStyle
    mSymbolStyle.loadAsync();
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
    ArrayList<String> keys = new ArrayList<>();
    // add the face symbol first as it should appear on the bottom of the multi layer symbol
    keys.add(mFaceSymbolKey);
    // loop through the selected symbols map's values to obtain the symbol keys
    for (SymbolStyleSearchResult symbolStyleSearchResult : mSelectedSymbols.values()) {
      // add the symbol key to the map
      keys.add(symbolStyleSearchResult.getKey());
    }

    // get the Future to perform the generation of the multi layer symbol
    ListenableFuture<Symbol> symbolFuture = mSymbolStyle.getSymbolAsync(keys);
    Symbol multilayerSymbol = null;
    try {
      // wait for the Future to complete and get the result
      multilayerSymbol = symbolFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      logErrorToUser(this, getString(R.string.error_loading_multilayer_symbol_failed, e.getMessage()));
    }

    if (multilayerSymbol == null) {
      return;
    }

    // get the Future to create the swatch of the multi layer symbol
    ListenableFuture<Bitmap> bitmapFuture = multilayerSymbol.createSwatchAsync(this, Color.TRANSPARENT);
    try {
      // wait for the Future to complete and get the result
      // this will block. Can it be moved to a separate thread?
      Bitmap bitmap = bitmapFuture.get();
      mPreviewView.setImageBitmap(bitmap);
      // set this field to enable us to add this symbol to the graphics overlay
      mCurrentMultilayerSymbol = multilayerSymbol;
    } catch (InterruptedException | ExecutionException e) {
      logErrorToUser(this, getString(R.string.error_loading_multilayer_bitmap_failed, e.getMessage()));
    }
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
    Graphic g = new Graphic(geometry, symbol);
    graphicsOverlay.getGraphics().add(g);
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

  /**
   * {@link RecyclerView.Adapter} subclass that displays symbols
   */
  private class SymbolAdapter extends RecyclerView.Adapter<SymbolAdapter.ViewHolder> {

    private ArrayList<SymbolStyleSearchResult> symbols = new ArrayList<>();
    private final OnSymbolPreviewTapListener mOnSymbolPreviewTapListener;

    public SymbolAdapter(OnSymbolPreviewTapListener onSymbolPreviewTapListener) {
      mOnSymbolPreviewTapListener = onSymbolPreviewTapListener;
    }

    @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
      return new ViewHolder(
          LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_symbol_adapter_item, viewGroup, false));
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
      viewHolder.bind(symbols.get(i), mOnSymbolPreviewTapListener);
    }

    @Override public int getItemCount() {
      return symbols.size();
    }

    void addSymbol(SymbolStyleSearchResult symbol) {
      this.symbols.add(symbol);
      notifyItemInserted(symbols.size() - 1);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

      private final ImageView mImageView;

      ViewHolder(@NonNull View itemView) {
        super(itemView);
        mImageView = itemView.findViewById(R.id.imageView);
      }

      private void bind(SymbolStyleSearchResult symbol, OnSymbolPreviewTapListener onSymbolPreviewTapListener) {
        // get the Future to create the swatch of the multi layer symbol
        ListenableFuture<Bitmap> bitmapFuture = symbol.getSymbol().createSwatchAsync(itemView.getContext(),
            Color.TRANSPARENT);
        try {
          // wait for the Future to complete and get the result
          // this will block. Can it be moved to a separate thread?
          Bitmap bitmap = bitmapFuture.get();
          mImageView.setImageBitmap(bitmap);
        } catch (InterruptedException | ExecutionException e) {
          logErrorToUser(itemView.getContext(), getString(R.string.error_loading_symbol_bitmap_failed, e.getMessage()));
        }
        itemView.setOnClickListener(v -> {
          onSymbolPreviewTapListener.onSymbolPreviewTap(symbol);
        });
      }
    }
  }

  /**
   * Subclass of {@link Observable} that can be subscribed to to be notified when a symbol search has been completed
   */
  private class SymbolStyleSearchResultObservable extends Observable {

    private List<SymbolStyleSearchResult> mSymbols;

    List<SymbolStyleSearchResult> getSymbols() {
      return mSymbols;
    }

    void setSymbols(List<SymbolStyleSearchResult> symbols) {
      if (symbols.size() > 0) {
        mSymbols = symbols;
        setChanged();
        notifyObservers();
      }
    }
  }

  private void logErrorToUser(Context context, String message) {
    Log.e(TAG, message);
    runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
  }
}

interface OnSymbolPreviewTapListener {
  void onSymbolPreviewTap(SymbolStyleSearchResult symbol);
}
