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

package com.arcgis.android.samples.localdata.localrasterdata;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.arcgis.android.samples.localdata.localrasterdata.FileBrowserFragment.OnFileAndFolderFinishListener;
import com.arcgis.android.samples.localdata.localrasterdata.FileBrowserFragment.RasterLayerAction;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.RasterLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.raster.FileRasterSource;
import com.esri.core.renderer.BlendRenderer;
import com.esri.core.renderer.HillshadeRenderer;
import com.esri.core.renderer.RGBRenderer;
import com.esri.core.renderer.RasterRenderer;
import com.esri.core.renderer.StretchParameters;
import com.esri.core.renderer.StretchParameters.ClipStretchParameters;
import com.esri.core.renderer.StretchParameters.StdDevStretchParameters;
import com.esri.core.renderer.StretchRenderer;

import java.io.File;
import java.io.FileNotFoundException;

public class MainActivity extends FragmentActivity implements
		OnFileAndFolderFinishListener, OnDialogDismissListener {

	private MapView mMapView;
	private String mElevationSourcePath = null;
	private String mInitDir = Environment.getExternalStorageDirectory()
			.getPath();

    // The extent of a Raster Layer, use to set the extent when raster layer loads
    Envelope mRasterLayerExtent = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Create a map view.
    mMapView = new MapView(this);
    // Add streets basemap
    mMapView.addLayer(new ArcGISTiledMapServiceLayer(
        getResources().getString(R.string.basemap_url)));
    setContentView(mMapView);

    // Set a Listener for map status changes
    // This will be called when adding raster layers as operational layers
    mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
        @Override
        public void onStatusChanged(Object source, STATUS status) {
      // Set the map extent once the map has been initialized and a raster layer
      // is added or changed; this will be indicated by the source being of type
      // RasterLayer and the initialization of the raster layer.
      if(source instanceof RasterLayer && STATUS.LAYER_LOADED == status){
        mMapView.setExtent(mRasterLayerExtent, 2);
      }
        }
    });

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_layer_raster:
			openFileBrowser();
			return true;
		case R.id.menu_renderer_blend:
		  changeBlendRenderer();
		  return true;
		case R.id.menu_renderer_hillshade:
			changeHillshadeRenderer();
			return true;
		case R.id.menu_renderer_rgb:
			changeRGBRenderer();
			return true;
		case R.id.menu_renderer_stretch:
			changeStretchRenderer();
			return true;
		}

		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		for (int i = 0; i < menu.size(); i++) {
			MenuItem item = menu.getItem(i);
			if ((item.getItemId() == R.id.menu_layer_raster)
					|| (item.getItemId() == R.id.menu_renderer)) {
				item.setVisible(true);
			} else if ((item.getItemId() == R.id.menu_dir_up)
					|| (item.getItemId() == R.id.menu_dir_select)) {
				item.setVisible(false);
			}
		}
		return true;
	}

	@Override
	// Handle the click event for the "ok" button in the dialog to input
	// parameters for raster renderer.
	public void onPositiveClicked(RasterRenderer renderer) {
		if (mMapView == null || !mMapView.isLoaded()) {
      return;
    }

		// Set renderer for raster layer
		Layer[] layers = mMapView.getLayers();
		for (Layer layer : layers) {
			if (layer instanceof RasterLayer) {
				RasterLayer rastLayer = (RasterLayer) layer;
				try {
				  if (renderer instanceof BlendRenderer) {
				    BlendRenderer blendRenderer = (BlendRenderer) renderer;
				    blendRenderer.setElevationSource(new FileRasterSource(mElevationSourcePath));
				    rastLayer.setRenderer(blendRenderer);
				  } else {
				    rastLayer.setRenderer(renderer);
				  }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	// Handle the event when a file is tapped in the file browser
	public void onFileFinish(String path, RasterLayerAction action) {
		// Dismiss file browser and refresh actionbar.
		this.getSupportFragmentManager().popBackStack();
		this.invalidateOptionsMenu();
		// Use the current path as the initial directory next time opening the
		// file bowser.
		File tempFile = new File(path);
		if (tempFile.isFile()) {
			mInitDir = tempFile.getParent();
		} else {
			mInitDir = tempFile.getPath();
		}

    addRasterLayer(path, action);
	}

	@Override
	public void onDirectoryFinish(String path) {

	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.unpause();
	}

	private RasterLayer initRasterLayer(String rasterPath, boolean asOperationalLayer) {
	  RasterLayer rastLayer = null;
	  if ((mMapView == null) || (!mMapView.isLoaded())) {
      return null;
    }
	  
	  try {
	    FileRasterSource rasterSource = new FileRasterSource(rasterPath);
	    if (asOperationalLayer) {
        rasterSource.project(mMapView.getSpatialReference());
      }
	    
      rastLayer = new RasterLayer(rasterSource);
	  } catch (IllegalArgumentException ie) {
      Toast.makeText(this, "null or empty path", Toast.LENGTH_SHORT).show();
    } catch (FileNotFoundException fe) {
      Toast.makeText(this, "raster file doesn't exist", Toast.LENGTH_SHORT).show();
    } catch (RuntimeException re) {
      Toast.makeText(this, "unsupported raster file", Toast.LENGTH_SHORT).show();
    }

	  return rastLayer;
	}
	
	private void loadRasterLayerAsBaseMapLayer(String rasterPath) {
	  RasterLayer rastLayer = initRasterLayer(rasterPath, false);
    if ((rastLayer == null) || (!rastLayer.isInitialized())) {
      return;
    }
    
    reset();
    mMapView.removeAll();
    mMapView.recycle();
    mMapView = new MapView(this);
    mMapView.addLayer(rastLayer);

    setContentView(mMapView);
	}

	private void loadRasterLayerAsOperationalLayer(String rasterPath) {
    RasterLayer rastLayer = initRasterLayer(rasterPath, true);
    if ((rastLayer == null) || (!rastLayer.isInitialized())) {
      return;
    }

    mMapView.addLayer(rastLayer);
    // set extent to raster layer
    mRasterLayerExtent = rastLayer.getFullExtent();

	}
	
	private void addRasterLayer(String rasterPath, RasterLayerAction action) {
		if (rasterPath == null || rasterPath.length() <= 0) {
			Toast.makeText(this, "Invalid path", Toast.LENGTH_LONG).show();
			return;
		}

		switch (action) {
		case BASEMAP_LAYER:
			loadRasterLayerAsBaseMapLayer(rasterPath);
			break;
		case OPERATIONAL_LAYER:
      loadRasterLayerAsOperationalLayer(rasterPath);
      break;
		case ELEVATION_SOURCE:
			mElevationSourcePath = rasterPath;
			break;
		case BASEMAP_LAYER_AND_ELEVATION_SOURCE:
			loadRasterLayerAsBaseMapLayer(rasterPath);
			mElevationSourcePath = rasterPath;
			break;
		case OPERATIONAL_LAYER_AND_ELEVATION_SOURCE:
		    loadRasterLayerAsOperationalLayer(rasterPath);
		    mElevationSourcePath = rasterPath;
		    break;
		case NONE:
			break;
		}
	}

	private RasterRenderer getRasterRenderer(RendererType type) {
	  if ((mMapView == null) || (!mMapView.isLoaded())) {
      return null;
    }
	  
	  Layer[] layers = mMapView.getLayers();
	  for (Layer layer : layers) {
	    if (layer instanceof RasterLayer) {
	      RasterLayer rasterLayer = (RasterLayer) layer;
	      RasterRenderer renderer = rasterLayer.getRenderer();
  	    switch (type) {
  	      case BLEND:
  	        if (renderer instanceof BlendRenderer) {
  	          return renderer;
  	        }
  	        break;
  	      case HILLSHADE:
  	        if ((renderer instanceof HillshadeRenderer)
                    && (!(renderer instanceof BlendRenderer))) {
  	          return renderer;
  	        }
  	        break;
  	      case RGB:
  	        if (renderer instanceof RGBRenderer) {
  	          return renderer;
  	        }
  	        break;
  	      case STRETCHED:
  	        if (renderer instanceof StretchRenderer) {
  	          return renderer;
  	        }
  	        break;
  	    }
	    }
	  }

	  return null;
	}
	
	private void createHillshadeRendererParameterFragment(RendererType type) {
	  if (!((type == RendererType.BLEND) || (type == RendererType.HILLSHADE))) {
      return;
    }
	  
	  RasterRenderer rasterRenderer = getRasterRenderer(type);
	  HillshadeRendererParametersFragment fragment = null;
	  if (rasterRenderer == null) {
	    fragment = HillshadeRendererParametersFragment
	         .newInstance(type.getCode(), EditTextUtils.DEFAULT_DOUBLE_VALUE,
	             EditTextUtils.DEFAULT_DOUBLE_VALUE,
	             EditTextUtils.DEFAULT_DOUBLE_VALUE,
	             EditTextUtils.DEFAULT_INT_VALUE,
	             EditTextUtils.DEFAULT_DOUBLE_VALUE,
	             EditTextUtils.DEFAULT_DOUBLE_VALUE,
	             EditTextUtils.DEFAULT_DOUBLE_VALUE);
	  } else {
	    if (rasterRenderer instanceof BlendRenderer) {
        BlendRenderer renderer = (BlendRenderer) rasterRenderer;
        fragment = HillshadeRendererParametersFragment
             .newInstance(type.getCode(), renderer.getAltitude(), renderer.getAzimuth(), 
                 renderer.getZfactor(), renderer.getSlopeType().ordinal(), renderer.getPixelSizeFactor(), 
                 renderer.getPixelSizePower(), renderer.getGamma());
      } else if (rasterRenderer instanceof HillshadeRenderer) {
	        HillshadeRenderer renderer = (HillshadeRenderer) rasterRenderer;
	        fragment = HillshadeRendererParametersFragment
	            .newInstance(type.getCode(), renderer.getAltitude(), renderer.getAzimuth(), 
	                renderer.getZfactor(), renderer.getSlopeType().ordinal(), renderer.getPixelSizeFactor(), 
	                renderer.getPixelSizePower(), EditTextUtils.DEFAULT_DOUBLE_VALUE);
	      }
	  }
	  
	  if (fragment == null) {
      return;
    }
	  
	  if (type == RendererType.BLEND) {
	    fragment.show(getSupportFragmentManager(), "blend dialog");
	  } else {
	    fragment.show(getSupportFragmentManager(), "hillshade dialog");
	  }
	}
	
	private void changeBlendRenderer() {
	  createHillshadeRendererParameterFragment(RendererType.BLEND);
  }

	private void changeHillshadeRenderer() {
	  createHillshadeRendererParameterFragment(RendererType.HILLSHADE);
	}

	private void createStretchParameterFragment(RendererType type) {
	  if (!((type == RendererType.RGB) || (type == RendererType.STRETCHED))) {
      return;
    }
    
    RasterRenderer rasterRenderer = getRasterRenderer(type);
    StretchParametersFragment fragment;
    if (rasterRenderer == null) {
      fragment = StretchParametersFragment
           .newInstance(type.getCode(), EditTextUtils.DEFAULT_INT_VALUE,
               EditTextUtils.DEFAULT_DOUBLE_VALUE,
               EditTextUtils.DEFAULT_DOUBLE_VALUE,
               EditTextUtils.DEFAULT_DOUBLE_VALUE,
               EditTextUtils.DEFAULT_DOUBLE_VALUE);
    } else {
      StretchParameters params = null;
      if (rasterRenderer instanceof StretchRenderer) {
        StretchRenderer renderer = (StretchRenderer) rasterRenderer;
        params = renderer.getStretchParameters();
      } else if (rasterRenderer instanceof RGBRenderer) {
        RGBRenderer renderer = (RGBRenderer) rasterRenderer;
        params = renderer.getStretchParameters();
      }
      
      if (params != null) {
        double stdDev = EditTextUtils.DEFAULT_DOUBLE_VALUE;
        double minClip = EditTextUtils.DEFAULT_DOUBLE_VALUE;
        double maxClip = EditTextUtils.DEFAULT_DOUBLE_VALUE;
        double gamma = params.getGamma();
        if (params instanceof StdDevStretchParameters) {
          StdDevStretchParameters stdParams = (StdDevStretchParameters) params;
          stdDev = stdParams.getStdDev();
        } else if (params instanceof ClipStretchParameters) {
          ClipStretchParameters clipParams = (ClipStretchParameters) params;
          minClip = clipParams.getMinClip();
          maxClip = clipParams.getMaxClip();
        }
        fragment = StretchParametersFragment
            .newInstance(type.getCode(), params.getType().ordinal(), 
                stdDev,
                minClip, 
                maxClip,
                gamma);
      } else {
        fragment = StretchParametersFragment
            .newInstance(type.getCode(), EditTextUtils.DEFAULT_INT_VALUE,
                EditTextUtils.DEFAULT_DOUBLE_VALUE,
                EditTextUtils.DEFAULT_DOUBLE_VALUE,
                EditTextUtils.DEFAULT_DOUBLE_VALUE,
                EditTextUtils.DEFAULT_DOUBLE_VALUE);
      }
    }
    
    if (fragment == null) {
      return;
    }
    
    if (type == RendererType.RGB) {
      fragment.show(getSupportFragmentManager(), "RGB dialog");
    } else {
      fragment.show(getSupportFragmentManager(), "stretch dialog");
    }
	}
	
	private void changeRGBRenderer() {
		createStretchParameterFragment(RendererType.RGB);
	}

	private void changeStretchRenderer() {
    createStretchParameterFragment(RendererType.STRETCHED);
	}
	
	private void reset() {
		mElevationSourcePath = null;
	}

	private void openFileBrowser() {
		FileBrowserFragment fileFragment = FileBrowserFragment.newInstance();
		fileFragment.setInitialDirectory(mInitDir);
		getSupportFragmentManager().beginTransaction()
				.add(android.R.id.content, fileFragment).addToBackStack(null)
				.commit();
	}

}