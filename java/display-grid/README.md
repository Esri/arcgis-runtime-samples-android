# Display grid

Display and work with coordinate system grids such as Latitude/Longitude, MGRS, UTM and USNG on a map view. 
This includes toggling labels visibility.

## How to use the sample

Tap on the `Change Grid` button in the toolbar to open the settings view. 
You can select type of grid from `Grid Type` (LatLong, MGRS, UTM and USNG) 
and modify it's properties likelabel visibility.

![Display Grid App](display-grid.png)

## How it works

`AGSMapView` has a property called `grid` of type `AGSGrid` and is initially set to use the LatitudeLongitude grid. 
The controls allow to hide the labels of the grid with the following properties/methods:
- `labelVisibility` : Specifies whether the grid's text labels are visible or not