/* Copyright 2015 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgisruntime.sample.mapsketching;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutionException;

/**
 * Wraps a GraphicsOverlay with additional logic for sketching points, lines, and polygons onto
 * the GraphicsOverlay. Also supports undoing/redoing of sketching actions, as well as clearing
 * entire GraphicsOverlay of all current sketches.
 */
public class SketchGraphicsOverlay {

  private final MapView mMapView;
  private final GraphicsOverlay mGraphicsOverlay;
  private final List<Graphic> mGraphics;
  // Symbols used when drawing new points/lines/polygons
  private final SimpleMarkerSymbol mPointPlacementSymbol;
  private final SimpleMarkerSymbol mPointPlacedSymbol;
  private final SimpleMarkerSymbol mPolylineVertexSymbol;
  private final SimpleLineSymbol mPolylinePlacementSymbol;
  private final SimpleLineSymbol mPolylinePlacedSymbol;
  private final SimpleMarkerSymbol mPolylineMidpointSymbol;
  private final SimpleFillSymbol mPolygonFillSymbol;
  // Listener is used to notify when undo/redo/clear buttons can be enabled and
  // when a drawing is finished
  private SketchGraphicsOverlayEventListener mListener;
  // Keep a reference to the current point, line, and/or polygon because drawn
  private Graphic mCurrentPoint;
  private Graphic mCurrentLine;
  private Graphic mCurrentPolygon;
  // Keep a reference to the current point collection for polyline/polygon to update geometry
  private PointCollection mCurrentPointCollection;
  // Current drawing mode
  private DrawingMode mDrawingMode = DrawingMode.NONE;
  // The first point of a polyline uses special logic so keep track of when it's started
  private boolean mIsPolylineStarted = false;
  private boolean mIsMidpointSelected = false;
  // Stack of actions to be undone
  private Stack<UndoRedoItem> mUndoElementStack = new Stack<>();
  // stack of actions to be redone
  private Stack<UndoRedoItem> mRedoElementStack = new Stack<>();

