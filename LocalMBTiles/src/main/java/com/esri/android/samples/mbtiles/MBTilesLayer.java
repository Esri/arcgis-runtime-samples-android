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

package com.esri.android.samples.mbtiles;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.esri.android.map.TiledServiceLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;

/**
 * The MBTilesLayer class allows you to work with a MBTiles stored in a SQLite
 * database.
 * 
 */
public class MBTilesLayer extends TiledServiceLayer {

  private SQLiteDatabase mapDb;
  private int mLevels = 0;

  /**
   * The constructor to instantiate MBTiles from a path on device
   * 
   * @param path
   *          path is expected to be of the form /sdcard/path/package.mbtiles
   */
  public MBTilesLayer(String path) {
    super(path);
    try {
      mapDb = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
    } catch (SQLException ex) {
      Log.e(this.getName(), ex.getMessage());
      throw (ex);
    }

    // Default TMS bounds = bounds of Web Mercator projection
    Envelope envWGS = new Envelope(-180.0, -85.0511, 180.0, 85.0511);

    // See if the MBTiles DB defines their own Bounds in the metadata table
    Cursor bounds = mapDb.rawQuery("SELECT value FROM metadata WHERE name = 'bounds'", null);
    if (bounds.moveToFirst()) {
      String bs = bounds.getString(0);
      String[] ba = bs.split(",", 4);
      if (ba.length == 4) {
        double leftLon = Double.parseDouble(ba[0]);
        double topLat = Double.parseDouble(ba[3]);
        double rightLon = Double.parseDouble(ba[2]);
        double bottomLat = Double.parseDouble(ba[1]);

        envWGS = new Envelope(leftLon, bottomLat, rightLon, topLat);
      }
    }

    Envelope envWeb = (Envelope) GeometryEngine.project(envWGS, SpatialReference.create(4326),
        SpatialReference.create(3857));

    Point origin = envWeb.getUpperLeft();

    Cursor maxLevelCur = mapDb.rawQuery("SELECT MAX(zoom_level) AS max_zoom FROM tiles", null);
    if (maxLevelCur.moveToFirst()) {
      mLevels = maxLevelCur.getInt(0);
    }

    Log.i("TAG", "Max levels = " + Integer.toString(mLevels));

    double[] resolution = new double[mLevels];
    double[] scale = new double[mLevels];
    for (int i = 0; i < mLevels; i++) {
      // see the TMS spec for derivation of the level 0 scale and resolution
      // For each level the resolution (in meters per pixel) doubles
      resolution[i] = 156543.032 / Math.pow(2, i);
      // Level 0 scale is 1:554,678,932. Each level doubles this.
      scale[i] = 554678932 / Math.pow(2, i);
    }

    /*
     * Note, the constructor must set the following values or we won't send the
     * status change events to listeners and the tiles will not be fetched
     * 
     * Origin is Top Left (web Mercator) , the rest are defined by the TMS
     * Global-mercator spec (scales, resolution, 96dpi 256x256 pixel tiles) See:
     * http://wiki.osgeo.org/wiki/Tile_Map_Service_Specification#global-mercator
     */
    TileInfo ti = new TileInfo(origin, scale, resolution, mLevels, 96, 256, 256);

    this.setTileInfo(ti);
    this.setFullExtent(envWeb);
    this.setDefaultSpatialReference(SpatialReference.create(3857));
    this.setInitialExtent(envWeb);

    this.initLayer();

  }

  @Override
  protected byte[] getTile(int level, int col, int row) throws Exception {

    // need to flip origin
    int nRows = (1 << level); // Num rows = 2^level
    int tmsRow = nRows - 1 - row;

    Cursor imageCur = mapDb.rawQuery("SELECT tile_data FROM tiles WHERE zoom_level = " + Integer.toString(level)
        + " AND tile_column = " + Integer.toString(col) + " AND tile_row = " + Integer.toString(tmsRow), null);
    if (imageCur.moveToFirst()) {
      return imageCur.getBlob(0);
    }
    return null; // Alternatively we might return a "no data" tile
  }

}
