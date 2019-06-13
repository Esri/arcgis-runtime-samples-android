# Find closest facility to an incident (interactive)

Find a route to the closest facility from a location.

![Find closest facility to an incident interactive App](find-closest-facility-to-an-incident-interactive.png)

## Use case

Quickly and accurately determining the most efficient route between a location and a facility is a frequently encountered task. For example, a paramedic may need to know which hospital in the vicinity offers the possibility of getting an ambulance patient critical medical care in the shortest amount of time. Solving for the closest hospital to the ambulance's location using an impedance of "travel time" would provide this information.

## How to use the sample

Click near any of the hospitals and a route will be displayed from that clicked location to the nearest hospital.

## How it works

1.  Create a new `ClosestFacilityTask` using a Url from an online network analysis service.
1.  Get `ClosestFacilityParameters` from the task.
1.  Add facilities to the parameters.
1.  Add an incident (as a `Point`) to the parameters.
1.  Get `ClosestFacilityResult` by solving the task with the parameters.
1.  Get the indexed list of closet facilities to the incident.
1.  Get the index of the closest facility.
1.  Get closest facility route from the facility result.
1.  Display the route on the `MapView` as a `Graphic` on a `GraphicsOverlay`.

## Relevant API

*   ClosestFacilityParameters
*   ClosestFacilityResult
*   ClosestFacilityRoute
*   ClosestFacilityTask
*   Facility
*   Graphic
*   GraphicsOverlay
*   Incident

## Tags
Routing & Logistics
incident
network analysis
route
search
