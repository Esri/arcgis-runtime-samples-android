package com.example.editfeatureattachments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.datasource.Feature;
import com.esri.arcgisruntime.datasource.FeatureQueryResult;
import com.esri.arcgisruntime.datasource.QueryParameters;
import com.esri.arcgisruntime.datasource.arcgis.ArcGISFeature;
import com.esri.arcgisruntime.datasource.arcgis.Attachment;
import com.esri.arcgisruntime.datasource.arcgis.FeatureEditResult;
import com.esri.arcgisruntime.datasource.arcgis.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class EditAttachmentActivity extends AppCompatActivity {

    private static String TAG = "EditAttachmentActivity";
    private static int RESULT_LOAD_IMAGE = 1;
    CustomList adapter;
    private List<Attachment> attachments;
    private Attachment[] arr;
    private FeatureLayer mFeatureLayer;
    private ArcGISFeature mSelectedArcGISFeature;
    private android.graphics.Point mClickPoint;
    private ServiceFeatureTable mServiceFeatureTable;
    private String mSelectedArcGISFeatureAttributeValue;
    private String mAttributeID;
    private ListView list;
    private ArrayList<String> attachmentList = new ArrayList<>();
    private ProgressDialog progressDialog;
    private int noOfAttachments = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attachments_listview);



        Bundle bundle = getIntent().getExtras();
        String s = bundle.getString(getApplication().getString(R.string.attribute));
        noOfAttachments = bundle.getInt(getApplication().getString(R.string.noOfAttachments));
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);

        Log.d(TAG, s);

        mServiceFeatureTable = new ServiceFeatureTable(getResources().getString(R.string.sample_service_url));
        // create the feature layer using the service feature table
        mFeatureLayer = new FeatureLayer(mServiceFeatureTable);

        progressDialog = new ProgressDialog(this);

        if(!(noOfAttachments == 0)) {
            progressDialog.setTitle("Fetching Attachments");
            progressDialog.setMessage("Please wait!!");

            progressDialog.show();
        } else {
            Toast.makeText(EditAttachmentActivity.this, "Looks like the feature doesn't have any attachments \n Click + to add attachments", Toast.LENGTH_LONG).show();
        }

        list = (ListView) findViewById(R.id.listview);

        adapter = new CustomList(EditAttachmentActivity.this, attachmentList);
        fetchAttachments(s);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                progressDialog.setTitle("Downloading Attachments");
                progressDialog.setMessage("Please wait!!");

                progressDialog.show();


                final ListenableFuture<InputStream> listenableFuture = attachments.get(position).fetchDataAsync();
                listenableFuture.addDoneListener(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            String fileName = attachmentList.get(position);
                            Drawable d = Drawable.createFromStream(listenableFuture.get(), fileName);

                            Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                            File root = null;
                            root = Environment.getExternalStorageDirectory();
                            File fileDir = new File(root.getAbsolutePath() + "/ArcGIS/Attachments");
                            fileDir.mkdirs();
                            File file = new File(fileDir, fileName);
                            FileOutputStream fos = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);

                            Log.d(TAG + "-bitmap", bitmap.getByteCount() + "");

                            fos.flush();
                            fos.close();

                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            // open the file in gallery
                            Intent i = new Intent();
                            i.setAction(android.content.Intent.ACTION_VIEW);
                            i.setDataAndType(Uri.fromFile(file), "image/png");
                            startActivity(i);


                        } catch (Exception e) {
                            Log.d(TAG + "-image-", e.toString());
                        }

                    }
                });
            }
        });

        //set onlong click listener to delete the attachment
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int pos, long id) {
                Log.v("long clicked","pos: " + pos);


                builder.setMessage("Are you sure you want to delete the attachment?");
                builder.setCancelable(true);

                builder.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteAttachment(pos);
                                dialog.dismiss();
                            }
                        });

                builder.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
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

    private void deleteAttachment(int pos) {
        progressDialog.setTitle("Deleting attachment");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        ListenableFuture<Void> deleteResult = mSelectedArcGISFeature.deleteAttachmentAsync(attachments.get(pos));

        deleteResult.addDoneListener(new Runnable() {
            @Override
            public void run() {
                ListenableFuture<Void> tableResult = mServiceFeatureTable.updateFeatureAsync(mSelectedArcGISFeature);
                tableResult.addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        applyServerEdits();
                    }
                });
            }
        });
    }

    private void fetchAttachments(String objectID) {
        // create objects required to do a selection with a query
        QueryParameters query = new QueryParameters();
        //make search case insensitive
        query.setWhereClause("OBJECTID = " + objectID);
        // call select features

        attachmentList = new ArrayList<>();
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

                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        if (progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }

                                        adapter = new CustomList(EditAttachmentActivity.this, attachmentList);
                                        list.setAdapter(adapter);

                                        adapter.refreshEvents();
                                        adapter.notifyDataSetChanged();
                                    }
                                });

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_attachment, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle menu item selection
        //if-else is used because this sample is used elsewhere as a Library module
        int itemId = item.getItemId();
        if (itemId == R.id.Add_Attachment) {
            selectAttachment();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Open Gallery to select an image as an attachment
     */
    private void selectAttachment() {
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            File imageFile = new File(picturePath);

            final String attachmentName = getApplication().getString(R.string.attachment) + "_" + System.currentTimeMillis() + ".png";

            progressDialog.setTitle("Applying Edits on Server");
            progressDialog.setMessage("Please wait!!");

            progressDialog.show();

            ListenableFuture<Attachment> addResult = mSelectedArcGISFeature.addAttachmentAsync(imageFile, "image/png", attachmentName);

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
                                fetchAttachments(mAttributeID);
                                // update the attachment list view on the control panel
                                Toast.makeText(EditAttachmentActivity.this, "Feature edited successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                System.out.println(
                                        "Server Error: Failed updating feature attachment to the server.");
                            }
                        } else {
                            System.out.println("Server did not return edit results");
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
