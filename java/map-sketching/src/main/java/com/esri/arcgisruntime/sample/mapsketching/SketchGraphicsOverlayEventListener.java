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

/**
 * Defines events that can be fired from a SketchGraphicsOverlay that the containing
 * activity should listen to in order to reflect the enabled/disabled/selected states
 * of the various button controls.
 */
public interface SketchGraphicsOverlayEventListener {

  /**
   * Called when the state of the undo event stack changes. If true, an undo can be
   * performed and hence the undo button should be enabled. If false, the undo event
   * stack is empty and the undo button should be disabled.
   *
   * @param undoEnabled true if the undo button should be enabled
   */
  void onUndoStateChanged(boolean undoEnabled);

  /**
   * Called when the state of the redo event stack changes. If true, an redo can be
   * performed and hence the redo button should be enabled. If false, the redo event
   * stack is empty and the redo button should be disabled.
   *
   * @param redoEnabled true if the redo button should be enabled
   */
  void onRedoStateChanged(boolean redoEnabled);

  /**
   * Called when the state of clearing the drawings changes. If true, there are currently
   * drawings on the SketchGraphicsOverlay which can be cleared, and hence the clear
   * button should be enabled. If false, the SketchGraphicsOverlay is empty and the
   * clear button should be disabled.
   *
   * @param clearEnabled true if the clear button should be enabled
   */
  void onClearStateChanged(boolean clearEnabled);

  /**
   * Called when a drawing is finished. When a drawing is finished, the currently selected
   * drawing button can be reset.
   */
  void onDrawingFinished();
}
