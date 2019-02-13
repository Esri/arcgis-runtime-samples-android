# Min Max Scale

View an integrated mesh layer from a scene service.

![Integrated Mesh Layer](integrated-mesh-layer.png)

## Use case

An integrated mesh is used to generate a mesh over a point cloud data set, which may be derived from a detailed LiDAR survey. LiDAR data is often of a high enough resolution to capture 3D features as small as walls and trees, but also the irregular surfaces of geological features like cliffs. An integrated mesh made from a point cloud of LiDAR data will include realistic textures and elevation information.

## How it works

1. Create an ArcGIS map.
2. Set min and max scales of map, ```ArcGISMap.setMaxScale()``` and ```ArcGISMap.setMinScale()```.
    <li>Create an ArcGIS map.  </li>
    <li>Set min and max scales of map, <code>ArcGISMap.setMaxScale()</code> and <code>ArcGISMap.setMinScale()</code>.</li>
    <li>Set the ArcGIS map to the <code>MapView</code>.</li>
</ol>

## Relevant API

- IntegratedMeshLayer

#### Tags

Layers
3D
integrated mesh