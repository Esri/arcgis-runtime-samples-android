# Find service area (interactive)

Find the service area within a network from a given point.

![Find service area App](find-service-area-interactive.png)

## Use case

A service area shows locations that can be reached from a facility based off a certain impedance, such as travel time or distance. Barriers can increase impedance by either adding to the time it takes to pass through the barrier or by altogether preventing passage.

You might calculate the region around a hospital in which ambulances can service in 30 min or less.

## How to use the sample

In order to find any service areas at least one facility needs to be added to the map view.
* To add a facility, click the facility button, then click anywhere on the map.
* To add a barrier, click the barrier button, and click multiple locations on map. Hit the barrier button again to finish drawing barrier. Hitting any other button will also stop the barrier from drawing.
* To show service areas around facilities that were added, click show service areas button. 
* The reset button clears all graphics and resets the service area task.
   
## How it works

1. Create a new `ServiceAreaTask` from a network service.
2. Create default `ServiceAreaParameters` from the service area task.
3. Set the parameters to return polygons (true) to return all service
   areas.
4. Add a `ServiceAreaFacility` to the parameters.
5. Get the `ServiceAreaResult` by solving the service area task using
   the parameters.
6. Get any `ServiceAreaPolygons` that were returned,
   serviceAreaResult.getResultPolygons(facilityIndex). 
7. Display the service area polygons as graphics in a `GraphicsOverlay`
   on the `MapView`.

## Relevant API

* PolylineBarrier
* ServiceAreaFacility
* ServiceAreaParameters
* ServiceAreaPolygon
* ServiceAreaResult
* ServiceAreaTask

## Tags
Routing and Logistics
facilities
barriers
impedance
