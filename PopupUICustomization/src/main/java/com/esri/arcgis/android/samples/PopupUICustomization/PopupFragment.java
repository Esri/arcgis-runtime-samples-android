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

package com.esri.arcgis.android.samples.PopupUICustomization;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISPopupInfo;
import com.esri.android.map.popup.Popup;
import com.esri.android.map.popup.PopupContainer;
import com.esri.android.map.popup.PopupContainerView;
import com.esri.core.map.popup.PopupInfo;

/*
 * A fragment to display pop-ups and handle user interactions with a pop-up. 
 */
public class PopupFragment extends Fragment {
	
	private PopupContainer mPopupContainer;
	private MapView mMapView;
	private boolean mIsInitialize, mIsDisplayed;
	private OnEditListener mEditListener;

	public PopupFragment() {
		mIsInitialize = false;
		mIsDisplayed = false;
	}
	
	public PopupFragment(MapView mapView) {
		this.mMapView = mapView;
		mPopupContainer = new PopupContainer(mMapView);
		mIsInitialize = true;
		mIsDisplayed = false;
	}
	
	public PopupFragment(MapView mapView, PopupContainer container) {
    this.mMapView = mapView;
    this.mPopupContainer = container;
    if (mPopupContainer != null)
      mIsInitialize = true;
    else
      mIsInitialize = false;
    mIsDisplayed = false;
  }
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		// Set listener to handle editing events
		mEditListener = (OnEditListener) activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Create popupcontainer if it hasn't been created
		if (mPopupContainer == null) {
		  mPopupContainer = new PopupContainer(mMapView);
			mIsInitialize = true;
		}
		
		// Fragment wants to add menu to action bar
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	  
		PopupContainerView view = null;
		
		if (mPopupContainer != null) {
			view = mPopupContainer.getPopupContainerView();
			view.setOnPageChangelistener(new OnPageChangeListener() {
				
				@Override
				public void onPageSelected(int arg0) {
					
				}
				
				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {
					// Refresh menu item while swipping popups
					Activity activity = (Activity)mMapView.getContext();
					activity.invalidateOptionsMenu();
				}
				
				@Override
				public void onPageScrollStateChanged(int arg0) {
					
				}
			});
		}
		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.popup_activity, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if ((mPopupContainer == null) || (mPopupContainer.getPopupCount() <= 0))
			return true;
		
		Popup popup = mPopupContainer.getCurrentPopup();
	    switch(item.getItemId()){
		    case R.id.menu_camera:
	      	startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), 1);
	       break;
		    case R.id.menu_delete:
	      	deleteFeature(popup);
	       break;
	      case R.id.menu_edit:
	        ViewGroup view = popup.getLayout().getLayout();
	        LinearLayout ll = (LinearLayout)view.findViewById(R.id.second_inner_linearlayout);
	        if (ll != null)
	          ll.setVisibility(View.GONE);
	      	editFeature(popup);
	        break;
	      case R.id.menu_save:
	      	saveFeature(popup);
	       break;
	    }
	    
		return true;
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
	
		// Turn on/off menu items based on popup's edit capabilities
		for (int i = 0; i < menu.size(); i++) {
			MenuItem item = menu.getItem(i);
			if (mPopupContainer != null) {
				Popup popup = mPopupContainer.getCurrentPopup();
				if (popup != null) {
					if (popup.isEditMode() ) {
						if ((item.getItemId() == R.id.menu_save) || (item.getItemId() == R.id.menu_camera)) {
							item.setVisible(true);
							item.setEnabled(true);
						}
						else {
							item.setVisible(false);
							item.setEnabled(false);
						}
					}
					else {
						if (((item.getItemId() == R.id.menu_edit) && (popup.isEditable()))
								|| ((item.getItemId() == R.id.menu_delete) && (popup.isDeletable()))) {
							item.setVisible(true);
							item.setEnabled(true);
						} else {
							item.setVisible(false);
							item.setEnabled(false);
						}
					}
				} else {
					item.setVisible(false);
					item.setEnabled(false);
				}
			} else {
				item.setVisible(false);
				item.setEnabled(false);
			}
		}
	}
	
	public void addPopup(Popup popup) {
		if (mPopupContainer != null) 
			mPopupContainer.addPopup(popup);
	}

	// Indicate if popupcontainer has been created
	public boolean isInitialize() {
		return mIsInitialize;
	}

	public void setInitialize(boolean isInitialize) {
		this.mIsInitialize = isInitialize;
	}

	// Indicate if fragment is displayed
	public boolean isDisplayed() {
		return mIsDisplayed;
	}

	public void setDisplayed(boolean isDisplayed) {
		this.mIsDisplayed = isDisplayed;
	}
	
	//Display 
  public void show() {
    if (mIsDisplayed)
      return;
    
    FragmentActivity activity = (FragmentActivity) mMapView.getContext();
    FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
    transaction.setCustomAnimations(R.anim.popup_rotate_in, R.anim.popup_rotate_out);
    transaction.add(android.R.id.content, this, null);
    transaction.addToBackStack(null);
    transaction.commit();
    setDisplayed(true);
  }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		 if ((resultCode == Activity.RESULT_OK) 
		     && (data != null) && (mPopupContainer != null)) {
	    	// Add the selected media as attachment.
	      Uri selectedImage = data.getData();
	      mPopupContainer.getCurrentPopup().addAttachment(selectedImage);
	    }
	}
	
	// When "delete" menu item is clicked
	private void deleteFeature(Popup popup) {
		ArcGISFeatureLayer featureLayer = getFeatureLayer(popup);
		mEditListener.onDelete(featureLayer, popup);
	}
	
	// When "edit" menu item is clicked
	private void editFeature(Popup popup) {
		ArcGISFeatureLayer featureLayer = getFeatureLayer(popup);
		mEditListener.onEdit(featureLayer, popup);
	}
	
	// When "save" menu item is clicked
	private void saveFeature(Popup popup) {
		ArcGISFeatureLayer featureLayer = getFeatureLayer(popup);
		mEditListener.onSave(featureLayer, popup);
	}
	
	// Get the feature layer which is associated with the current popup
	private ArcGISFeatureLayer getFeatureLayer(Popup popup) {
		ArcGISFeatureLayer featureLayer = null;
		
		if ((mMapView == null) || (popup == null))
			return null;
		PopupInfo popupInfo = popup.getPopupInfo();
		if (popupInfo instanceof ArcGISPopupInfo) {
			ArcGISPopupInfo agsPopupInfo = (ArcGISPopupInfo) popupInfo;
			Layer[] layers = mMapView.getLayers();
			for (Layer layer : layers) {
				if ((layer instanceof ArcGISFeatureLayer) 
						&& (layer.getUrl().compareToIgnoreCase(agsPopupInfo.getLayerUrl()) == 0)) {
					featureLayer = (ArcGISFeatureLayer) layer;
					return featureLayer;
				}
			}
		}
		
		return featureLayer;
	}
	
	// Listener to handle editing events
	public interface OnEditListener {
		public void onDelete(ArcGISFeatureLayer fl, Popup popup);
		public void onEdit(ArcGISFeatureLayer fl, Popup popup);
		public void onSave(ArcGISFeatureLayer fl, Popup popup);
	}

}
