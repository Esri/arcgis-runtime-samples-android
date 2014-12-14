# Geometry Editor

The purpose of this sample is to demonstrate how to create features (point, polyline, polygon) with the ArcGIS for Android API. The sample supports template based editing for the three types of feature layers (point, line and polygon).

Tap the '+' icon in the action bar to start adding a feature. A list of available templates is displayed showing the templates symbol to allow you to quickly select the required feature to add to the map.

When adding a point feature, tap the map to position the feature. Tapping the map again moves the point to the new position.

When adding polygon or polyline features:
* add a new vertex by simply tapping on a new location on the map;
* move an existing vertex by tapping it and then tapping its new location on the map;
* delete an existing vertex by tapping it and then tapping the trash can icon on the action bar.

Additional points are drawn at the midpoint of each line. A midpoint can be moved by tapping the midpoint and then tapping its new location on the map.

In addition to the trash can, the action bar presents the following icons when editing a feature:
* floppy disk icon to Save the feature by uploading it to the server;
* 'X' icon to Discard the feature;
* undo icon to Undo the last action performed (i.e. the last addition, move or deletion of a point).

Whenever a feature is being added, a long-press on the map displays a magnifier that allows a location to be selected more accurately.

## Sample Design

The Activity class sets up the feature layers in its ```onCreate()``` method. It also restores the state of the MapView (i.e. center and resolution) if the activity is being recreated, in which case this state will have been saved by the ```onSaveInstanceState()``` method. Note that the activity’s ```android:configChanges``` attribute is set in the AndroidManifest file such that the activity will not be shut down and restarted due to changes to orientation or keyboard configuration. This gives a good user experience on such configuration changes, because the MapView and layers do not need to be recreated, and it’s acceptable because this particular app doesn’t make extensive use of config-dependent layouts, strings, drawables, dimensions, etc.

The action bar is initially setup in ```onCreateOptionsMenu()``` and option selection is handled in ```onOptionsItemSelected()```. Initially there is only an Add action. This is handled by ```actionAdd()``` which calls ```listTemplates()``` to determine what feature types can be added. All of the layers in the map are inspected and if an instance of an ```ArcGISFeatureLayer``` is found, all its templates are retrieved. By passing a template to the ```featureLayer.createFeatureWithTemplate()``` method, the graphic and its symbol are returned. These are saved, together with the template name.

An instance of ```FeatureTypeDialogFragment``` is then created to display a list of feature types to the user. An instance of the inner class ```FeatureTypeListAdapter``` creates the list items from the template data saved above and an ```OnItemClickListener``` puts the activity into the appropriate ```EditMode``` for creating a feature of the selected type.

Editing actions are performed by clicking on action bar items (see the ```onOptionsItemSelected()``` method) and tapping or long-pressing on the map (see the inner-class ```MyTouchListener```).

During editing, the feature being created is drawn on a GraphicsLayer (```mGraphicsLayerEditing```).

If a feature template of type point is selected, a point is added to the graphics layer when the map is tapped. Additional taps move the point and a full history is maintained by an ArrayList of ```EditingStates``` objects (see below). The feature is saved to the server when the Save is clicked in the action bar.

If a feature template of type polyline or polygon is selected, taps on the map cause points to be drawn via the ```drawVertices()``` method. When there is more than one point, the points are connected using the ```drawPolylineOrPolygon()``` method. The logic for the construction of the polyline and polygon are similar. The only difference is that in one case, the geometry constructed is a multipath polyline versus a multipath polygon. A midpoint is drawn midway between every vertex pair by the ```drawMidPoints()``` method.

For all taps on the map, it is determined if the tap should be a new vertex or the selection of an existing vertex via a proximity test. If an existing point is selected (midpoint or user created vertex), it is highlighted and the next map touch event or long press moves that point rather than adding a new one.

An ```EditingStates``` object contains a snapshot of all of the current points on the map (including which point if any is selected). A list of ```EditingStates``` objects is maintained, one for each edit operation, and this is used to perform the undo operation.

To save the feature to the server, the ```applyEdits()``` method is called on the appropriate feature layer. This happens only when the Save action is selected, not when individual points are being created and edited.