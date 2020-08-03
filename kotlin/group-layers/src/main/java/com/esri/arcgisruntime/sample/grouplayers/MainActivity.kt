package com.esri.arcgisruntime.sample.grouplayers

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.esri.arcgisruntime.layers.GroupLayer
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.LayerList
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.view.*

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val portal = Portal("https://www.arcgis.com/")
    val portalItem = PortalItem(portal, "74ec7d6ca482442ba24f80b708aec67e")

    val scene = ArcGISScene(portalItem)

    sceneView.scene = scene

    scene.operationalLayers.forEach { layer -> layer.addDoneLoadingListener { layer.isVisible = true } }

    scene.addDoneLoadingListener {
      setupBottomSheet(scene.operationalLayers)
    }
  }

  private fun onLayerCheckedChanged(layer: Layer, isChecked: Boolean) {
    Toast.makeText(this, "${layer.name} is now $isChecked", Toast.LENGTH_LONG).show()
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

      recyclerView.adapter = LayerListAdapter(layers) { layer: Layer, isChecked: Boolean -> onLayerCheckedChanged(layer, isChecked)}
      recyclerView.layoutManager = LinearLayoutManager(applicationContext)
      // rotate the arrow so it starts off in the correct rotation
      header.imageView.rotation = 180f
    }

    // shrink the scene view so it is not hidden under the bottom sheet header
    (sceneView.layoutParams as CoordinatorLayout.LayoutParams).bottomMargin =
      bottomSheet.header.height
  }
}

