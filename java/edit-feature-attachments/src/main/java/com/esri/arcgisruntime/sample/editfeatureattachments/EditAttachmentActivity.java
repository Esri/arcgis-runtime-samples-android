/* Copyright 2016 Esri
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

package com.esri.arcgisruntime.sample.editfeatureattachments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.Attachment;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.sample.arrayadapter.CustomList;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class EditAttachmentActivity extends AppCompatActivity {

    private static final String TAG = "EditAttachmentActivity";
    private static final int RESULT_LOAD_IMAGE = 1;
    private CustomList adapter;
    private int noOfAttachments;
    private FloatingActionButton addAttachmentFab;
    private final int requestCodeFolder = 2;
    private final int requestCodeGallery = 3;
    private final String[] permission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private List<Attachment> attachments;
    private ArcGISFeature mSelectedArcGISFeature;
    private ServiceFeatureTable mServiceFeatureTable;
    private String mAttributeID;
    private ListView list;
    private ArrayList<String> attachmentList = new ArrayList<>();
    private ProgressDialog progressDialog;
    private AlertDialog.Builder builder;
    private boolean permissionsGranted = false;
    private int listPosition;
    private View listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attachments);

        Bundle bundle = getIntent().getExtras();
        String s = bundle.getString(getString(R.string.attribute));
        noOfAttachments = bundle.getInt(getApplication().getString(R.string.noOfAttachments));

        // Build a alert dialog with specified style
        builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);

        // inflate the floating action button
        addAttachmentFab = (FloatingActionButton) findViewById(R.id.addAttachmentFAB);

        // select an image to upload as an attachment
        addAttachmentFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!permissionsGranted) {
                    getPermissions(requestCodeGallery);
                } else {
                    selectAttachment();
                }

            }
        });

        mServiceFeatureTable = new ServiceFeatureTable(getResources().getString(R.string.sample_service_url));

        progressDialog = new ProgressDialog(this);

        // display progress dialog if selected feature has attachments
        if (noOfAttachments != 0) {
            progressDialog.setTitle(getApplication().getString(R.string.fetching_attachments));
            progressDialog.setMessage(getApplication().getString(R.string.wait));

            progressDialog.show();
        } else {
            Toast.makeText(EditAttachmentActivity.this, getApplication().getString(R.string.empty_attachment_message), Toast.LENGTH_LONG).show();
        }

        // inflate the list view
        list = (ListView) findViewById(R.id.listView);
        // create custom adapter
        adapter = new CustomList(EditAttachmentActivity.this, attachmentList);
        // set custom adapter on the list
        list.setAdapter(adapter);
        fetchAttachmentsFromServer(s);

        // listener on attachment items to download the attachment
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {

                listPosition = position;
                listView = view;
                if (!permissionsGranted) {
                    getPermissions(requestCodeFolder);
                } else {
                    fetchAttachmentAsync(position);
                }
            }
        });


        //set on long click listener to delete the attachment
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int pos, long id) {

                builder.setMessage(getApplication().getString(R.string.delete_query));
                builder.setCancelable(true);

                builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteAttachment(pos);
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }
        });
    }

    private void fetchAttachmentAsync(final int position) {

        progressDialog.setTitle(getApplication().getString(R.string.downloading_attachments));
        progressDialog.setMessage(getApplication().getString(R.string.wait));
        progressDialog.show();

        // create a listenableFuture to fetch the attachment asynchronously
        final ListenableFuture<InputStream> listenableFuture = attachments.get(position).fetchDataAsync();
        listenableFuture.addDoneListener(new Runnable() {
            @Override
            public void run() {
                try {
                    String fileName = attachmentList.get(position);
                    // create a drawable from InputStream
                    Drawable d = Drawable.createFromStream(listenableFuture.get(), fileName);
                    // create a bitmap from drawable
                    Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                    File root = getExternalFilesDir(null);
                    File fileDir = new File(root.getAbsolutePath() + "/ArcGIS/Attachments");
                    // create folder /ArcGIS/Attachments in external storage
                    boolean isDirectoryCreated = fileDir.exists();
                    if (!isDirectoryCreated) {
                        isDirectoryCreated = fileDir.mkdirs();
                    }
                    File file = null;
                    if (isDirectoryCreated) {
                        file = new File(fileDir, fileName);
                        FileOutputStream fos = new FileOutputStream(file);
                        // compress the bitmap to PNG format
                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                        fos.flush();
                        fos.close();
                    }

                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    // open the file in gallery
                    Intent i = new Intent();
                    i.setAction(android.content.Intent.ACTION_VIEW);
                    i.setDataAndType(Uri.fromFile(file), "image/png");
                    startActivity(i);

                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }

            }
        });
    }

    /**
     * Delete the attachment from the feature
     *
     * @param pos position of the attachment in the list view to be deleted
     */
    private void deleteAttachment(int pos) {
        progressDialog.setTitle(getApplication().getString(R.string.deleting_attachments));
        progressDialog.setMessage(getApplication().getString(R.string.wait));
        progressDialog.show();

        ListenableFuture<Void> deleteResult = mSelectedArcGISFeature.deleteAttachmentAsync(attachments.get(pos));
        attachmentList.remove(pos);
        adapter.notifyDataSetChanged();

        deleteResult.addDoneListener(new Runnable() {
            @Override
            public void run() {
                ListenableFuture<Void> tableResult = mServiceFeatureTable.updateFeatureAsync(mSelectedArcGISFeature);
                // apply changes back to the server
                tableResult.addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        applyServerEdits();
                    }
                });
            }
        });
    }


    /**
     * Asynchronously fetch the attachments to view as a list
     *
     * @param objectID
     */
    private void fetchAttachmentsFromServer(String objectID) {
        attachmentList = new ArrayList<>();
        // create objects required to do a selection with a query
        QueryParameters query = new QueryParameters();
        // set the where clause of the query
        query.setWhereClause("OBJECTID = " + objectID);

        // query the feature table
        final ListenableFuture<FeatureQueryResult> future = mServiceFeatureTable.queryFeaturesAsync(query);

        future.addDoneListener(new Runnable() {
            @Override
            public void run() {
                try {
                    FeatureQueryResult result = future.get();
                    Feature feature = result.iterator().next();
                    mSelectedArcGISFeature = (ArcGISFeature) feature;
                    // get the number of attachments
                    final ListenableFuture<List<Attachment>> attachmentResults = mSelectedArcGISFeature.fetchAttachmentsAsync();
                    attachmentResults.addDoneListener(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                attachments = attachmentResults.get();
                                // if selected feature has attachments, display them in a list fashion
                                if (!attachments.isEmpty()) {
                                    //
                                    for (Attachment attachment : attachments) {
                                        attachmentList.add(attachment.getName());
                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            if (progressDialog.isShowing()) {
                                                progressDialog.dismiss();
                                            }
                                            adapter = new CustomList(EditAttachmentActivity.this, attachmentList);
                                            list.setAdapter(adapter);
                                            adapter.notifyDataSetChanged();
                                        }
                                    });

                                }


                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    });

                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        });


    }


    /**
     * Open Gallery to select an image as an attachment
     */

    private void getPermissions(int requestCode) {
        boolean permissionCheck = ContextCompat.checkSelfPermission(EditAttachmentActivity.this, permission[0]) ==
                PackageManager.PERMISSION_GRANTED;

        if (!permissionCheck) {
            // If permissions are not already granted, request permission from the user.
            ActivityCompat.requestPermissions(EditAttachmentActivity.this, permission, requestCode);

        } else {
            permissionsGranted = true;
            if (requestCode == requestCodeGallery) {
                selectAttachment();
            } else {
                fetchAttachmentAsync(listPosition);
            }

        }
    }

    private void selectAttachment() {

        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, RESULT_LOAD_IMAGE);

    }

    /**
     * Upload the selected image from the gallery as an attachment to the selected feature
     *
     * @param requestCode RESULT_LOAD_IMAGE request code to identify the requesting activity
     * @param resultCode  activity result code
     * @param data        Uri of the selected image
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                // covert file to bytes to pass to ArcGISFeature
                byte[] imageByte = new byte[0];
                try {
                    File imageFile = new File(picturePath);
                    imageByte = FileUtils.readFileToByteArray(imageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                final String attachmentName = getApplication().getString(R.string.attachment) + "_" + System.currentTimeMillis() + ".png";

                progressDialog.setTitle(getApplication().getString(R.string.apply_edit_message));
                progressDialog.setMessage(getApplication().getString(R.string.wait));

                progressDialog.show();

                ListenableFuture<Attachment> addResult = mSelectedArcGISFeature.addAttachmentAsync(imageByte, "image/png", attachmentName);

                addResult.addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        final ListenableFuture<Void> tableResult = mServiceFeatureTable.updateFeatureAsync(mSelectedArcGISFeature);
                        tableResult.addDoneListener(new Runnable() {
                            @Override
                            public void run() {
                                applyServerEdits();
                            }
                        });
                    }
                });
            }
        }


    }

    /**
     * Applies changes from a Service Feature Table to the server.
     */
    private void applyServerEdits() {

        try {
            // check that the feature table was successfully updated
            // apply edits to the server
            final ListenableFuture<List<FeatureEditResult>> updatedServerResult = mServiceFeatureTable.applyEditsAsync();
            updatedServerResult.addDoneListener(new Runnable() {

                @Override
                public void run() {


                    try {
                        List<FeatureEditResult> edits = updatedServerResult.get();
                        if (edits.size() > 0) {
                            if (!edits.get(0).hasCompletedWithErrors()) {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                //attachmentList.add(fileName);
                                mAttributeID = mSelectedArcGISFeature.getAttributes().get("objectid").toString();
                                fetchAttachmentsFromServer(mAttributeID);
                                // update the attachment list view on the control panel
                                Toast.makeText(EditAttachmentActivity.this, getApplication().getString(R.string.success_message), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(EditAttachmentActivity.this, getApplication().getString(R.string.failure_message), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(EditAttachmentActivity.this, getApplication().getString(R.string.failure_edit_results), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * send the updated attachment count back to MainActivity and finish the current Activity
     */
    @Override
    public void onBackPressed() {

        Intent intent = new Intent();
        intent.putExtra(getApplication().getString(R.string.noOfAttachments), attachmentList.size());
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Location permission was granted. This would have been triggered in response to failing to start the
            // LocationDisplay, so try starting this again.
            permissionsGranted = true;
            if (requestCode == requestCodeGallery) {
                selectAttachment();
            } else {
                fetchAttachmentAsync(listPosition);
            }

        } else {
            // If permission was denied, show toast to inform user what was chosen. If LocationDisplay is started again,
            // request permission UX will be shown again, option should be shown to allow never showing the UX again.
            // Alternative would be to disable functionality so request is not shown again.
            Toast.makeText(EditAttachmentActivity.this, getResources().getString(R.string.storage_permission_denied), Toast
                    .LENGTH_SHORT).show();
            permissionsGranted = false;

        }
    }
}