  /**
   * Instantiates a SketchGraphicsOverlay with the specified MapView (to which the overlay is
   * added) and SketchGraphicsOverlayEventListener (used to notify the main activity when undo/
   * redo/clear buttons should be enabled/disabled and when a drawing is finished.
   *
   * @param mapView the MapView to which the overlay should be added
   * @param listener the listener to notify upon state changes
   */
  public SketchGraphicsOverlay(MapView mapView, SketchGraphicsOverlayEventListener listener) {
    mMapView = mapView;
    mListener = listener;
    mGraphicsOverlay = new GraphicsOverlay();
    // Add a graphics overlay and get our list of graphics for modification
    mMapView.getGraphicsOverlays().add(mGraphicsOverlay);
    mGraphics = mGraphicsOverlay.getGraphics();
    // Set a drawing touch listener for sketching
    mMapView.setOnTouchListener(new DrawingMapViewOnTouchListener(mMapView.getContext(), mMapView));

    // Outline symbols for outlining the main symbols
    SimpleLineSymbol blackOutline =
            new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.rgb(0, 0, 0), 1);
    SimpleLineSymbol whiteOutline =
            new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.rgb(255, 255, 255), 1);

    // Create all the different symbols
    // When placing a point, it will be a red circle with black outline
    mPointPlacementSymbol =
            new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 7);
    mPointPlacementSymbol.setOutline(blackOutline);
    // A placed point (single point) will be a blue circle with black outline
    mPointPlacedSymbol =
            new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.BLUE, 7);
    mPointPlacedSymbol.setOutline(blackOutline);
    // A placed vertex of a polyline will be a blue square with a white outline
    mPolylineVertexSymbol =
            new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.SQUARE, Color.BLUE, 5);
    mPolylineVertexSymbol.setOutline(whiteOutline);
    // While placing a polyline, the line will be red
    mPolylinePlacementSymbol =
            new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 1);
    // Once placed, a polyline will become blue
    mPolylinePlacedSymbol =
            new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 1);
    // A midpoint of a polyline segment will be a semi-transparent white circle with black outline
    mPolylineMidpointSymbol =
            new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.WHITE, 5);
    mPolylineMidpointSymbol.setOutline(blackOutline);
    // Polygons will be filled with a semi-transparent black solid shade
    mPolygonFillSymbol =
            new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.BLACK, null);
  }

  /**
   * Sets the current drawing mode of the SketchGraphicsOverlay.
   *
   * @param drawingMode the drawing mode to set
   */
  public void setDrawingMode(DrawingMode drawingMode) {
    // If we try to start a new drawing before finishing our last, finish the current one
    if (mDrawingMode != DrawingMode.NONE) {
      finishDrawing();
    }
    mDrawingMode = drawingMode;
    // If the drawing mode is polyline or polygon, set the current point collection to an empty collection
    if (mDrawingMode == DrawingMode.POLYLINE || mDrawingMode == DrawingMode.POLYGON) {
      mCurrentPointCollection = new PointCollection(mMapView.getSpatialReference());
    }
  }

  /**
   * Convenience method for queueing an undo or a redo event. In addition to queueing the
   * event, it will also notify the listener to enable the undo or redo button if the stack
   * was previously empty.
   *
   * @param stack the stack to which the event should be added
   * @param item the UndoRedoItem to queue
   */
  private void queueUndoRedoItem(Stack<UndoRedoItem> stack, UndoRedoItem item) {
    // If the stack is currently empty, we should notify the listener to enable to button
    if(stack.isEmpty()) {
      // If it's the undo stack, fire the undo state changed listener
      if(stack == mUndoElementStack) {
        mListener.onUndoStateChanged(true);
      // Otherwise fire the redo state changed listener
      } else {
        mListener.onRedoStateChanged(true);
      }
    }
    // Finally, push the item to the stack
    stack.push(item);
  }

  /**
   * Undo the last event that took place.
   */
  public void undo() {
    // Handle an undo event, popping an event from the undo stack and pushing a new event to the redo stack
    handleUndoRedoEvent(mUndoElementStack, mRedoElementStack);
  }

  /**
   * Redo the action previously undone with a call to undo().
   */
  public void redo() {
    // Handle an redo event, popping an event from the redo stack and pushing a new event to the undo stack
    handleUndoRedoEvent(mRedoElementStack, mUndoElementStack);
  }

  /**
   * Convenience method for clearing the undo or redo event stack. Additionally notifies
   * the listener to disable the corresponding button.
   *
   * @param stack the stack to clear
   */
  private void clearStack(Stack<UndoRedoItem> stack) {
    stack.clear();
    // Notify the listener based on which stack was cleared
    if (stack == mUndoElementStack) {
      mListener.onUndoStateChanged(false);
    } else {
      mListener.onRedoStateChanged(false);
    }
  }

  /**
   * This method handles performing an undo or redo event. An event will be popped from the specified
   * stack and an opposite event type (to undo/redo that) will be pushed into the other stack.
   *
   * @param from the stack from which to pop an event
   * @param to   the stack in which to push the opposing event
   */
  @SuppressWarnings("unchecked")
  private void handleUndoRedoEvent(Stack<UndoRedoItem> from, Stack<UndoRedoItem> to) {
    // index is used in a couple places so define it here
    int index, pointIndex;
    List<Graphic> graphics;
    if (!from.isEmpty()) {
      UndoRedoItem item = from.pop();
      // If this was the last event in the stock, notify the listener to disable the corresponding button
      if (from.isEmpty()) {
        if (from == mUndoElementStack) {
          // disable to selected drawing mode
          mListener.onDrawingFinished();
          mListener.onUndoStateChanged(false);
        } else {
          mListener.onRedoStateChanged(false);
        }
      }
      // Check whether the graphics list was empty before we process the event
      boolean graphicsWasEmpty = mGraphics.isEmpty();
      switch (item.getEvent()) {
        // If the event was adding a graphic, then the action taken here is to remove the graphic
        case ADD_POINT:
          // Get the graphic[s] previously added and remove them from the graphics list
          graphics = (List<Graphic>) item.getElement();
          mGraphics.removeAll(graphics);
          // Queue a new event indicating that we've removed the graphic[s]
          queueUndoRedoItem(to, new UndoRedoItem(UndoRedoItem.Event.REMOVE_POINT, graphics));
          mIsMidpointSelected = false;
          mIsPolylineStarted = false;
          mCurrentPoint = null;
          mCurrentPointCollection = new PointCollection(mMapView.getSpatialReference());
          break;
        // If the event was removing a graphic, then the action taken here is to add it back
        case REMOVE_POINT:
          // Readd the graphic[s] previously removed.
          graphics = (List<Graphic>) item.getElement();
          mGraphics.addAll(graphics);
          // Queue a new event indicating that we've added the graphic[s]
          queueUndoRedoItem(to, new UndoRedoItem(UndoRedoItem.Event.ADD_POINT, graphics));
          break;
        // If the event was adding a polyline point, the action taken here is to remove the last point added
        case ADD_POLYLINE_POINT:
          // Get the index of the current point (which will be the one most recently added)
          pointIndex = (mDrawingMode == DrawingMode.POLYGON) ?
                  mCurrentPointCollection.size() - 2 : mCurrentPointCollection.size() - 1;
          // Remove it from the point collection and update the current line (and polygon if applicable)
          Point p = mCurrentPointCollection.remove(pointIndex);
          mCurrentLine.setGeometry(new Polyline(mCurrentPointCollection));
          if (mDrawingMode == DrawingMode.POLYGON) {
            mCurrentPolygon.setGeometry(new Polygon(mCurrentPointCollection));
          }
          // Undoing an add point always removes the final point
          index = mGraphics.size() - 1;
          // Remove the point, and remove the midpoint before it
          mGraphics.remove(index--);
          mGraphics.remove(index--);
          // If we're drawing a polygon, we also need to update the final midpoint position
          if (mDrawingMode == DrawingMode.POLYGON) {
            updatePolygonMidpoint();
            // If we are down to only 1 point (size will be 2 because 1st and final point are duplicates)
            // Then we want to remove the final midpoint
            if (mCurrentPointCollection.size() == 2) {
              mGraphics.remove(index--);
              mCurrentPoint = mGraphics.get(index);
            } else {
              // Otherwise just set the point before the final midpoint as current point
              mCurrentPoint = mGraphics.get(index - 1);
            }
          } else {
            // If we're drawing a polyline then the current point will be the final point (which is where
            // index will now be pointing)
            mCurrentPoint = mGraphics.get(index);
          }
          // Change the symbol to the placement symbol
          mCurrentPoint.setSymbol(mPointPlacementSymbol);
          // Queue a new event indicating that we've removed a polyline point
          queueUndoRedoItem(to, new UndoRedoItem(UndoRedoItem.Event.REMOVE_POLYLINE_POINT, p));
          break;
        // If the event was moving a polyline point, the action taken here is to move it back
        case MOVE_POLYLINE_POINT:
          // Get the corresponding MovePolylinePointElement
          UndoRedoItem.MovePolylinePointElement element = (UndoRedoItem.MovePolylinePointElement) item.getElement();
          // Queue a new event indicating a polyline point move with the necessary information
          queueUndoRedoItem(to, new UndoRedoItem(UndoRedoItem.Event.MOVE_POLYLINE_POINT,
                  new UndoRedoItem.MovePolylinePointElement(mCurrentPoint, (Point)mCurrentPoint.getGeometry(), element.isMidpoint())));
          // Get the old Graphic of the point that was moved
          Graphic oldGraphic = element.getGraphic();
          // Get the previous point position
          Point oldPoint = element.getPoint();
          // Find the index of the moved point. Since we have complete control over how we're adding the undo elements,
          // we can safely assume here that oldGraphic.getGeometry() is a Point. However, proper practice (here and other
          // places) would be to check that the geometry is an instanceof Point before casting.
          pointIndex = mCurrentPointCollection.indexOf(oldGraphic.getGeometry());
          // Find the index of the moved graphic
          index = mGraphics.indexOf(oldGraphic);
          // Set the current working point's symbol to a placed vertex symbol before switching
          mCurrentPoint.setSymbol(mPolylineVertexSymbol);
          // Change our current working point to the old moved graphic
          mCurrentPoint = mGraphics.get(index);
          // Set it's symbol to the placement symbol
          mCurrentPoint.setSymbol(mPointPlacementSymbol);
          // If the element is/was a midpoint, we need to handle adding/removing surrounding midpoints
          if (element.isMidpoint()) {
            Point newGeometry = oldPoint;
            // If this is an undo
            if (from == mUndoElementStack) {
              // Go back to having a midpoint selected
              mIsMidpointSelected = true;
              // Remove the current point from the point collection (since it's going back to being a midpoint)
              mCurrentPointCollection.remove(pointIndex);
              // Remove the midpoint before this point. Since this shifts the index, the index will now be
              // for the midpoint after our point
              mGraphics.remove(index - 1);
              // So remove that index and then decrement to get the index back at our graphic
              mGraphics.remove(index--);
              // Our point will now be a midpoint so get the midpoint between the points before and after it and set it
              Point endPoint = (mDrawingMode == DrawingMode.POLYGON && index == mGraphics.size() - 1) ?
                      mCurrentPointCollection.get(mCurrentPointCollection.size() - 1) : (Point) mGraphics.get(index + 1).getGeometry();
              newGeometry = getMidpoint((Point) mGraphics.get(index - 1).getGeometry(), endPoint);
            } else {
              // If it's a redo, then we need to make a new vertex point and add new midpoints before and after it
              splitMidpoint(newGeometry);
            }
            // Finally set the current point's position
            mCurrentPoint.setGeometry(newGeometry);
          } else {
            // If it wasn't a midpoint, then change the point's position within the point collection and update the
            // graphic's geometry
            mCurrentPointCollection.set(pointIndex, oldPoint);
            mCurrentPoint.setGeometry(oldPoint);
            // If this isn't the first point, adjust the midpoint's position before it
            if (pointIndex != 0) {
              Point preMidpoint = getMidpoint(mCurrentPointCollection.get(pointIndex - 1), oldPoint);
              mGraphics.get(index - 1).setGeometry(preMidpoint);
            }
            // If this isn't the last point, adjust the midpoints position after it
            if (pointIndex != mCurrentPointCollection.size() - 1) {
              Point postMidpoint = getMidpoint(oldPoint, mCurrentPointCollection.get(pointIndex + 1));
              mGraphics.get(index + 1).setGeometry(postMidpoint);
            }
          }
          if (mDrawingMode == DrawingMode.POLYGON) {
            // If we're moving the first point of a polygon, we need to replicate that change
            // in the final point as well and update the final midpoint
            if (pointIndex == 0) {
              mCurrentPointCollection.set(mCurrentPointCollection.size() - 1, oldPoint);
              updatePolygonMidpoint();
            }
            // In either case, update the polygon's geometry
            mCurrentPolygon.setGeometry(new Polygon(mCurrentPointCollection));
          }
          // Update the line's geometry
          mCurrentLine.setGeometry(new Polyline(mCurrentPointCollection));
          break;
        // If the event was removing a polyline point, the action taken here is to add it back
        case REMOVE_POLYLINE_POINT:
          // Get the point that was removed, and add it back to the point collection
          Point point = (Point) item.getElement();
          if (mDrawingMode == DrawingMode.POLYGON) {
            // If adding back to a polygon, remove the final midpoint so it can be readded
            if (mCurrentPointCollection.size() > 2) {
              mGraphics.remove(mGraphics.size() - 1);
            }
            // Add it at the second to last position
            mCurrentPointCollection.add(mCurrentPointCollection.size() - 1, point);
          } else {
            // If just a line, add it in the final position
            mCurrentPointCollection.add(point);
          }
          addPolylinePoint(point);
          // Queue a new event indicating that we've added a polyline point
          to.add(new UndoRedoItem(UndoRedoItem.Event.ADD_POLYLINE_POINT, null));
          break;
        // If the event was finishing a polyline, the action taken here is to remove the whole polyline
        case ADD_POLYLINE:
          // Create a new graphics list and add to it all the pieces of the polyline, so we can add it back with a redo
          graphics = new ArrayList<>();
          index = mGraphics.size() - 1;
          // Add all of the points of the polyline
          while (index > 0 && !(mGraphics.get(index).getGeometry() instanceof Polyline)) {
            graphics.add(0, mGraphics.remove(index--));
          }
          // Add the polyline itself
          graphics.add(0, mGraphics.remove(index--));
          // If removing a polygon, also add the polygon
          if (index > -1 && mGraphics.get(index).getGeometry() instanceof Polygon) {
            graphics.add(0, mGraphics.remove(index));
          }
          // Queue a new event indicating that we've removed a polyline
          queueUndoRedoItem(to, new UndoRedoItem(UndoRedoItem.Event.REMOVE_POLYLINE, graphics));
          break;
        // If the event was removing a polyline, the action taken here is to add it back
        case REMOVE_POLYLINE:
          // Get the graphics that were previously removed
          graphics = (List<Graphic>) item.getElement();
          // Add them all to the list of graphics
          mGraphics.addAll(graphics);
          // Queue a new event indicating that we've added a polyline
          queueUndoRedoItem(to, new UndoRedoItem(UndoRedoItem.Event.ADD_POLYLINE, null));
          break;
        // If the event was moving a point, the action taken here is to move it back
        case MOVE_POINT:
          if (mCurrentPoint != null) {
            // Queue a new event indicating that we moved the point, with its current geometry before we change it
            queueUndoRedoItem(to, new UndoRedoItem(UndoRedoItem.Event.MOVE_POINT, mCurrentPoint.getGeometry()));
            // Set the geometry back
            mCurrentPoint.setGeometry((Geometry) item.getElement());
          }
          break;
        // If the event was erasing all graphics, the action taken here is to put them all back
        case ERASE_GRAPHICS:
          // Add all the graphics back
          mGraphics.addAll((List<Graphic>) item.getElement());
          // Queue a new event indicating that we've replaced all the graphics
          queueUndoRedoItem(to, new UndoRedoItem(UndoRedoItem.Event.REPLACE_GRAPHICS, null));
          break;
        // If the event was replacing all the graphics, the action taken here is to clear them all
        case REPLACE_GRAPHICS:
          // Queue a new event indicating that we've erased the graphics
          queueUndoRedoItem(to, new UndoRedoItem(UndoRedoItem.Event.ERASE_GRAPHICS, copyGraphics()));
          // Erase all graphics
          mGraphics.clear();
          break;
      }
      boolean graphicsIsEmpty = mGraphics.isEmpty();
      // If the graphic list was previously empty and now it's not, notify the listener to enable
      // the clear button
      if(graphicsWasEmpty && !graphicsIsEmpty) {
        mListener.onClearStateChanged(true);
      // If previously non empty and now it is, notify the listener to disable the clear button
      } else if (!graphicsWasEmpty && graphicsIsEmpty) {
        mListener.onDrawingFinished();
        mListener.onClearStateChanged(false);
      }
    }
  }

  /**
   * Clear all of the graphics on the SketchGraphicsOverlay and reset the current drawing state.
   */
  public void clear() {
    // Before clearing, finish any drawing that may currently be in progress
    finishDrawing();
    if (!mGraphics.isEmpty()) {
      queueUndoRedoItem(mUndoElementStack, new UndoRedoItem(UndoRedoItem.Event.ERASE_GRAPHICS, copyGraphics()));
      mGraphics.clear();
    }
    mDrawingMode = DrawingMode.NONE;
    mIsPolylineStarted = false;
    mCurrentPoint = null;
    mCurrentLine = null;
    mCurrentPolygon = null;
    mCurrentPointCollection = null;
    mListener.onClearStateChanged(false);
  }

  /**
   * Creates a copy of the current graphics in the SketchGraphicsOverlay. This is used to replace graphics
   * after they have been cleared.
   *
   * @return a copy of the current graphics list
   */
  private List<Graphic> copyGraphics() {
    List<Graphic> graphicsCopy = new ArrayList<>();
    for (int i = 0; i < mGraphics.size(); ++i) {
      graphicsCopy.add(mGraphics.get(i));
    }
    return graphicsCopy;
  }

  /**
   * Helper method to get the midpoint of two points
   *
   * @param a the first point
   * @param b the second point
   * @return the midpoint of the two points
   */
  private Point getMidpoint(Point a, Point b) {
    double midX = (a.getX() + b.getX()) / 2.0;
    double midY = (a.getY() + b.getY()) / 2.0;
    return new Point(midX, midY, mMapView.getSpatialReference());
  }

  /**
   * Splits a line segment on the midpoint, adding a new vertex where the midpoint
   * had been and adding new midpoints before and after the new vertex.
   *
   * @param newGeometry the position of the new vertex
   */
  private void splitMidpoint(Point newGeometry) {
    // get the index of the current working graphic
    int graphicIndex = mGraphics.indexOf(mCurrentPoint);
    int pointIndex;
    // If we're drawing a polygon and splitting the final midpoint then the index in which
    // to insert the new point will be second to last
    if (mDrawingMode == DrawingMode.POLYGON && graphicIndex == mGraphics.size() - 1) {
      pointIndex = mCurrentPointCollection.size() - 1;
    } else {
      // If it's not a polygon or not the final midpoint, get the index in the point collection of
      // the point following the midpoint so the new vertex can be added before it
      Point pointAfterMidpoint = (Point) mGraphics.get(graphicIndex + 1).getGeometry();
      // Since the midpoints aren't in the point collection, get the index of the point after it
      pointIndex = mCurrentPointCollection.indexOf(pointAfterMidpoint);
    }
    // Add a new point at this index with the midpoint's new geometry
    mCurrentPointCollection.add(pointIndex, newGeometry);
    // Find the locations of the new midpoints (before and after the just added vertex point)
    Point newPreMidpoint = getMidpoint(mCurrentPointCollection.get(pointIndex - 1), newGeometry);
    Point newPostMidpoint = getMidpoint(newGeometry, mCurrentPointCollection.get(pointIndex + 1));
    // The graphic index is current pointing at the old midpoint, so add the pre-midpoint here
    // which will shift the index. Increment the counter so it points at the old midpoint again
    mGraphics.add(graphicIndex++, new Graphic(newPreMidpoint, mPolylineMidpointSymbol));
    // Add the post-midpoint at the index after the old midpoint
    mGraphics.add(graphicIndex + 1, new Graphic(newPostMidpoint, mPolylineMidpointSymbol));
    // Now that we've split and added a new vertex, the selected point is no longer a midpoint
    mIsMidpointSelected = false;
  }

  /**
   * Helper method to add a point to the polyline/polygon. Handles the work of
   * changing the working points symbol and updating the polyline/polygon geometry.
   *
   * @param point the point to add
   */
  private void addPolylinePoint(Point point) {
    Point midPoint = getMidpoint((Point) mCurrentPoint.getGeometry(), point);
    mCurrentPoint.setSymbol(mPolylineVertexSymbol);
    mCurrentLine.setGeometry(new Polyline(mCurrentPointCollection));
    mGraphics.add(new Graphic(midPoint, mPolylineMidpointSymbol));
    mCurrentPoint = new Graphic(point, mPointPlacementSymbol);
    mGraphics.add(mCurrentPoint);
    if (mDrawingMode == DrawingMode.POLYGON) {
      mCurrentPolygon.setGeometry(new Polygon(mCurrentPointCollection));
      Point polygonMidpoint = getMidpoint((Point) mCurrentPoint.getGeometry(), mCurrentPointCollection.get(0));
      mGraphics.add(new Graphic(polygonMidpoint, mPolylineMidpointSymbol));
    }
  }

  /**
   * Helper method to update the final midpoint of a polygon.
   */
  private void updatePolygonMidpoint() {
    // There will only be a final midpoint if there are at least 3 points
    if (mCurrentPointCollection.size() > 2) {
      // Get the final midpoint graphic and update its geometry with the midpoint of the final and first points
      Graphic postMidpoint = mGraphics.get(mGraphics.size() - 1);
      Point postMidpointGeometry = getMidpoint(mCurrentPointCollection.get(mCurrentPointCollection.size() - 2), mCurrentPointCollection.get(0));
      postMidpoint.setGeometry(postMidpointGeometry);
    }
  }

  /**
   * Finishes the current drawing by finalizing the working graphic[s], resetting the drawing state, and notifying
   * the listener that the drawing has finished.
   */
  private void finishDrawing() {
    // If current point is null then there is no drawing to finish
    if (mCurrentPoint != null) {
      switch (mDrawingMode) {
        case POINT:
          // If we're drawing a point, set the symbol to the placed symbol and reset the current point
          mCurrentPoint.setSymbol(mPointPlacedSymbol);
          mCurrentPoint = null;
          if (!mUndoElementStack.isEmpty()) {
            // Remove any of the move graphic undo events. Once placed, undo should just remove the point
            while (mUndoElementStack.peek().getEvent() == UndoRedoItem.Event.MOVE_POINT) {
              mUndoElementStack.pop();
            }
          }
          break;
        case POLYGON:
          // If we're drawing a polygon, logic is similar to finishing a polyline, but additionally need
          // to remove the final midpoint
          if (mGraphics.size() > 0) {
            mGraphics.remove(mGraphics.size() - 1);
          }
        case POLYLINE:
          // Set the current point to the placed vertex symbol and set the line to the placed line symbol
          mCurrentPoint.setSymbol(mPolylineVertexSymbol);
          mCurrentLine.setSymbol(mPolylinePlacedSymbol);
          // The second to last graphic is the final midpoint, and we need to remove all midpoints
          int index = 0;
          if (mGraphics.size() > 1) {
            index = mGraphics.size() - 2;
          }
          // Pop events until all the add/move polyline point events are gone (once placed, we only want to remove
          // a polyline on undo). The final popped event will be an ADD_GRAPHIC event, which will be replaced
          // further down by an ADD_POLYLINE event
          if (!(mUndoElementStack.isEmpty())) {
            UndoRedoItem.Event event;
            do {
              event = mUndoElementStack.pop().getEvent();
            }
            while (event == UndoRedoItem.Event.ADD_POLYLINE_POINT || event == UndoRedoItem.Event.MOVE_POLYLINE_POINT);

            while (index > 0 && mGraphics.get(index).getSymbol().equals(mPolylineMidpointSymbol)) {
              // For each add event, remove the midpoint and decrement the index
              mGraphics.remove(index);
              index -= 2;
            }
            // Push a new event indicating that we've finished a POLYLINE
            mUndoElementStack.add(new UndoRedoItem(UndoRedoItem.Event.ADD_POLYLINE, null));
          }
          // Reset the boolean and working graphics
          mIsPolylineStarted = false;
          mCurrentPoint = null;
          mCurrentLine = null;
          mCurrentPolygon = null;
          mCurrentPointCollection = null;
          mIsMidpointSelected = false;
          break;
      }
    }
    // Reset drawing mode and empty the redo stack
    mDrawingMode = DrawingMode.NONE;
    clearStack(mRedoElementStack);
    mListener.onDrawingFinished();
  }

  /**
   * Represents the different possible drawing modes the SketchGraphicsOverlay can be in
   */
  public enum DrawingMode {
    POINT,
    POLYLINE,
    POLYGON,
    NONE
  }

  /**
   * Represents a single action that can be undone/redone in the sketching stack
   */
  public static class UndoRedoItem {

    // Each item has an event type and optionally an object to use in undoing/redoing the action
    private Event mEvent;
    private Object mElement;

    /**
     * Creates a new UndoRedoItem with the specified event type and optional object.
     *
     * @param event   the type of event that occured
     * @param element optionally an object to help undo/redo the action
     */
    public UndoRedoItem(Event event, Object element) {
      mEvent = event;
      mElement = element;
    }

    /**
     * Gets the type of the event.
     *
     * @return the type of the event
     */
    public Event getEvent() {
      return mEvent;
    }

    /**
     * Gets the object with which to undo/redo the action (depending on the event type,
     * may be null).
     *
     * @return the object with which to undo/redo the action, or null if there is none
     */
    public Object getElement() {
      return mElement;
    }

    /**
     * Indicates different types of events that can occur.
     */
    public enum Event {
      ADD_POINT,
      MOVE_POINT,
      REMOVE_POINT,
      ADD_POLYLINE_POINT,
      MOVE_POLYLINE_POINT,
      REMOVE_POLYLINE_POINT,
      ADD_POLYLINE,
      REMOVE_POLYLINE,
      ERASE_GRAPHICS,
      REPLACE_GRAPHICS
    }

    /**
     * Represents the specific action of moving a polyline point, which additionally needs
     * to indicate if the point moved was a midpoint.
     */
    public static class MovePolylinePointElement {
      Graphic mGraphic;
      Point mPoint;
      boolean mIsMidpoint;

      /**
       * Instantiates a new MovePolylinePointElement.
       *
       * @param graphic the graphic of the moved point
       * @param point the position of the moved point
       * @param isMidpoint true if the moved point was a midpoint
       */
      public MovePolylinePointElement(Graphic graphic, Point point, boolean isMidpoint) {
        mGraphic = graphic;
        mPoint = point;
        mIsMidpoint = isMidpoint;
      }

      /**
       * Gets the graphic of the moved point.
       *
       * @return the graphic of the moved point
       */
      public Graphic getGraphic() {
        return mGraphic;
      }

      /**
       * Gets the position of the moved point (note this is required because the Point
       * returned by graphic.getGeometry() will have changed by reference).
       *
       * @return the position of the moved point
       */
      public Point getPoint() {
        return mPoint;
      }

      /**
       * Checks if the moved point was a midpoint.
       *
       * @return true if the moved point was a midpoint
       */
      public boolean isMidpoint() {
        return mIsMidpoint;
      }
    }
  }

  /**
   * A custom MapViewOnTouchListener that handles drawing events on the SketchGraphicsOverlay
   */
  private class DrawingMapViewOnTouchListener extends DefaultMapViewOnTouchListener {

    // Boolean flags to indicate whether we've chosen a midpoint or not and if we've started dragging it
    private boolean mVertexDragStarted = false;

    /**
     * Instantiates a new DrawingMapViewOnTouchListener with the specified context and MapView.
     *
     * @param context the application context from which to get the display metrics
     * @param mapView the MapView on which to control touch events
     */
    public DrawingMapViewOnTouchListener(Context context, MapView mapView) {
      super(context, mapView);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
      // get the screen point where user tapped
      final android.graphics.Point screenPoint = new android.graphics.Point((int) event.getX(), (int) event.getY());

      // identify graphics on the sketch graphics overlay
      final ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphic = mMapView.identifyGraphicsOverlayAsync(mGraphicsOverlay, screenPoint, 10.0, false);

      identifyGraphic.addDoneListener(new Runnable() {
        @Override
        public void run() {
          try {
            // get the list of graphics returned by identify
            IdentifyGraphicsOverlayResult identifyResult = identifyGraphic.get();
            List<Graphic> graphic = identifyResult.getGraphics();

            // In order to put new points inside a previously drawn or currently drawing polygon, don't trigger
            // on clicking a polygon graphic
            if (!graphic.isEmpty() && !(graphic.get(0).getGeometry() instanceof Polygon)) {
              // Clicking a graphic only changes the current point if we're drawing a polyline or polygon
              if (mDrawingMode == DrawingMode.POLYLINE || mDrawingMode == DrawingMode.POLYGON) {
                // Get the graphic we selected
                Graphic g = graphic.get(0);
                // If we clicked a point other than the point we're currently working with..
                if (mCurrentPoint != null && !mCurrentPoint.equals(g)) {
                  // If the last thing we had was a midpoint and we never moved it, set its symbol back to a midpoint
                  if (mIsMidpointSelected && !mVertexDragStarted) {
                    mCurrentPoint.setSymbol(mPolylineMidpointSymbol);
                  } else {
                    // If it wasn't a midpoint or we moved it, change it to a placed vertex symbol.
                    mCurrentPoint.setSymbol(mPolylineVertexSymbol);
                  }
                  // If the selected graphic has the midpoint symbol, take note that we selected a midpoint
                  mIsMidpointSelected = (g.getSymbol().equals(mPolylineMidpointSymbol));
                  mVertexDragStarted = false;
                  // Set our current working point to the selected graphic and change its symbol to the placing symbol
                  mCurrentPoint = g;
                  mCurrentPoint.setSymbol(mPointPlacementSymbol);
                }
              }
            } else {
              // Check if the graphics list was empty before we add our point
              boolean graphicsWasEmpty = mGraphics.isEmpty();
              // If we didn't click an existing graphic, add a new point to the current drawing
              Point point = mMapView.screenToLocation(screenPoint);
              if (mDrawingMode == DrawingMode.POINT) {
                if (mCurrentPoint == null) {
                  // If this is the first click after setting drawing mode to point, add a new grahpic
                  mCurrentPoint = new Graphic(point, mPointPlacementSymbol);
                  mGraphics.add(mCurrentPoint);
                  List<Graphic> graphics = new ArrayList<>();
                  graphics.add(mCurrentPoint);
                  // Push a new event indicating that we've added a graphic
                  queueUndoRedoItem(mUndoElementStack, new UndoRedoItem(UndoRedoItem.Event.ADD_POINT, graphics));
                } else {
                  // If we've already placed a point, clicking a new location will move the point
                  // Queue a new event indicating we've moved a graphic
                  queueUndoRedoItem(mUndoElementStack, new UndoRedoItem(UndoRedoItem.Event.MOVE_POINT, mCurrentPoint.getGeometry()));
                  mCurrentPoint.setGeometry(point);
                }
              } else if (mDrawingMode == DrawingMode.POLYLINE || mDrawingMode == DrawingMode.POLYGON) {
                // If we're drawing a polyline or polygon, we need to add a point to the point collection
                mIsMidpointSelected = false;
                if (!mIsPolylineStarted) {
                  // If this is the first point of a polyline
                  mCurrentPointCollection.add(point);
                  if (mDrawingMode == DrawingMode.POLYGON) {
                    // If it's a polygon, add a final point as the start point so the polyline draws a complete polygon
                    mCurrentPointCollection.add(point);
                  }
                } else {
                  // If we've already started the polyline/polygon...
                  if (mDrawingMode == DrawingMode.POLYGON) {
                    // If it's a polygon and there are at least 3 points, then we need to remove the last graphic (which is
                    // the midpoint of the last line segment) so that we can get the midpoint of the new segment and add it
                    if (mCurrentPointCollection.size() > 2) {
                      mGraphics.remove(mGraphics.size() - 1);
                    }
                    // Add the new point before the last point (so the polyline draws completely around the polygon)
                    mCurrentPointCollection.add(mCurrentPointCollection.size() - 1, point);
                  } else {
                    // If we're drawing a polyline just add it to the end
                    mCurrentPointCollection.add(point);
                  }
                }
                // If this is the first point set up the point, line and polygon grahpics
                if (!mIsPolylineStarted) {
                  // Create a new polyline and point
                  mCurrentLine = new Graphic(new Polyline(mCurrentPointCollection), mPolylinePlacementSymbol);
                  mCurrentPoint = new Graphic(point, mPointPlacementSymbol);
                  //
                  List<Graphic> graphics = new ArrayList<>();
                  // If we're drawing a polygon, also create a polygon graphic
                  if (mDrawingMode == DrawingMode.POLYGON) {
                    mCurrentPolygon = new Graphic(new Polygon(mCurrentPointCollection), mPolygonFillSymbol);
                    // Add it first so the line and points draw on top of it
                    mGraphics.add(mCurrentPolygon);
                    graphics.add(mCurrentPolygon);
                  }
                  // Add the line first so points drop on top of it
                  mGraphics.add(mCurrentLine);
                  mGraphics.add(mCurrentPoint);
                  graphics.add(mCurrentLine);
                  graphics.add(mCurrentPoint);
                  // Queue a new event indicating we've added a point
                  queueUndoRedoItem(mUndoElementStack, new UndoRedoItem(UndoRedoItem.Event.ADD_POINT, graphics));
                  mIsPolylineStarted = true;
                } else {
                  // If we've already started the line, just add the polyline point
                  addPolylinePoint(point);
                  queueUndoRedoItem(mUndoElementStack, new UndoRedoItem(UndoRedoItem.Event.ADD_POLYLINE_POINT, null));
                }
              }
              boolean graphicsIsEmpty = mGraphics.isEmpty();
              // If the graphics list was previously empty and now it's not, notify the listener
              // to enable the clear button
              if (graphicsWasEmpty && !graphicsIsEmpty) {
                mListener.onClearStateChanged(true);
                // If previous non empty and now it is, notify listener to disable the clear button
              } else if (!graphicsWasEmpty && graphicsIsEmpty) {
                mListener.onClearStateChanged(false);
              }
              // Any time we add a new graphic, clear the redo stack since we should only be able to
              // do redos directly after undos
              clearStack(mRedoElementStack);
            }
          } catch (InterruptedException | ExecutionException ie) {
            ie.printStackTrace();
          }
        }
      });
      return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
      // Long press finishes a drawing
      finishDrawing();
    }

    @Override
    public boolean onScroll(MotionEvent from, MotionEvent to, float distanceX, float distanceY) {
      // Assume that we're going to call the super method (for panning)
      boolean callSuper = true;
      // If we don't have a current point we're just going to call super
      if (mCurrentPoint != null) {
        // If we do have a current working point, check to see if we're we are dragging from is close to our working graphic
        android.graphics.Point currentPoint = mMapView.locationToScreen((Point) mCurrentPoint.getGeometry());
        android.graphics.Point fromPoint = new android.graphics.Point((int) from.getX(), (int) from.getY());
        int dx = currentPoint.x - fromPoint.x;
        int dy = currentPoint.y - fromPoint.y;
        int distance = (int) Math.sqrt((dx * dx) + (dy * dy));
        if (distance < 20) {
          // If it is, don't call the super method since we'll be moving our working point
          callSuper = false;
          // Get the location point that we're moving to
          android.graphics.Point toPoint = new android.graphics.Point((int) to.getX(), (int) to.getY());
          Point oldGeometry = (Point) mCurrentPoint.getGeometry();
          // Make a copy of the current geometry so that changes to the previous geometry don't update by reference
          Point oldPointCopy = new Point(oldGeometry.getX(), oldGeometry.getY(), mMapView.getSpatialReference());
          Point newGeometry = mMapView.screenToLocation(toPoint);
          // If this is the first move event after clicking down, mark the current position so we can undo the move event
          if (!mVertexDragStarted) {
            // Queue a new event indicating that we've moved a point (or polyline point)
            if (mDrawingMode == DrawingMode.POINT) {
              queueUndoRedoItem(mUndoElementStack, new UndoRedoItem(UndoRedoItem.Event.MOVE_POINT, oldPointCopy));
            } else {
              queueUndoRedoItem(mUndoElementStack, new UndoRedoItem(UndoRedoItem.Event.MOVE_POLYLINE_POINT,
                      new UndoRedoItem.MovePolylinePointElement(mCurrentPoint, oldPointCopy, mIsMidpointSelected)));
            }
          }
          // If we're drawing a polyline or polygon, move the current point and the attached line segments
          if (mDrawingMode == DrawingMode.POLYLINE || mDrawingMode == DrawingMode.POLYGON) {
            // get the index of the current working graphic
            int graphicIndex = mGraphics.indexOf(mCurrentPoint);
            int pointIndex;
            // If our current working graphic is a midpoint and this is the first event of dragging it, we'll need
            // to add a new vertex where the midpoint was and create two new midpoints to the surrounding points
            if (mIsMidpointSelected && !mVertexDragStarted) {
              splitMidpoint(newGeometry);
            } else {
              // If it's not a midpoint, then just find the index of the vertex point
              pointIndex = mCurrentPointCollection.indexOf(mCurrentPoint.getGeometry());
              // Update the location of the selected point to the new geometry and update the line graphic
              mCurrentPointCollection.set(pointIndex, newGeometry);
              // Get the midpoint before this point so it can move with the line segment
              Graphic preMidpoint = (pointIndex == 0) ? null : mGraphics.get(graphicIndex - 1);
              // If it's not null (only null if this is the first point in the line)...
              if (preMidpoint != null) {
                // Get the new midpoint location and update the graphic's geometry
                Point preMidpointGeometry = getMidpoint(mCurrentPointCollection.get(pointIndex - 1), newGeometry);
                preMidpoint.setGeometry(preMidpointGeometry);
              }
              // Get the midpoint after this point so it can move with the line segment
              Graphic postMidpoint = (pointIndex == mCurrentPointCollection.size() - 1) ? null : mGraphics.get(graphicIndex + 1);
              // If it's not null (only null if this is the last point in the line)...
              if (postMidpoint != null) {
                // Get the new midpoint location and update the graphic's geometry
                Point postMidpointGeometry = getMidpoint(newGeometry, mCurrentPointCollection.get(pointIndex + 1));
                postMidpoint.setGeometry(postMidpointGeometry);
              }
              // If we're drawing a polygon we also need to update the polygon geometry and final midpoint
              if (mDrawingMode == DrawingMode.POLYGON) {
                if (pointIndex == 0 || pointIndex == mCurrentPointCollection.size() - 2) {
                  // If we're moving the first point, we need to replicate the change in the duplicate final point
                  if (pointIndex == 0) {
                    mCurrentPointCollection.set(mCurrentPointCollection.size() - 1, newGeometry);
                  }
                  updatePolygonMidpoint();
                }
                mCurrentPolygon.setGeometry(new Polygon(mCurrentPointCollection));
              }
              mCurrentLine.setGeometry(new Polyline(mCurrentPointCollection));
            }
          }
          // Indicate that we've started the point drag
          mVertexDragStarted = true;
          // Finally update the geometry up the current point
          mCurrentPoint.setGeometry(newGeometry);
          // Any time we add a new graphic, clear the redo stack since we should only be able to
          // do redos directly after undos
          clearStack(mRedoElementStack);
        }
      }
      // If we didn't do a point drag, call super to pan the map
      if (callSuper) {
        super.onScroll(from, to, distanceX, distanceY);
      }
      return true;
    }

    @Override
    public boolean onUp(MotionEvent event) {
      // Reset the drag started flag when the pointer is lifted
      mVertexDragStarted = false;
      return true;
    }
  }
}
