/* Copyright 2013 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the sample code usage restrictions document for further information. 
 */

package com.arcgis.android.samples.geometrysample;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class GeometrySampleActivity extends FragmentActivity implements
		SampleListFragment.OnSampleNameSelectedListener {

	BufferFragment bufferFrag;

	UnionDifferenceFragment uniondiffFrag;

	SpatialRelationshipsFragment spatialrelationFrag;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.geometry_samples);

		// Check whether the activity is using the layout version with
		// the fragment_container FrameLayout. If so, we must add the first
		// fragment
		if (findViewById(R.id.fragment_container) != null) {

			// However, if we're being restored from a previous state,
			// then we don't need to do anything and should return or else
			// we could end up with overlapping fragments.
			if (savedInstanceState != null) {
				return;
			}

			// Create an instance of the SampleListFragment
			SampleListFragment firstFragment = new SampleListFragment();

			// In case this activity was started with special instructions from
			// an Intent,
			// pass the Intent's extras to the fragment as arguments
			firstFragment.setArguments(getIntent().getExtras());

			// Add the fragment to the 'fragment_container' FrameLayout
			getSupportFragmentManager().beginTransaction()
					.add(R.id.fragment_container, firstFragment).commit();
		}
	}

	public void onArticleSelected(int position) {

		FragmentTransaction fragTransaction = getSupportFragmentManager()
				.beginTransaction();

		if (bufferFrag != null && !bufferFrag.isDetached()) {

			fragTransaction.detach(bufferFrag);
			bufferFrag = null;
		}

		if (uniondiffFrag != null && !uniondiffFrag.isDetached()) {

			fragTransaction.detach(uniondiffFrag);
			uniondiffFrag = null;
		}

		if (spatialrelationFrag != null && !spatialrelationFrag.isDetached()) {

			fragTransaction.detach(spatialrelationFrag);
			spatialrelationFrag = null;
		}

		switch (position) {
		case 0:

			if (bufferFrag == null || bufferFrag.getShownIndex() != position) {
				// Make new fragment to show this selection.
				bufferFrag = BufferFragment.newInstance(position);

				// Execute a transaction, replacing any existing fragment
				// with this one inside the frame.

				fragTransaction.add(R.id.sample_fragment, bufferFrag);

				fragTransaction.setTransition(FragmentTransaction.TRANSIT_NONE);
				fragTransaction.commit();
			}

			break;

		case 1:

			if (uniondiffFrag == null
					|| uniondiffFrag.getShownIndex() != position) {
				// Make new fragment to show this selection.
				uniondiffFrag = UnionDifferenceFragment.newInstance(position);

				// Execute a transaction, replacing any existing fragment
				// with this one inside the frame.

				fragTransaction.add(R.id.sample_fragment, uniondiffFrag);

				fragTransaction.setTransition(FragmentTransaction.TRANSIT_NONE);
				fragTransaction.commit();
			}

			break;

		case 2:

			if (spatialrelationFrag == null
					|| spatialrelationFrag.getShownIndex() != position) {
				// Make new fragment to show this selection.
				spatialrelationFrag = SpatialRelationshipsFragment
						.newInstance(position);

				// Execute a transaction, replacing any existing fragment
				// with this one inside the frame.

				fragTransaction.add(R.id.sample_fragment, spatialrelationFrag);

				fragTransaction.setTransition(FragmentTransaction.TRANSIT_NONE);
				fragTransaction.commit();
			}

			break;

		default:
			break;

		}
	}
}