# View Layer Status
The sample demonstrates how to view the status of the layers on the map. This is obtained from the enum value of ```LayerViewStatus```. A signal handler is set up on the map to handle the ```LayerViewStateChangedEvent``` signal, and the status text is updated when the status changes.

![screen shot 2016-05-31 at 4 23 20 pm](https://cloud.githubusercontent.com/assets/12448081/15693690/1f10dc52-274c-11e6-92db-fbe1689f856d.png)
#Features

* Map
* MapView
* LayerViewStateChangedListener
* ArcGISTiledLayer
* ArcGISMapImageLayer
* ServiceFeatureTable
* FeatureLayer

# Developer Pattern

```java
mMapView.addLayerViewStateChangedListener(new LayerViewStateChangedListener() {
            @Override
            public void layerViewStateChanged(LayerViewStateChangedEvent layerViewStateChangedEvent) {

                // get the layer which changed it's state
                Layer layer = layerViewStateChangedEvent.getLayer();

                // get the View Status of the layer
                // View status will be either of ACTIVE, ERROR, LOADING, NOT_VISIBLE, OUT_OF_SCALE, UNKNOWN
                String viewStatus = layerViewStateChangedEvent.getLayerViewStatus().iterator().next().toString();

                final int layerIndex = mMap.getOperationalLayers().indexOf(layer);

                // finding and updating status of the layer
                switch (layerIndex) {
                    case TILED_LAYER:
                        timeZoneTextView.setText(viewStatusString(viewStatus));
                        break;
                    case IMAGE_LAYER:
                        worldCensusTextView.setText(viewStatusString(viewStatus));
                        break;
                    case FEATURE_LAYER:
                        recreationTextView.setText(viewStatusString(viewStatus));
                        break;
                }

            }
        });
```
