# Web Tiled Layer

![Web Tiled Layer](webtiledlayer.png)

This sample demonstrates how to display map tiles from an online resource using the WebTiledLayer.

## Features
* ArcGISMap
* Basemap
* MapView
* WebTiledLayer

## Developer Pattern
`WebTiledLayer` provides a simple way to integrate non-ArcGIS Services as a layer in a map. In this case, map tiles from Stamen are added to the map. The template URL is specified by setting the subDomains, level, col, and row attributes. Additionally, copyright information is added to the layer so that the layer can be properly attributed. The layer is added to a `Basemap`, and a `Basemap` is added to a `ArcGISMap`. Finally, the Map is set on the `MapView`, and the tiled layer is displayed.

```java
// list of subdomains
List<String> subDomains = Arrays.asList("a", "b", "c", "d");
// url pattern
String templateUri = "http://{subDomain}.tile.stamen.com/terrain/{level}/{col}/{row}.png";

// webtile layer
final WebTiledLayer webTiledLayer = new WebTiledLayer(templateUri, subDomains);
webTiledLayer.loadAsync();
webTiledLayer.addDoneLoadingListener(new Runnable() {
    @Override
    public void run() {
        if(webTiledLayer.getLoadStatus() == LoadStatus.LOADED){
            // use webtile layer as Basemap
            ArcGISMap map = new ArcGISMap(new Basemap(webTiledLayer));
            mMapView.setMap(map);
            // custom attributes
            webTiledLayer.setAttribution("Map tiles by <a href=\"http://stamen.com/\">Stamen Design</a>, " +
                    "under <a href=\"http://creativecommons.org/licenses/by/3.0\">CC BY 3.0</a>. " +
                    "Data by <a href=\"http://openstreetmap.org/\">OpenStreetMap</a>, " +
                    "under <a href=\"http://creativecommons.org/licenses/by-sa/3.0\">CC BY SA</a>.");
        }
    }
});
```