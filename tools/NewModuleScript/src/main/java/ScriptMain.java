import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Scanner;

public class ScriptMain {

    private String sampleName;

    private String sampleWithHyphen;
    private String sampleWithoutSpaces;
    private String samplesRepoPath;

    public static void main(String[] args) {
        ScriptMain mainClassObj = new ScriptMain();
        mainClassObj.run();
    }

    private void run(){

        Scanner scanner = new Scanner(System.in);

        // Get the name of the sample
        System.out.println("Enter Name of the sample with spaces (Eg. \"Display Map\"): ");
        sampleName = scanner.nextLine();

        sampleWithHyphen = sampleName.replace(" ", "-").toLowerCase();
        sampleWithoutSpaces = sampleName.replace(" ", "").toLowerCase();

        samplesRepoPath = Paths.get("").toAbsolutePath().toString().replace("/tools/NewModuleScript","");
        System.out.println("Using repository: "+  samplesRepoPath);

        try{
            createFilesAndFolders();
            deleteUnwantedFiles();
            updateSampleContent();
            updateSettingsGradle();    
        }catch (Exception e){
            exitProgram(e);
        }
        System.out.println("Sample Successfully Created! ");
    }


    /**
     * This function cleans up unwanted files copied
     * when createFilesAndFolders() is called
     */
    private void deleteUnwantedFiles() {
        File buildFolder = new File(samplesRepoPath + "/kotlin/" + sampleWithHyphen + "/build");
        File displayMapKotlinFolder = new File(
                samplesRepoPath + "/kotlin/" + sampleWithHyphen + "/src/main/java/com/esri/arcgisruntime/sample/displaymap");
        File image = new File(samplesRepoPath + "/kotlin/" + sampleWithHyphen + "/display-map.png");
        try {
            FileUtils.deleteDirectory(buildFolder);
            FileUtils.deleteDirectory(displayMapKotlinFolder);
            image.delete();
        } catch (IOException e) {
            exitProgram(e);
            e.printStackTrace();
        }
    }

    /**
     * Creates the files and folders needed for a new sample
     */
    private void createFilesAndFolders() {
        // Create the sample resource folders
        File destinationResDirectory = new File(samplesRepoPath + "/kotlin/" + sampleWithHyphen);
        destinationResDirectory.mkdirs();
        // Display Map's res directory to copy over to new sample
        File sourceResDirectory = new File(samplesRepoPath + "/kotlin/display-map/");

        // Perform copy of the Android res folders from display-map sample.
        try {
            FileUtils.copyDirectory(sourceResDirectory, destinationResDirectory);
        } catch (IOException e) {
            e.printStackTrace();
            exitProgram(e);
        }

        // Create the sample package directory in the source folder
        File packageDirectory = new File(samplesRepoPath + "/kotlin/" + sampleWithHyphen + "/src/main/java/com/esri/arcgisruntime/sample/" + sampleWithoutSpaces);
        if(!packageDirectory.exists()){
            packageDirectory.mkdirs();
        }else{
            exitProgram(new Exception("\"Sample already exists!\""));
        }

        // Copy display-map MainActivity.kt to new sample
        File sourceFile = new File(samplesRepoPath + "/kotlin/" + sampleWithHyphen + "/src/main/java/com/esri/arcgisruntime/sample/displaymap/MainActivity.kt");

        // Perform copy
        try {
            FileUtils.copyFileToDirectory(sourceFile, packageDirectory);
        } catch (IOException e) {
            e.printStackTrace();
            exitProgram(e);
        }
    }

    /**
     * Exits the program with error -1 if it encounters an error
     * @param e Error message to display
     */
    private void exitProgram(Exception e){
        System.out.println("Error creating the sample: " + e.getMessage());
        System.out.println("StackTrace:");
        e.printStackTrace();
        System.exit(-1);
    }

    /**
     * Updates the content in the copied files to reflect the name of the sample
     * Eg. README.md, build.gradle, MainActivity.kt, etc.
     */
    private void updateSampleContent() {

        //Update README.md
        File file = new File(samplesRepoPath + "/kotlin/" + sampleWithHyphen + "/README.md");
        try {
            FileUtils.write(file,"# " + sampleName, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            exitProgram(e);
        }

        //Update README.metadata.json
        file = new File(samplesRepoPath + "/kotlin/" + sampleWithHyphen + "/README.metadata.json");
        try {
            FileUtils.write(file,"{\n}", StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            exitProgram(e);
        }

        //Update build.gradle
        file = new File(samplesRepoPath + "/kotlin/" + sampleWithHyphen + "/build.gradle");
        try {
            String fileContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            fileContent = fileContent.replace("demos.displaymap", "sample." + sampleWithoutSpaces);
            FileUtils.write(file,fileContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            exitProgram(e);
        }

        //Update AndroidManifest.xml
        file = new File(samplesRepoPath + "/kotlin/" + sampleWithHyphen + "/src/main/AndroidManifest.xml");
        try {
            String fileContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            fileContent = fileContent.replace("displaymap", sampleWithoutSpaces);
            FileUtils.write(file,fileContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            exitProgram(e);
        }

        //Update activity_main.xml
        file = new File(samplesRepoPath + "/kotlin/" + sampleWithHyphen + "/src/main/res/layout/activity_main.xml");
        try {
            String fileContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            fileContent = fileContent.replace("displaymap", sampleWithoutSpaces);
            FileUtils.write(file,fileContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            exitProgram(e);
        }

        //Update strings.xml
        file = new File(samplesRepoPath + "/kotlin/" + sampleWithHyphen + "/src/main/res/values/strings.xml");
        try {
            String fileContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            fileContent = fileContent.replace(
                    "<string name=\"app_name\">Display map</string>",
                    "<string name=\"app_name\">" + sampleName +"</string>");
            FileUtils.write(file,fileContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            exitProgram(e);
        }

        //Update MainActivity.kt
        file = new File(samplesRepoPath + "/kotlin/" + sampleWithHyphen + "/src/main/java/com/esri/arcgisruntime/sample/"+sampleWithoutSpaces+"/MainActivity.kt");
        try {
            String fileContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            fileContent = fileContent.replace("Copyright 2017", "Copyright " + Calendar.getInstance().get(Calendar.YEAR));
            fileContent = fileContent.replace("sample.displaymap", "sample." + sampleWithoutSpaces);
            FileUtils.write(file,fileContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            exitProgram(e);
        }
    }

    /**
     * Updates settings.gradle to reflect the new sample.
     */
    private void updateSettingsGradle() {
        File file = new File(samplesRepoPath + "/kotlin/settings.gradle");
        try {
            String fileContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            while(fileContent.charAt(fileContent.length()-1) != '\'')
                fileContent = fileContent.substring(0,fileContent.length()-1);

            String newSampleModified = ",\r\n        ':"+sampleWithHyphen+"'\r\n";

            //Adds the new sample to the end
            fileContent = fileContent + newSampleModified;
            FileUtils.write(file,fileContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            exitProgram(e);
        }
    }


    /**
     * Needed only for debugging purposes
     */
    private void resetProgram() {
        File toDelete = new File(samplesRepoPath + "/kotlin/" + sampleWithHyphen);
        try {
            FileUtils.deleteDirectory(toDelete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}