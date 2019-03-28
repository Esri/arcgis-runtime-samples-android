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
import java.util.Observer;
import java.util.concurrent.ExecutionException;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
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

public class MainActivity extends AppCompatActivity implements Observer, OnSymbolPreviewTapListener {

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

  private Symbols symbols = new Symbols();
  private String mFaceSymbolKey;

  private SymbolStyle mSymbolStyle;

  private HashMap<String, SymbolStyleSearchResult> mSelectedSymbols = new HashMap<>();

  private Symbol mCurrentMultilayerSymbol;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mMapView = findViewById(R.id.mapView);
    ArcGISMap map = new ArcGISMap(Basemap.createTopographic());
    mMapView.setMap(map);

    mGraphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(mGraphicsOverlay);

    // add listener to handle motion events, which only responds once a geodatabase is loaded
    mMapView.setOnTouchListener(
        new DefaultMapViewOnTouchListener(MainActivity.this, mMapView) {
          @Override
          public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            addGraphic(mGraphicsOverlay, mapPointFrom(motionEvent), mCurrentMultilayerSymbol);
            return true;
          }
        });

    mEyesRecyclerView = findViewById(R.id.eyesRecyclerView);
    mMouthRecyclerView = findViewById(R.id.mouthRecyclerView);
    mHatRecyclerView = findViewById(R.id.hatRecyclerView);
    mPreviewView = findViewById(R.id.previewView);

    setupRecyclerViews();

    symbols.addObserver(this);

    requestPermissions();
  }

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

  private void loadSymbols() {
    mSymbolStyle = new SymbolStyle(
        Environment.getExternalStorageDirectory() + getString(R.string.mobile_style_file_path));
    mSymbolStyle.addDoneLoadingListener(() -> {
      if (mSymbolStyle.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
        String error = "Mobile style file failed to load: " + mSymbolStyle.getLoadError();
        Log.e(TAG, error);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        return;
      }

      ListenableFuture<SymbolStyleSearchParameters> defaultSearchParametersFuture = mSymbolStyle
          .getDefaultSearchParametersAsync();
      SymbolStyleSearchParameters defaultSearchParameters = null;

      try {
        defaultSearchParameters = defaultSearchParametersFuture.get();
      } catch (InterruptedException | ExecutionException e) {
        String error = "Loading default symbol style search parameters failed: " + e.getMessage();
        Log.e(TAG, error);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
      }

      if (defaultSearchParameters == null) {
        return;
      }

      ListenableFuture<List<SymbolStyleSearchResult>> symbolStyleSearchResultFuture = mSymbolStyle
          .searchSymbolsAsync(defaultSearchParameters);
      try {
        symbols.setSymbols(symbolStyleSearchResultFuture.get());
      } catch (InterruptedException | ExecutionException e) {
        String error = "Searching for symbols failed: " + e.getMessage();
        Log.e(TAG, error);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
      }
    });
    mSymbolStyle.loadAsync();
  }

  @Override public void update(Observable o, Object arg) {
    if (o instanceof Symbols) {
      for (SymbolStyleSearchResult symbol : ((Symbols) o).getSymbols()) {
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
    }
  }

  @Override public void onSymbolPreviewTap(SymbolStyleSearchResult symbol) {
    mSelectedSymbols.put(symbol.getCategory(), symbol);

    ArrayList<String> keys = new ArrayList<>();
    keys.add(mFaceSymbolKey);
    for (SymbolStyleSearchResult symbolStyleSearchResult : mSelectedSymbols.values()) {
      keys.add(symbolStyleSearchResult.getKey());
    }

    ListenableFuture<Symbol> symbolFuture = mSymbolStyle.getSymbolAsync(keys);
    Symbol multilayerSymbol = null;
    try {
      multilayerSymbol = symbolFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }

    if (multilayerSymbol == null) {
      return;
    }

    ListenableFuture<Bitmap> bitmapFuture = multilayerSymbol.createSwatchAsync(this, Color.TRANSPARENT);
    try {
      // this will block. Can it be moved to a separate thread?
      Bitmap bitmap = bitmapFuture.get();
      mPreviewView.setImageBitmap(bitmap);
      mCurrentMultilayerSymbol = multilayerSymbol;
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  /**
   * Converts motion event to an ArcGIS map point.
   *
   * @param motionEvent containing coordinates of an Android screen point
   * @return a corresponding map point in the place
   */
  private Point mapPointFrom(MotionEvent motionEvent) {
    // get the screen point
    android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),
        Math.round(motionEvent.getY()));
    // return the point that was clicked in map coordinates
    return mMapView.screenToLocation(screenPoint);
  }

  private void addGraphic(GraphicsOverlay graphicsOverlay, Point point, Symbol symbol) {
    Graphic g = new Graphic(point, symbol);
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
      Toast.makeText(this, getResources().getString(R.string.error_read_permission_denied), Toast.LENGTH_SHORT).show();
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
        ListenableFuture<Bitmap> bitmapFuture = symbol.getSymbol().createSwatchAsync(itemView.getContext(),
            Color.TRANSPARENT);
        try {
          // this will block. Can it be moved to a separate thread?
          Bitmap bitmap = bitmapFuture.get();
          mImageView.setImageBitmap(bitmap);
        } catch (InterruptedException | ExecutionException e) {
          e.printStackTrace();
        }
        itemView.setOnClickListener(v -> {
          onSymbolPreviewTapListener.onSymbolPreviewTap(symbol);
        });
      }
    }
  }

  private class Symbols extends Observable {

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
}

interface OnSymbolPreviewTapListener {
  void onSymbolPreviewTap(SymbolStyleSearchResult symbol);
}
