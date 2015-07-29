package com.esri.arcgis.android.samples.offlineeditor;

/* Copyright 2014 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the sample code usage restrictions document for further information.
 *
 */

import android.content.Context;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class OfflineActions implements Callback {

  private static final int MENU_DELETE = 0;

  private static final int MENU_UNDO = 1;

  private static final int MENU_SAVE = 2;

  private static final int MENU_SYNC = 3;
  
  private static final int MENU_TEMPLATES = 4;

  Context mContext;

  public OfflineActions(final OfflineEditorActivity activity) {
    mContext = activity;
  }

  @Override
  public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    switch (item.getItemId()) {
      case MENU_DELETE:
        ((OfflineEditorActivity) mContext).clear();
        break;

      case MENU_UNDO:
        ((OfflineEditorActivity) mContext).undo();
        break;

      case MENU_SAVE:
        try {
          ((OfflineEditorActivity) mContext).save();
        } catch (Exception e) {
          Toast.makeText(mContext,
              "This edit is outside the replica extent. You are limited to edits inside your replicated extent",
              Toast.LENGTH_LONG).show();
        }
        break;

      case MENU_SYNC:
        ((OfflineEditorActivity) mContext).syncGdb();
        break;
        
      case MENU_TEMPLATES:
        ((OfflineEditorActivity) mContext).showEditTemplatePicker();
        break;

      default:
        break;
    }
    return false;
  }

  @Override
  public boolean onCreateActionMode(ActionMode mode, Menu menu) {

    MenuItem item;
    item = menu.add(Menu.NONE, MENU_UNDO, 2, "undo");
    item.setIcon(android.R.drawable.ic_menu_revert);
    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

    item = menu.add(Menu.NONE, MENU_DELETE, 3, "discard");
    item.setIcon(R.drawable.ic_action_content_discard);
    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

    item = menu.add(Menu.NONE, MENU_SAVE, 1, "save");
    item.setIcon(R.drawable.ic_action_save);
    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

    item = menu.add(Menu.NONE, MENU_SYNC, 4, "sync");
    item.setIcon(R.drawable.ic_action_sync);
    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    
    item = menu.add(Menu.NONE, MENU_TEMPLATES, 5, "show templates");
    item.setIcon(R.drawable.ic_action_edit_template);
    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

    return true;
  }

  @Override
  public void onDestroyActionMode(ActionMode mode) {
    ((OfflineEditorActivity) mContext).clear();
    OfflineEditorActivity.editFlag = false;
    mContext = null;

  }

  @Override
  public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    return false;
  }

}
