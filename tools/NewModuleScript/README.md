
# New Module Script

This script creates a new sample and configures it as a new module in Android Studio. The Intellij project creates an `.jar` artifact which is used to create new samples. 

## How the script works

The script uses the sample `Display Map` as a template to create a new sample. Here is the breakdown of the process of the `.jar` executable: 

 - Sets up the path of the repository and the sample names used in the sample's package directory
 - Creates the necessary files and folders using the template. Example: `build.gradle`, `src/`, `libs/`, `README.metadata.json`,  `README.md` and `proguard-rules.pro`
 - Removes unwanted files like `display-map.png` and `build/` directory
 - Creates a blank `README.metadata.json` file
 - Updates the sample content to reflect the name of the sample. Example: `README.md` header, the build.gradle's  `applicationId`, the package name in `AndroidManifest.xml`, the `app_name` in `strings.xml`, the copyright year in `MainActivity.kt` and so on.
 - Adds the sample module name to `settings.gradle`. The script adds the module name to the end of the gradle file.
 

## How to use the script

Refer to the `README.md` at `arcgis-runtime-samples-android/tools/`
