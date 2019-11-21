/*
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.sample.grouplayers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class BottomSheetRecyclerView extends RecyclerView {

  public BottomSheetRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public BottomSheetRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  /**
   * Intercept touch events and determine if {@link RecyclerView} should grab touch event to allow scrolling of RecyclerView
   * within Bottom Sheet
   * @param e event intercepted
   * @return return true to consume the event, false otherwise
   */
  @Override public boolean onInterceptTouchEvent(MotionEvent e) {
    if (e.getAction() == MotionEvent.ACTION_SCROLL && canScrollVertically(1)) {
      return true;
    }
    return super.onInterceptTouchEvent(e);
  }
}
