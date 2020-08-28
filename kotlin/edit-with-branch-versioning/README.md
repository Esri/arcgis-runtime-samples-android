# Edit with branch versioning

Create, query and edit a specific server version using service geodatabase.

![Image of edit with branch versioning](edit-with-branch-versioning.png)

## Use case

Workflows often progress in discrete stages, with each stage requiring the allocation of a different set of resources and business rules. Typically, each stage in the overall process represents a single unit of work, such as a work order or job. To manage these, you can create a separate, isolated version and modify it. Once this work is complete, you can integrate the changes into the default version.

## How to use the sample

Upon opening the sample, you will be prompted to enter credentials for the service(un: editor01/pwd: editor01.password). Once loaded, the map will zoom to the extent of the feature layer. The current version is indicated at the top of the map. Tap "Create Version" to open a dialog to specify the version information (name, access, and description). The version name must meet the following criteria:
1. Must not exceed 62 characters
2. Must not include: Period (.), Semicolon (;), Single quotation mark ('), Double quotation mark (")
3. Must not include a space for the first character

* Note - the version name will have the username and a period (.) prepended to it. E.g "editor01.MyNewUniqueVersionName"

Then tap "Create" to create the version with the information that you specified. Select a feature to open a dialog, from which you can edit the feature attribute and tap confirm; or tap "Edit location" and tap again on the map to relocate the point.

Tap the button in the bottom right corner to switch back and forth between the version you created and the default version.

## How it works

1. Create and load a `ServiceGeodatabase` with a feature service URL that has enabled Version Management.
2. Get the `ServiceFeatureTable` from the service geodatabase.
3. Create a `FeatureLayer` from the service feature table.
4. Create `ServiceVersionParameters` with a unique name, `AccessVersion`, and description.
    * Note - See the additional information section for more restrictions on the version name.
5. Create a new version calling `serviceGeodatabase.createVersionAsync(...)` passing in the service version parameters.
6. Listen to the returned `ListenableFuture<ServiceVersionInfo>` to obtain the `ServiceVersionInfo` of the version created.
7. Switch to the version you have just created using `serviceGeodatabase.switchVersionAsync(...)`, passing in the version name obtained from the service version info from *step 6*.
8. Select a `Feature` from the map and edit it's "TYPDAMAGE" attribute from the options listed in the combo box.
9. Tap on the map to relocate the feature.
10. Apply these edits to your version by calling `serviceGeodatabase.applyEditsAsync()`.
11. Switch back and forth between your version and the default version to see how the two versions differ.

## Relevant API

* FeatureLayer
* ServiceFeatureTable
* ServiceGedodatabase
* ServiceGeodatabase.applyEditsAsync()
* ServiceGeodatabase.createVersionAsync()
* ServiceGeodatabase.switchVersionAsync()
* ServiceVersionInfo
* ServiceVersionParameters
* VersionAccess

## About the data

The feature layer used in this sample is [Damage to commercial buildings](https://sampleserver7.arcgisonline.com/arcgis/rest/services/DamageAssessment/FeatureServer/0) located in Naperville, Illinois.

## Additional information

The name of the version must meet the following criteria:
1. Must not exceed 62 characters
2. Must not include: Period (.), Semicolon (;), Single quotation mark ('), Double quotation mark (")
3. Must not include a space for the first character

Branch versioning access permission:
1. VersionAccess.PUBLIC - Any portal user can view and edit the version.
2. VersionAccess.PROTECTED - Any portal user can view, but only the version owner, feature layer owner, and portal administrator can edit the version.
3. VersionAccess.PRIVATE - Only the version owner, feature layer owner, and portal administrator can view and edit the version.

## Tags

branch versioning, edit, version control, version management server
