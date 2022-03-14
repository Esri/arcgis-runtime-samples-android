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

package com.esri.arcgisruntime.sample.manageoperationallayers;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.mapping.LayerList;
import com.esri.arcgisruntime.sample.listviewdragginganimation.DynamicListView;
import com.esri.arcgisruntime.sample.listviewdragginganimation.StableArrayAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class OperationalLayers extends AppCompatActivity {

    private ArrayList<String> mAddedLayerList, mRemovedLayerList;
    private LayerList mMapOperationalLayers;
    private final ArrayList<Layer> mRemovedLayers = new ArrayList<>();
    private StableArrayAdapter mOperationalLayerAdapter;
    private ArrayAdapter<String> mRemovedLayerAdapter;
    private int mOperationalLayersListViewId;
    private int mRemovedLayerListViewId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operational_layers);

        // inflate Button from the layout
        Button doneButton = findViewById(R.id.donebutton);

        // listviewids to reuse one dialogbox between the two listview onItemClickListeners
        mOperationalLayersListViewId = getResources().getIdentifier("dynamiclistview", "id", this.getPackageName());
        mRemovedLayerListViewId = getResources().getIdentifier("listView", "id", this.getPackageName());

        // get the string array with the layer names from the strings resource file
        String[] layers = getResources().getStringArray(R.array.manage_operational_layer_array);

        // initialize the addedLayerList array with the names of the layer added initially to the Map
        mAddedLayerList = new ArrayList<>(Arrays.asList(layers));
        // removed layer list is initially blank
        mRemovedLayerList = new ArrayList<>();

        // get the map operational layers LayerList from the MainActivity
        mMapOperationalLayers = MainActivity.getOperationalLayerList();
        // the sample maintains an arraylist with the Layer objects that have been removed from the Map,
        // so that they can be re-added to the map

        // initialize the adapter for the list of layer added to the Map
        mOperationalLayerAdapter = new StableArrayAdapter(this, R.layout.text_view, mAddedLayerList);
        // inflate the operationalLayers listview
        DynamicListView operationalLayersListView = findViewById(R.id.dynamiclistview);

        operationalLayersListView.setLayerList(mAddedLayerList);
        operationalLayersListView.setAdapter(mOperationalLayerAdapter);
        operationalLayersListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // initialize the adapter for the list of removed layers
        mRemovedLayerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mRemovedLayerList);

        // inflate the removedlayers listview
        ListView removedLayerListView = findViewById(R.id.listView);
        removedLayerListView.setAdapter(mRemovedLayerAdapter);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if there are two layers present in the added layers list
                // handle the cell swaping
                if (mAddedLayerList.size() > 1) {
                    // if the first item on the list it world elevations make sure that the
                    // same layer is present in the LayerList of the Map
                    if (mAddedLayerList.get(0).equals("World Elevations")) {
                        if (!mMapOperationalLayers.get(0).getName().equals("WorldElevations")) {
                            // if not then swap the layer positons
                            Layer temp = mMapOperationalLayers.remove(0);
                            mMapOperationalLayers.add(1, temp);
                        }
                    } else {
                        if (!mMapOperationalLayers.get(0).getName().equals("Census")) {
                            Layer temp = mMapOperationalLayers.remove(0);
                            mMapOperationalLayers.add(1, temp);
                        }
                    }
                }

                // with the user selected options applied go back to the mainActivity
                Intent intent = new Intent(OperationalLayers.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);

            }
        });

        // if the layer in the Added layers ListView is clicked on, handle it
        operationalLayersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showDialog(mOperationalLayersListViewId, position, "This layer will be removed, confirm?");
            }
        });

        // if the layer in the removed layers ListView is clicked on, handle it
        removedLayerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showDialog(mRemovedLayerListViewId, position, "This layer will be added, confirm?");
            }
        });

    }

    /**
     * removes the layer in the added layers list, and adds it to the removed layers list
     * both from the view and the LayerList
     *
     * @param position of the layer to be removed in the removedLayerList
     */
    private void removeLayer(int position) {
        mRemovedLayerList.add(mAddedLayerList.get(position));
        mRemovedLayers.add(mMapOperationalLayers.remove(position));
        mAddedLayerList.remove(position);

        mRemovedLayerAdapter.notifyDataSetChanged();
        mOperationalLayerAdapter.notifyDataSetChanged();

    }

    /**
     * adds the layer to the added layers list, and removes it from the removed layers list
     *
     * @param position of the layer to be added in the addedLayerList
     */
    private void addLayer(int position) {
        mAddedLayerList.add(mRemovedLayerList.get(position));
        mMapOperationalLayers.add(mRemovedLayers.remove(position));
        mRemovedLayerList.remove(position);

        mRemovedLayerAdapter.notifyDataSetChanged();
        mOperationalLayerAdapter.notifyDataSetChanged();
    }


    /**
     * shows the dialog box
     *
     * @param listViewId to determine which ListView the user clicked on
     * @param position   position of the item in the List
     * @param message    title of the dialog box
     */
    private void showDialog(final int listViewId, final int position, String message) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                OperationalLayers.this);

        // set title
        alertDialogBuilder.setTitle("manage-operational-layers");

        // set dialog message
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if the list view id is for OperationalLayersListView then
                        // remove the layer
                        if (listViewId == mOperationalLayersListViewId) {
                            removeLayer(position);
                        } else {
                            // add the layer
                            addLayer(position);
                        }


                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }

}
