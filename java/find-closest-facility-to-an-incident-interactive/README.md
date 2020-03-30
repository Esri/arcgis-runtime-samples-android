# Find closest facility to an incident (interactive)

Find a route to the closest facility from a location.

![Image of find closest facility to an incident interactive](find-closest-facility-to-an-incident-interactive.png)

## Use case

Quickly and accurately determining the most efficient route between a location and a facility is a frequently encountered task. For example, a paramedic may need to know which hospital in the vicinity offers the possibility of getting an ambulance patient critical medical care in the shortest amount of time. Solving for the closest hospital to the ambulance's location using an impedance of "travel time" would provide this information.

## How to use the sample

Tap near any of the hospitals and a route will be displayed from that tapped location to the nearest hospital.

## How it works

1.  Create a `ClosestFacilityTask` using a Url from an online network analysis service.
2.  Get `ClosestFacilityParameters` from task, `task.createDefaultParametersAsync().get()`.
3.  Add the list of facilities to parameters, `closestFacilityParameters.setFacilities(facilitiesList)`.
4.  Add the incident to parameters, `closestFacilityParameters.setIncidents(Collections.singletonList(new Incident(incidentPoint)))`.
5.  Get `ClosestFacilityResult` from solving task with parameters, `task.solveClosestFacilityAsync(facilityParameters).get()`.
6.  Get index list of closet facilities to incident, `facilityResult.getRankedFacilitiesIndexes(0)`.
7.  Get index of closest facility, `rankedFacilitiesList.get(0)`.
8.  Find closest facility route, `facilityResult.getRoute(closestFacilityIndex, IncidentIndex)`.
9.  Display route to `MapView`:
    *   Create `Graphic` from route geometry, `new Graphic(route.getRouteGeometry(), routeSymbol)`.
    *   Add graphic to `GraphicsOverlay` which is attached to the mapview.

## Relevant API

*   ClosestFacilityParameters
*   ClosestFacilityResult
*   ClosestFacilityRoute
*   ClosestFacilityTask
*   Facility
*   Graphic
*   GraphicsOverlay
*   Incident
*   MapView

## Tags

incident, network analysis, route, search