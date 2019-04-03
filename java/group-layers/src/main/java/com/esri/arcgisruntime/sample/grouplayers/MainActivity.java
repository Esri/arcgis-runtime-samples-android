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

package com.esri.arcgisruntime.sample.grouplayers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.GroupLayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Surface;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.samples.grouplayers.R;
import com.esri.arcgisruntime.util.ListenableList;

public class MainActivity extends AppCompatActivity implements OnLayerCheckedChangedListener {

  private SceneView mSceneView;
  private BottomSheetBehavior mBottomSheetBehavior;
  private RecyclerView mLayersRecyclerView;
  private LayersAdapter mLayersAdapter;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mSceneView = findViewById(R.id.sceneView);
    mLayersRecyclerView = findViewById(R.id.layersRecyclerView);

    View bottomSheet = findViewById(R.id.bottomSheet);
    mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    mSceneView.setOnTouchListener(new DefaultSceneViewOnTouchListener(mSceneView) {
      @Override public boolean onTouch(View view, MotionEvent motionEvent) {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
          mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
          return true;
        }
        return super.onTouch(view, motionEvent);
      }
    });

    // create a scene with a basemap and add it to the scene view
    ArcGISScene scene = new ArcGISScene();
    scene.setBasemap(Basemap.createImagery());
    mSceneView.setScene(scene);

    // set the base surface with world elevation
    Surface surface = new Surface();
    surface.getElevationSources().add(new ArcGISTiledElevationSource(
        "http://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer"));
    scene.setBaseSurface(surface);

    // create different types of layers
    ArcGISSceneLayer devOne = new ArcGISSceneLayer(
        "https://scenesampleserverdev.arcgis.com/arcgis/rest/services/Hosted/DevA_Trees/SceneServer/layers/0");
    ArcGISSceneLayer devTwo = new ArcGISSceneLayer(
        "https://scenesampleserverdev.arcgis.com/arcgis/rest/services/Hosted/DevA_Pathways/SceneServer/layers/0");
    ArcGISSceneLayer devThree = new ArcGISSceneLayer(
        "https://scenesampleserverdev.arcgis.com/arcgis/rest/services/Hosted/DevA_BuildingShell_Textured/SceneServer/layers/0");
    ArcGISSceneLayer nonDevOne = new ArcGISSceneLayer(
        "https://scenesampleserverdev.arcgis.com/arcgis/rest/services/Hosted/PlannedDemo_BuildingShell/SceneServer/layers/0");
    FeatureLayer nonDevTwo = new FeatureLayer(new ServiceFeatureTable(
        "https://services.arcgis.com/P3ePLMYs2RVChkJx/arcgis/rest/services/DevelopmentProjectArea/FeatureServer/0"));

    // create a group layer from scratch by adding the layers as children
    GroupLayer groupLayer = new GroupLayer();
    groupLayer.setName("Group: Dev A");
    groupLayer.getLayers().addAll(Arrays.asList(devOne, devTwo, devThree));

    // add the group layer and other layers to the scene as operational layers
    scene.getOperationalLayers().addAll(Arrays.asList(groupLayer, nonDevOne, nonDevTwo));

    // zoom to the extent of the group layer when the child layers are loaded
    ListenableList<Layer> layers = groupLayer.getLayers();
    for (Layer childLayer : layers) {
      childLayer.addDoneLoadingListener(() -> {
        if (childLayer.getLoadStatus() == LoadStatus.LOADED) {
          mSceneView.setViewpointCamera(new Camera(groupLayer.getFullExtent().getCenter(), 700, 0, 60, 0));
        }
      });
    }

    setupRecyclerView(scene.getOperationalLayers());
  }

  /**
   * Setup {@link RecyclerView} to display layers
   *
   * @param layers
   */
  private void setupRecyclerView(List<Layer> layers) {
    mLayersAdapter = new LayersAdapter(this);
    mLayersRecyclerView.setAdapter(mLayersAdapter);

    for (Layer layer : layers) {
      // if layer can be shown in legend
      if (layer.canShowInLegend()) {
        layer.addDoneLoadingListener(() -> {
          mLayersAdapter.addLayer(layer);
        });
        layer.loadAsync();
      }
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main_activity, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_show_layer_list) {
      // if bottom sheet is not shown
      if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
        // show bottom sheet
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
      } else {
        // hide bottom sheet
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
      }
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Called when a checkbox on a layer in the list is checked or unchecked
   *
   * @param layer   that has been checked or unchecked
   * @param checked whether the checkbox has been checked or unchecked
   */
  @Override public void layerCheckedChanged(Layer layer, boolean checked) {
    layer.setVisible(checked);
    if (layer instanceof GroupLayer) {
      for (Layer childLayer : ((GroupLayer) layer).getLayers()) {
        childLayer.setVisible(checked);
      }
    }
  }

  @Override
  protected void onPause() {
    mSceneView.pause();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mSceneView.resume();
  }

  @Override
  protected void onDestroy() {
    mSceneView.dispose();
    super.onDestroy();
  }

  /**
   * An adapter that displays {@link Layer}s and their children if they are an instance of {@link GroupLayer}
   */
  private class LayersAdapter extends RecyclerView.Adapter<LayersAdapter.ViewHolder>
      implements OnLayerCheckedChangedListener {

    private static final int VIEW_TYPE_PARENT = 0;
    private static final int VIEW_TYPE_LAYER = 1;

    private List<Layer> mLayers = new ArrayList<>();
    private OnLayerCheckedChangedListener mOnLayerCheckedChangedListener;

    private LayersAdapter(OnLayerCheckedChangedListener onLayerCheckedChangedListener) {
      mOnLayerCheckedChangedListener = onLayerCheckedChangedListener;
    }

    @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
      // inflate the layout for a GroupLayer
      if (getItemViewType(i) == VIEW_TYPE_PARENT) {
        return new ParentViewHolder(
            LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_item_parent, viewGroup, false),
            this);
      } else {
        // inflate the layout for a Layer
        return new LayerViewHolder(
            LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_item_layer, viewGroup, false),
            this);
      }
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
      viewHolder.bind(mLayers.get(i), mLayers.get(i).isVisible());
    }

    @Override public int getItemViewType(int position) {
      if (mLayers.get(position) instanceof GroupLayer) {
        return VIEW_TYPE_PARENT;
      } else {
        return VIEW_TYPE_LAYER;
      }
    }

    @Override public int getItemCount() {
      return mLayers.size();
    }

    /**
     * Add a {@link Layer} to the adapter
     *
     * @param layer
     */
    void addLayer(Layer layer) {
      if (!mLayers.contains(layer)) {
        mLayers.add(layer);
        notifyItemInserted(mLayers.size() - 1);
      }
    }

    /**
     * Called when a checkbox on a layer in the list is checked or unchecked
     *
     * @param layer   that has been checked or unchecked
     * @param checked whether the checkbox has been checked or unchecked
     */
    @Override public void layerCheckedChanged(Layer layer, boolean checked) {
      if (mOnLayerCheckedChangedListener != null) {
        mOnLayerCheckedChangedListener.layerCheckedChanged(layer, checked);
      }
      notifyDataSetChanged();
    }

    /**
     * Subclass of {@link ViewHolder} to display {@link View}s related to {@link GroupLayer}s and their children {@link Layer}s
     */
    class ParentViewHolder extends ViewHolder {

      private CheckBox mParentCheckbox;
      private TextView mParentTextView;
      private final ViewGroup mChildLayout;

      ParentViewHolder(@NonNull View itemView, OnLayerCheckedChangedListener onLayerCheckedChangedListener) {
        super(itemView, onLayerCheckedChangedListener);
        mParentCheckbox = itemView.findViewById(R.id.layerCheckbox);
        mParentTextView = itemView.findViewById(R.id.layerNameTextView);
        mChildLayout = itemView.findViewById(R.id.childLayout);
      }

      @Override void bind(Layer layer, boolean isVisible) {
        mParentCheckbox.setOnCheckedChangeListener(null);
        mParentCheckbox.setChecked(isVisible);
        mParentTextView.setText(layer.getName());

        mParentCheckbox.setOnCheckedChangeListener(
            (buttonView, isChecked) -> mOnLayerCheckedChangedListener.layerCheckedChanged(layer, isChecked));

        // if children can be shown in legend
        if (((GroupLayer) layer).isShowChildrenInLegend()) {
          for (Layer childLayer : ((GroupLayer) layer).getLayers()) {
            // if the layer has not been loaded
            if (childLayer.getLoadStatus() != LoadStatus.LOADED) {
              // add a listener to run when the layer has been loaded
              childLayer.addDoneLoadingListener(() -> {
                addChildLayer(childLayer);
              });
              // load layer
              childLayer.loadAsync();
            } else {
              addChildLayer(childLayer);
            }
          }
        }
      }

      /**
       * Add a {@link View} showing the child {@link Layer} of the {@link GroupLayer}
       *
       * @param childLayer layer to display
       */
      private void addChildLayer(Layer childLayer) {
        View view;

        // try to reuse the View if possible
        if (mChildLayout.findViewWithTag(childLayer) == null) {
          view = LayoutInflater.from(itemView.getContext())
              .inflate(R.layout.adapter_item_layer, mChildLayout, false);
          view.setTag(childLayer);
        } else {
          view = mChildLayout.findViewWithTag(childLayer);
        }

        ((LinearLayout.LayoutParams) view.getLayoutParams()).setMarginStart(
            itemView.getResources().getDimensionPixelSize(R.dimen.adapter_item_child_margin_start));
        ((CheckBox) view.findViewById(R.id.layerCheckbox)).setOnCheckedChangeListener(null);
        ((CheckBox) view.findViewById(R.id.layerCheckbox)).setChecked(childLayer.isVisible());
        ((CheckBox) view.findViewById(R.id.layerCheckbox)).setOnCheckedChangeListener(
            (buttonView, isChecked) -> mOnLayerCheckedChangedListener.layerCheckedChanged(childLayer, isChecked));
        ((TextView) view.findViewById(R.id.layerNameTextView)).setText(childLayer.getName());
        if (mChildLayout.findViewWithTag(childLayer) == null) {
          mChildLayout.addView(view);
        }
      }
    }

    /**
     * Subclass of {@link ViewHolder} to display {@link View}s related to a {@link Layer}
     */
    class LayerViewHolder extends ViewHolder {

      private CheckBox mCheckbox;
      private TextView mTextView;

      LayerViewHolder(@NonNull View itemView, OnLayerCheckedChangedListener onLayerCheckedChangedListener) {
        super(itemView, onLayerCheckedChangedListener);
        mCheckbox = itemView.findViewById(R.id.layerCheckbox);
        mTextView = itemView.findViewById(R.id.layerNameTextView);
      }

      @Override void bind(Layer layer, boolean isVisible) {
        mCheckbox.setOnCheckedChangeListener(null);
        mCheckbox.setChecked(isVisible);
        mTextView.setText(layer.getName());

        mCheckbox.setOnCheckedChangeListener(
            (buttonView, isChecked) -> mOnLayerCheckedChangedListener.layerCheckedChanged(layer, isChecked));
      }
    }

    abstract class ViewHolder extends RecyclerView.ViewHolder {
      OnLayerCheckedChangedListener mOnLayerCheckedChangedListener;

      ViewHolder(@NonNull View itemView, OnLayerCheckedChangedListener onLayerCheckedChangedListener) {
        super(itemView);
        mOnLayerCheckedChangedListener = onLayerCheckedChangedListener;
      }

      abstract void bind(Layer layer, boolean isVisible);
    }
  }
}

interface OnLayerCheckedChangedListener {
  void layerCheckedChanged(Layer layer, boolean checked);
}
