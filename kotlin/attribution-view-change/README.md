# Attribution View Change

This sample demonstrates showing UI components responding to attribution view.

![Attribution View Change App](attribution-view-change.png)

## Features

* ArcGISMap
* Basemap

## Developer Pattern

Set the bottom margin of FAB to respect the height of the attribution bar.

```kotlin
 // set attribution bar listener
 val params = fab.layoutParams as CoordinatorLayout.LayoutParams
 mapView.addAttributionViewLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
     val heightDelta = bottom - oldBottom
     params.bottomMargin += heightDelta
     Toast.makeText(this, "new bounds [" + left + "," + top + "," + right + "," + bottom + "]" +
             " old bounds [" + oldLeft + "," + oldTop + "," + oldRight + "," + oldBottom + "]", Toast.LENGTH_SHORT).show()
        }
```
