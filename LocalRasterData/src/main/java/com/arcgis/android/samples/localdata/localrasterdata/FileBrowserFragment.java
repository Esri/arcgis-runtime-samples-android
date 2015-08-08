/*
 * Portions of this code is based on code from
 * https://github.com/mburman/Android-File-Explore and contributions from
 * Sugan Krishnan (https://github.com/rgksugan)
 * under Apache2 license.  Modifications have made to use fragment
 * instead of activity and added more features to the file browser.
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

package com.arcgis.android.samples.localdata.localrasterdata;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileBrowserFragment extends Fragment {

  // Stores names of traversed directories
  ArrayList<String> pathDirsList = new ArrayList<String>();

  private List<Item> fileList = new ArrayList<Item>();
  private File path = null;
  private String chosenFile;

  ArrayAdapter<Item> adapter;

  private boolean mShowHiddenFilesAndDirs = true;
  private boolean mDirectoryShownIsEmpty = false;
  private String mFilterFileExtension = null;
  private String mRequestedStartDir = null;
  private OnFileAndFolderFinishListener mFinishListener;

  // Action constants
  static final int SELECT_DIRECTORY = 1; // default action
  static final int SELECT_FILE = 2;
  private int mCurrentAction = SELECT_FILE;

  public static FileBrowserFragment newInstance() {
    return  new FileBrowserFragment();
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    applyInitialDirectory();
    parseDirectoryPath();
    loadFileList();
    this.createFileListAdapter();
    this.initializeFileListView();
    updateCurrentDirectoryTextView();

    mFinishListener = (OnFileAndFolderFinishListener) getActivity();
    // Fragment wants to add menu to action bar
    setHasOptionsMenu(true);
    getActivity().invalidateOptionsMenu();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.filebrowser_layout, null);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.menu_dir_select:
      returnDirectoryFinishActivity();
      return true;
    case R.id.menu_dir_up:
      loadDirectoryUp();
      loadFileList();
      adapter.notifyDataSetChanged();
      updateCurrentDirectoryTextView();
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    for (int i = 0; i < menu.size(); i++) {
      MenuItem item = menu.getItem(i);
      if ((item.getItemId() == R.id.menu_layer_raster) || (item.getItemId() == R.id.menu_renderer)
          || (item.getItemId() == R.id.menu_dir_select)) {
        item.setVisible(false);
      } else if ((item.getItemId() == R.id.menu_dir_up)) {
        item.setVisible(true);
      }
    }
  }

  public void setInitialDirectory(String dir) {
    this.mRequestedStartDir = dir;
  }

  private void applyInitialDirectory() {
    if (mRequestedStartDir != null && mRequestedStartDir.length() > 0) {
      File tempFile = new File(mRequestedStartDir);
      if (tempFile.isDirectory())
        this.path = tempFile;
    }

    if (this.path == null) {
      // No or invalid directory supplied in intent
      if (Environment.getExternalStorageDirectory().isDirectory()
          && Environment.getExternalStorageDirectory().canRead())
        path = Environment.getExternalStorageDirectory();
      else
        path = new File("/");
    }
  }

  private void parseDirectoryPath() {
    pathDirsList.clear();
    String pathString = path.getAbsolutePath();
    String[] parts = pathString.split("/");
    int i = 0;
    while (i < parts.length) {
      pathDirsList.add(parts[i]);
      i++;
    }
  }

  private void loadDirectoryUp() {
    // present directory removed from list
    String s = pathDirsList.remove(pathDirsList.size() - 1);
    // path modified to exclude present directory
    path = new File(path.toString().substring(0, path.toString().lastIndexOf(s)));
    fileList.clear();
  }

  private void updateCurrentDirectoryTextView() {
    int i = 0;
    String curDirString = "";
    while (i < pathDirsList.size()) {
      curDirString += pathDirsList.get(i) + "/";
      i++;
    }
    if (pathDirsList.size() == 0) {
      curDirString = "/";
    }

    ((TextView) getView().findViewById(R.id.currentDirectoryTextView)).setText("Current directory: " + curDirString);
  }

  private void showToast(String message) {
    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
  }

  private void initializeFileListView() {
    ListView lView = (ListView) (getView().findViewById(R.id.fileListView));
    lView.setBackgroundColor(Color.LTGRAY);
    LinearLayout.LayoutParams lParam = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
        LayoutParams.MATCH_PARENT);
    lParam.setMargins(15, 5, 15, 5);
    lView.setAdapter(this.adapter);
    lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        chosenFile = fileList.get(position).file;
        final File sel = new File(path + "/" + chosenFile);
        if (sel.isDirectory()) {
          // Directory
          if (sel.canRead()) {
            // Adds chosen directory to list
            pathDirsList.add(chosenFile);
            path = new File(sel + "");
            loadFileList();
            adapter.notifyDataSetChanged();
            updateCurrentDirectoryTextView();
          } else {
            showToast("Path does not exist or cannot be read");
          }
        } else {
          // File picked or an empty directory message clicked
          if (!mDirectoryShownIsEmpty) {
            // show a popup menu to allow users to open a raster layer for
            // different purpose including basemap layer, operational layer,
            // elevation data source for BlendRenderer, or some combinations.
            PopupMenu popupMenu = new PopupMenu(getActivity(), view);
            popupMenu.inflate(R.menu.file_browser_popup_menu);
            popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

              @Override
              public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                case R.id.menu_raster_base_layer:
                  returnFileFinishActivity(sel.getAbsolutePath(), RasterLayerAction.BASEMAP_LAYER);
                  break;
                case R.id.menu_raster_operational_layer:
                  returnFileFinishActivity(sel.getAbsolutePath(), RasterLayerAction.OPERATIONAL_LAYER);
                  break;
                case R.id.menu_raster_elevation_source:
                  returnFileFinishActivity(sel.getAbsolutePath(), RasterLayerAction.ELEVATION_SOURCE);
                  break;
                case R.id.menu_raster_base_elevation:
                  returnFileFinishActivity(sel.getAbsolutePath(), RasterLayerAction.BASEMAP_LAYER_AND_ELEVATION_SOURCE);
                  break;
                case R.id.menu_raster_operational_elevation:
                  returnFileFinishActivity(sel.getAbsolutePath(), RasterLayerAction.OPERATIONAL_LAYER_AND_ELEVATION_SOURCE);
                  break;
                }
                return true;
              }
            });
            popupMenu.show();
          }
        }
      }
    });

  }

  private void returnDirectoryFinishActivity() {
    mFinishListener.onDirectoryFinish(path.getAbsolutePath());
  }

  private void returnFileFinishActivity(String filePath, RasterLayerAction action) {
    mFinishListener.onFileFinish(filePath, action);
  }

  private void loadFileList() {
    fileList.clear();
    if (path.exists() && path.canRead()) {
      FilenameFilter filter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String filename) {
          File sel = new File(dir, filename);
          boolean showReadableFile = mShowHiddenFilesAndDirs || sel.canRead();
          // Filters based on whether the file is hidden or not
          if (mCurrentAction == SELECT_DIRECTORY) {
            return (sel.isDirectory() && showReadableFile);
          }
          if (mCurrentAction == SELECT_FILE) {
            // If it is a file check the extension if provided
            if (sel.isFile() && mFilterFileExtension != null) {
              return (showReadableFile && sel.getName().endsWith(mFilterFileExtension));
            }
            return (showReadableFile);
          }
          return true;
        }
      };

      String[] fList = path.list(filter);
      this.mDirectoryShownIsEmpty = false;
      for (int i = 0; i < fList.length; i++) {
        // Convert into file path
        File sel = new File(path, fList[i]);
        int drawableID = R.drawable.file_icon;
        boolean canRead = sel.canRead();
        // Set drawables
        if (sel.isDirectory()) {
          if (canRead) {
            drawableID = R.drawable.folder_icon;
          } else {
            drawableID = R.drawable.folder_icon_light;
          }
        }
        fileList.add(i, new Item(fList[i], drawableID));
      }

      if (fileList.size() == 0) {
        this.mDirectoryShownIsEmpty = true;
        fileList.add(0, new Item("Directory is empty", -1));
      } else {
        Collections.sort(fileList, new ItemFileNameComparator());
      }
    } else {
      Toast.makeText(getActivity(),
          "path does not exist or cannot be read", Toast.LENGTH_SHORT).show();
    }
  }

  private void createFileListAdapter() {
    adapter = new ArrayAdapter<Item>(getActivity(), android.R.layout.select_dialog_item, android.R.id.text1, fileList) {
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        // creates view
        View view = super.getView(position, convertView, parent);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        // put the image on the text view
        int drawableID = 0;
        if (fileList.get(position).icon != -1) {
          // If icon == -1, then directory is empty
          drawableID = fileList.get(position).icon;
        }
        textView.setCompoundDrawablesWithIntrinsicBounds(drawableID, 0, 0, 0);

        textView.setEllipsize(null);

        int dp3 = (int) (3 * getResources().getDisplayMetrics().density + 0.5f);
        textView.setCompoundDrawablePadding(dp3);
        textView.setBackgroundColor(Color.LTGRAY);
        return view;
      }
    };
  }

  private class Item {
    public String file;
    public int icon;

    public Item(String file, Integer icon) {
      this.file = file;
      this.icon = icon;
    }

    @Override
    public String toString() {
      return file;
    }
  }

  private class ItemFileNameComparator implements Comparator<Item> {
    public int compare(Item lhs, Item rhs) {
      return lhs.file.toLowerCase().compareTo(rhs.file.toLowerCase());
    }
  }

  // Listener to handle finish events
  public interface OnFileAndFolderFinishListener {
    public void onFileFinish(String path, RasterLayerAction action);

    public void onDirectoryFinish(String path);
  }

  enum RasterLayerAction {
    BASEMAP_LAYER, OPERATIONAL_LAYER, ELEVATION_SOURCE, BASEMAP_LAYER_AND_ELEVATION_SOURCE, OPERATIONAL_LAYER_AND_ELEVATION_SOURCE, NONE;
  }
}