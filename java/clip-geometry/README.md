# Clip geometry

Clip a geometry with another geometry.

![Image of clip geometry](clip-geometry.png)

## Use case

Create a new set of geometries for analysis (e.g. displaying buffer zones around abandoned coal mine shafts in an area of planned urban development) by clipping intersecting geometries.

## How to use the sample

Tap the "Clip" button to clip the blue graphic with the red dashed envelopes.

## How it works

1.  Use the static method `GeometryEngine.clip()` to generate a clipped `Geometry`, passing in an existing `Geometry` and an `Envelope` as parameters.  The existing geometry will be clipped where it intersects an envelope.
2.  Create a new `Graphic` from the clipped geometry and add it to a `GraphicsOverlay` on the `MapView`.

## Relevant API

* Envelope
* Geometry
* GeometryEngine.clip
* Graphic
* GraphicsOverlay

## Additional information

Note: the resulting geometry may be null if the envelope does not intersect the geometry being clipped.

## Tags

analysis, clip, geometry
