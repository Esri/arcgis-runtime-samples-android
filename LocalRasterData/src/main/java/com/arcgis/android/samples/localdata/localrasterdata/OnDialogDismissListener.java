/* Copyright 2014 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the Sample code usage restrictions document for further information.
 *
 */

package com.arcgis.android.samples.localdata.localrasterdata;

import com.esri.core.renderer.RasterRenderer;

// Event listener of the OK button of a dialog.
public interface OnDialogDismissListener {
  public void onPositiveClicked(RasterRenderer renderer);
}
