package com.esri.arcgisruntime.sample.grouplayers

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.ArcGISSceneLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.GroupLayer
import com.esri.arcgisruntime.layers.GroupVisibilityMode
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.LayerList
import com.esri.arcgisruntime.mapping.view.Camera
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.view.*


class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a scene with an imagery basemap
    val scene = ArcGISScene(Basemap.createImagery())
    sceneView.scene = scene

    // create different types of layers
    val trees =
      ArcGISSceneLayer("https://tiles.arcgis.com/tiles/P3ePLMYs2RVChkJx/arcgis/rest/services/DevA_Trees/SceneServer/layers/0").apply {
        name = "Trees"
      }
    val pathways =
      FeatureLayer(ServiceFeatureTable("https://services.arcgis.com/P3ePLMYs2RVChkJx/arcgis/rest/services/DevA_Pathways/FeatureServer/1")).apply {
        name = "Pathways"
      }
    val projectArea =
      FeatureLayer(ServiceFeatureTable("https://services.arcgis.com/P3ePLMYs2RVChkJx/arcgis/rest/services/DevelopmentProjectArea/FeatureServer/0")).apply {
        name = "Project area"
        // set the scene's viewpoint based on this layer's extent
        addDoneLoadingListener {
          sceneView.setViewpointCamera(
            Camera(
              fullExtent.center,
              700.0,
              0.0,
              60.0,
              0.0
            )
          )
        }
      }
    val buildingsA =
      ArcGISSceneLayer("https://tiles.arcgis.com/tiles/P3ePLMYs2RVChkJx/arcgis/rest/services/DevA_BuildingShells/SceneServer/layers/0").apply {
        name = "Buildings A"
      }
    val buildingsB =
      ArcGISSceneLayer("https://tiles.arcgis.com/tiles/P3ePLMYs2RVChkJx/arcgis/rest/services/DevB_BuildingShells/SceneServer/layers/0").apply {
        name = "Buildings B"
      }

    // create a group layer from scratch by adding the trees, pathways, and project area as children
    val projectAreaGroupLayer = GroupLayer().apply {
      name = "Project area group"
      layers.addAll(arrayOf(trees, pathways, projectArea))
    }
    // create a group layer for the buildings and set its visibility mode to exclusive
    val buildingsGroupLayer = GroupLayer().apply {
      name = "Buildings group"
      layers.addAll(arrayOf(buildingsA, buildingsB))
      visibilityMode = GroupVisibilityMode.EXCLUSIVE
    }

    // add the group layer and other layers to the scene as operational layers
    scene.apply {
      operationalLayers.addAll(arrayOf(projectAreaGroupLayer, buildingsGroupLayer))
      addDoneLoadingListener {
        setupBottomSheet(scene.operationalLayers)
      }
    }
  }

  private fun onLayerCheckedChanged(layer: Layer, isChecked: Boolean) {
    layer.isVisible = isChecked
  }

  /** Creates a bottom sheet to display a list of group layers.
   *
   * @param layers a list of layers and group layers to be displayed on the scene
   */
  private fun setupBottomSheet(layers: LayerList) {
    // create a bottom sheet behavior from the bottom sheet view in the main layout
    val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
      // expand the bottom sheet, and ensure it is displayed on the screen when collapsed
      state = BottomSheetBehavior.STATE_EXPANDED
      peekHeight = bottomSheet.header.height
      // animate the arrow when the bottom sheet slides
      addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
          bottomSheet.header.imageView.rotation = slideOffset * 180f
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
          bottomSheet.header.imageView.rotation = when (newState) {
            BottomSheetBehavior.STATE_EXPANDED -> 180f
            else -> bottomSheet.header.imageView.rotation
          }
        }
      })
    }

    bottomSheet.apply {
      visibility = View.VISIBLE
      // expand or collapse the bottom sheet when the header is clicked
      header.setOnClickListener {
        bottomSheetBehavior.state = when (bottomSheetBehavior.state) {
          BottomSheetBehavior.STATE_COLLAPSED -> BottomSheetBehavior.STATE_EXPANDED
          else -> BottomSheetBehavior.STATE_COLLAPSED
        }
      }

      // initialize the recycler view with the group layers and set the callback for the checkboxes
      recyclerView.adapter = LayerListAdapter(layers) { layer: Layer, isChecked: Boolean ->
        onLayerCheckedChanged(
          layer,
          isChecked
        )
      }
      recyclerView.layoutManager = LinearLayoutManager(applicationContext)
      // rotate the arrow so it starts off in the correct rotation
      header.imageView.rotation = 180f
    }

    // shrink the scene view so it is not hidden under the bottom sheet header when collapsed
    (sceneView.layoutParams as CoordinatorLayout.LayoutParams).bottomMargin =
      bottomSheet.header.height
  }

  override fun onResume() {
    super.onResume()
    sceneView.resume()
  }

  override fun onPause() {
    sceneView.pause()
    super.onPause()
  }

  override fun onDestroy() {
    sceneView.dispose()
    super.onDestroy()
  }
}

