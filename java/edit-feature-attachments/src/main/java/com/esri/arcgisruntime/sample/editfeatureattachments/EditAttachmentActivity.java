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

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class EditAttachmentActivity extends AppCompatActivity {

  private static final String TAG = EditAttachmentActivity.class.getSimpleName();

  private static final int RESULT_LOAD_IMAGE = 1;
  private CustomList adapter;
  private List<Attachment> attachments;
  private ArcGISFeature mSelectedArcGISFeature;
  private ServiceFeatureTable mServiceFeatureTable;
  private String mAttributeID;
  private ListView listView;
  private ArrayList<String> attachmentList = new ArrayList<>();
  private ProgressDialog progressDialog;
  private AlertDialog.Builder builder;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.attachments);

    Bundle bundle = getIntent().getExtras();
    String s = bundle.getString(getString(R.string.attribute));
    int noOfAttachments = bundle.getInt(getApplication().getString(R.string.noOfAttachments));

    // Build a alert dialog with specified style
    builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);

    // get a reference to the floating action button
    FloatingActionButton addAttachmentFab = findViewById(R.id.addAttachmentFAB);

    // select an image to upload as an attachment
    addAttachmentFab.setOnClickListener(v -> selectAttachment());

    mServiceFeatureTable = new ServiceFeatureTable(getResources().getString(R.string.sample_service_url));

    progressDialog = new ProgressDialog(this);

    // display progress dialog if selected feature has attachments
    if (noOfAttachments != 0) {
      progressDialog.setTitle(getApplication().getString(R.string.fetching_attachments));
      progressDialog.setMessage(getApplication().getString(R.string.wait));
      progressDialog.show();
    } else {
      Toast.makeText(this, getString(R.string.empty_attachment_message), Toast.LENGTH_LONG).show();
    }

    // get a reference to the list view
    listView = findViewById(R.id.listView);
    // create custom adapter
    adapter = new CustomList(this, attachmentList);
    // set custom adapter on the list
    listView.setAdapter(adapter);
    fetchAttachmentsFromServer(s);

    // listener on attachment items to download the attachment
    listView.setOnItemClickListener((parent, view, position, id) -> fetchAttachmentAsync(position));

    // set on long click listener to delete the attachment
    listView.setOnItemLongClickListener((parent, view, position, id) -> {
      builder.setMessage(getApplication().getString(R.string.delete_query));
      builder.setCancelable(true);
      builder.setPositiveButton(getResources().getString(R.string.yes), (dialog, which) -> {
        deleteAttachment(position);
        dialog.dismiss();
      });
      builder.setNegativeButton(getResources().getString(R.string.no), (dialog, which) -> dialog.cancel());
      AlertDialog alert = builder.create();
      alert.show();
      return true;
    });
  }

  private void fetchAttachmentAsync(final int position) {

    progressDialog.setTitle(getApplication().getString(R.string.downloading_attachments));
    progressDialog.setMessage(getApplication().getString(R.string.wait));
    progressDialog.show();

    // create a listenableFuture to fetch the attachment asynchronously
    final ListenableFuture<InputStream> fetchDataFuture = attachments.get(position).fetchDataAsync();
    fetchDataFuture.addDoneListener(() -> {
      try {
        String fileName = attachmentList.get(position);
        // create a drawable from InputStream
        Drawable d = Drawable.createFromStream(fetchDataFuture.get(), fileName);
        // create a bitmap from drawable
        Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
        File fileDir = new File(getExternalFilesDir(null) + "/ArcGIS/Attachments");
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
        i.setAction(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.fromFile(file), "image/png");
        startActivity(i);

      } catch (Exception e) {
        Log.d(TAG, e.toString());
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

    deleteResult.addDoneListener(() -> {
      ListenableFuture<Void> tableResult = mServiceFeatureTable.updateFeatureAsync(mSelectedArcGISFeature);
      // apply changes back to the server
      tableResult.addDoneListener(this::applyServerEdits);
    });
  }

  /**
   * Asynchronously fetch the given feature's attachments and show them a list view.
   *
   * @param objectID of the feature from which to fetch attachments
   */
  private void fetchAttachmentsFromServer(String objectID) {
    attachmentList = new ArrayList<>();
    // create objects required to do a selection with a query
    QueryParameters query = new QueryParameters();
    // set the where clause of the query
    query.setWhereClause("OBJECTID = " + objectID);

    // query the feature table
    final ListenableFuture<FeatureQueryResult> featureQueryResultFuture = mServiceFeatureTable
        .queryFeaturesAsync(query);

    featureQueryResultFuture.addDoneListener(() -> {
      try {
        FeatureQueryResult result = featureQueryResultFuture.get();
        Feature feature = result.iterator().next();
        mSelectedArcGISFeature = (ArcGISFeature) feature;
        // get the number of attachments
        final ListenableFuture<List<Attachment>> attachmentResults = mSelectedArcGISFeature.fetchAttachmentsAsync();
        attachmentResults.addDoneListener(() -> {
          try {
            attachments = attachmentResults.get();
            // if selected feature has attachments, display them in a list fashion
            if (!attachments.isEmpty()) {
              for (Attachment attachment : attachments) {
                attachmentList.add(attachment.getName());
              }
              runOnUiThread(() -> {
                if (progressDialog.isShowing()) {
                  progressDialog.dismiss();
                }
                adapter = new CustomList(this, attachmentList);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
              });
            }
          } catch (Exception e) {
            String error = "Error getting attachment: " + e.getMessage();
            Log.e(TAG, error);
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
          }
        });
      } catch (Exception e) {
        String error = "Error getting feature query result: " + e.getMessage();
        Log.e(TAG, error);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
      }
    });
  }

  private void selectAttachment() {
    Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
      try {
        InputStream imageInputStream = getContentResolver().openInputStream(selectedImage);

        byte[] imageBytes = bytesFromInputStream(imageInputStream);

        final String attachmentName = getString(R.string.attachment) + '_' + System.currentTimeMillis() + ".png";

        progressDialog.setTitle(getApplication().getString(R.string.apply_edit_message));
        progressDialog.setMessage(getApplication().getString(R.string.wait));
        progressDialog.show();

        ListenableFuture<Attachment> addResult = mSelectedArcGISFeature
            .addAttachmentAsync(imageBytes, "image/png", attachmentName);

        addResult.addDoneListener(() -> {
          final ListenableFuture<Void> tableResult = mServiceFeatureTable.updateFeatureAsync(mSelectedArcGISFeature);
          tableResult.addDoneListener(this::applyServerEdits);
        });

      } catch (IOException e) {
        String error = "Error converting image to byte array: " + e.getMessage();
        Log.e(TAG, error);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
      }
    }
  }

  /**
   * Applies changes from a Service Feature Table to the server.
   */
  private void applyServerEdits() {
    try {
      // apply edits to the server
      final ListenableFuture<List<FeatureEditResult>> updatedServerResult = mServiceFeatureTable.applyEditsAsync();
      updatedServerResult.addDoneListener(() -> {
        try {
          List<FeatureEditResult> edits = updatedServerResult.get();
          // check that the feature table was successfully updated
          if (!edits.isEmpty()) {
              if (progressDialog.isShowing()) {
                progressDialog.dismiss();
              }
              mAttributeID = mSelectedArcGISFeature.getAttributes().get("objectid").toString();
              fetchAttachmentsFromServer(mAttributeID);
              // update the attachment list view on the control panel
              Toast.makeText(this, getString(R.string.success_message), Toast.LENGTH_SHORT).show();
          } else {
            Toast.makeText(this, getString(R.string.failure_edit_results), Toast.LENGTH_SHORT).show();
          }
        } catch (Exception e) {
          String error = "Error getting feature edit result: " + e.getMessage();
          Log.e(TAG, error);
          Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
        }
      });
    } catch (Exception e) {
      String error = "Error applying edits to server: " + e.getMessage();
      Log.e(TAG, error);
      Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
    }
  }

  /**
   * Converts the given input stream into a byte array.
   *
   * @param inputStream from an image
   * @return an array of bytes from the input stream
   * @throws IOException if input stream can't be read
   */
  private static byte[] bytesFromInputStream(InputStream inputStream) throws IOException {
    try (ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream()) {
      int bufferSize = 1024;
      byte[] buffer = new byte[bufferSize];
      int len;
      while ((len = inputStream.read(buffer)) != -1) {
        byteBuffer.write(buffer, 0, len);
      }
      return byteBuffer.toByteArray();
    }
  }

  /**
   * Send the updated attachment count back to MainActivity and finish the current Activity
   */
  @Override
  public void onBackPressed() {
    Intent intent = new Intent();
    intent.putExtra(getApplication().getString(R.string.noOfAttachments), attachmentList.size());
    setResult(RESULT_OK, intent);
    finish();
    super.onBackPressed();
  }
}
