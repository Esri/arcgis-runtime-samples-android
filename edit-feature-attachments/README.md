# Edit feature attachments
The sample demonstrates how you can add, delete or fetch attachments for a specific feature in a feature layer.

![Edit Feature Attachments](edit-feature-attachments.png)

## Features
* ServiceFeatureTable
* FeatureLayer
* FetchAttachmentsAsync
* FetchDataAsync
* DeleteAttachmentAsync
* UpdateFeatureAsync
* ApplyEditsAsync

## How to use the sample
Tapping a feature on the map opens a callout displaying number of attachments. Tap on the ![screen shot 2016-06-08 at 9 47 59 am](https://cloud.githubusercontent.com/assets/12448081/15902683/0b7dbe36-2d5e-11e6-9d11-0b3082f1f3ac.png) **info** to view/edit the attachments. Selecting a list entry downloads the attachment and opens it in gallery to view. Tap on the floating action button **+** to add an attachment or long press to delete.

## Developer Pattern
The map view provides a way to add a listener to screen taps using the ```setOnTouchListener``` method. The app uses the ```MotionEvent``` passed in to the ```onSingleTapConfirmed``` method to identify features on mapview based on the tolerance. ```fetchAttachmentsAsync``` method on selected feature returns a ```List<Attachment>``` list of attachments. To download an attachment ```fetchDataAsync``` method returns an ```InputStream``` which is used to download the attachment as a drawable before it can be converted to Bitmap. 

```java
// create a drawable from InputStream
Drawable d = Drawable.createFromStream(inputStream, fileName);
// create a bitmap from drawable
Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
```

To add/delete an attachment, ```addAttachmentAsync``` and ```deleteAttachmentAsync``` methods are used. A listener is created to update the **ServiceFeatureTable** after finishing the async tasks of add/delete. The ```updateFeatureAsync``` updates the feature table and ```applyEditsAsync``` method is then called on the service table to apply the local changes on the server asynchronously.
