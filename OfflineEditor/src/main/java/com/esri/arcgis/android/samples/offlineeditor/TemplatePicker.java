/* Copyright 2015 Esri
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

package com.esri.arcgis.android.samples.offlineeditor;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.core.geodatabase.GeodatabaseFeature;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.FeatureTemplate;
import com.esri.core.map.FeatureType;
import com.esri.core.renderer.Renderer;
import com.esri.core.symbol.Symbol;
import com.esri.core.symbol.SymbolHelper;
import com.esri.core.table.TableException;

public class TemplatePicker extends PopupWindow {
  Context context;

  MapView map;

  private FeatureLayer selectedLayer;

  private FeatureTemplate selectedTemplate;

  private Symbol selectedSymbol;

  public TemplatePicker(Context ctx, MapView map) {
    this.context = ctx;
    this.map = map;
    setContentView(populateContentView());
    Display display = ((Activity) ctx).getWindowManager().getDefaultDisplay();
    // create an pont object to receive the size information
    android.graphics.Point size = new android.graphics.Point();
    // get the size in pixels
    display.getSize(size);
    // size.x = width
    setWidth(size.x);
    setHeight(300);
    setFocusable(true);
  }

  public View populateContentView() {

    ScrollView scrollTemplateGroup = new ScrollView(context);
    scrollTemplateGroup.setVerticalScrollBarEnabled(true);
    scrollTemplateGroup.setVisibility(View.VISIBLE);
    scrollTemplateGroup.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);

    LinearLayout templateGroup = new LinearLayout(context);
    templateGroup.setPadding(10, 10, 10, 10);
    templateGroup.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
        android.view.ViewGroup.LayoutParams.MATCH_PARENT));
    templateGroup.setBackgroundResource(R.drawable.popupbg);
    templateGroup.setOrientation(LinearLayout.VERTICAL);

    for (Layer layer : map.getLayers()) {
      if (layer instanceof FeatureLayer) {
        RelativeLayout templateLayout = new RelativeLayout(context);
        templateLayout.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT));

        TextView layerName = new TextView(context);
        layerName.setPadding(10, 10, 10, 10);
        layerName.setText(layer.getName());
        layerName.setTextColor(Color.MAGENTA);
        layerName.setId(1);

        RelativeLayout.LayoutParams layerTemplateParams = new RelativeLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        layerTemplateParams.addRule(RelativeLayout.BELOW, layerName.getId());
        HorizontalScrollView scrollTemplateAndType = new HorizontalScrollView(context);
        scrollTemplateAndType.setPadding(5, 5, 5, 5);
        LinearLayout layerTemplate = new LinearLayout(context);
        layerTemplate.setBackgroundColor(Color.WHITE);
        layerTemplate.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
        layerTemplate.setId(2);
        String typeIdField = ((GeodatabaseFeatureTable) ((FeatureLayer) layer).getFeatureTable()).getTypeIdField();

        if (typeIdField.equals("")) {
          List<FeatureTemplate> featureTemp = ((GeodatabaseFeatureTable) ((FeatureLayer) layer).getFeatureTable())
              .getFeatureTemplates();

          for (FeatureTemplate featureTemplate : featureTemp) {
            GeodatabaseFeature g;
            try {
              g = ((GeodatabaseFeatureTable) ((FeatureLayer) layer).getFeatureTable()).createFeatureWithTemplate(
                  featureTemplate, null);
              Renderer renderer = ((FeatureLayer) layer).getRenderer();
              Symbol symbol = renderer.getSymbol(g);

              Bitmap bitmap = createBitmapfromSymbol(symbol, (FeatureLayer) layer);

              populateTemplateView(layerTemplate, bitmap, featureTemplate, (FeatureLayer) layer, symbol);
            } catch (TableException e) {

              e.printStackTrace();
            }

          }

        } else {
          List<FeatureType> featureTypes = ((GeodatabaseFeatureTable) ((FeatureLayer) layer).getFeatureTable())
              .getFeatureTypes();

          for (FeatureType featureType : featureTypes) {

            FeatureTemplate[] templates = featureType.getTemplates();
            for (FeatureTemplate featureTemplate : templates) {
              GeodatabaseFeature g;
              try {
                g = ((GeodatabaseFeatureTable) ((FeatureLayer) layer).getFeatureTable()).createFeatureWithTemplate(
                    featureTemplate, null);
                Renderer renderer = ((FeatureLayer) layer).getRenderer();
                Symbol symbol = renderer.getSymbol(g);
                Bitmap bitmap = createBitmapfromSymbol(symbol, (FeatureLayer) layer);

                populateTemplateView(layerTemplate, bitmap, featureTemplate, (FeatureLayer) layer, symbol);
              } catch (TableException e) {
                e.printStackTrace();
              }

            }

          }
        }

        templateLayout.addView(layerName);
        scrollTemplateAndType.addView(layerTemplate);
        templateLayout.addView(scrollTemplateAndType, layerTemplateParams);
        templateGroup.addView(templateLayout);
      }

    }
    scrollTemplateGroup.addView(templateGroup);

    return scrollTemplateGroup;
  }

  private Bitmap createBitmapfromSymbol(Symbol symbol, FeatureLayer layer) {
    Bitmap bitmap = null;
    if (layer.getGeometryType().equals(Geometry.Type.POINT)) {
      Point pt = new Point(20, 20);
      bitmap = SymbolHelper.getLegendImage(symbol, pt, 50, 50, Color.WHITE);
    } else if (layer.getGeometryType().equals(Geometry.Type.POLYLINE)) {
      Polyline polyline = new Polyline();
      polyline.startPath(0, 0);
      polyline.lineTo(40, 40);
      bitmap = SymbolHelper.getLegendImage(symbol, polyline, 50, 50, Color.WHITE);
    } else if (layer.getGeometryType().equals(Geometry.Type.POLYGON)) {
      Polygon polygon = new Polygon();
      polygon.startPath(0, 0);
      polygon.lineTo(40, 0);
      polygon.lineTo(40, 40);
      polygon.lineTo(0, 40);
      polygon.lineTo(0, 0);
      bitmap = SymbolHelper.getLegendImage(symbol, polygon, 50, 50, Color.WHITE);
    }
    return bitmap;
  }

  private void populateTemplateView(LinearLayout layerTemplate, Bitmap bitmap, final FeatureTemplate featureTemplate,
      final FeatureLayer flayer, final Symbol symbol) {
    LinearLayout templateAndType = new LinearLayout(context);
    templateAndType.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
        android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
    templateAndType.setOrientation(LinearLayout.VERTICAL);
    templateAndType.setPadding(10, 10, 10, 10);

    final ImageButton template = new ImageButton(context);
    template.setBackgroundColor(Color.WHITE);

    template.setImageBitmap(bitmap);
    template.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        selectedLayer = flayer;
        selectedTemplate = featureTemplate;
        selectedSymbol = symbol;
        dismiss();

      }
    });

    TextView templateType = new TextView(context);
    templateType.setText(featureTemplate.getName());
    templateType.setPadding(5, 5, 5, 5);
    templateAndType.addView(template);
    templateAndType.addView(templateType);

    LinearLayout.LayoutParams templateParams = new LinearLayout.LayoutParams(
        android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
    templateParams.setMargins(20, 20, 20, 20);
    layerTemplate.addView(templateAndType, templateParams);
  }

  public FeatureLayer getSelectedLayer() {
    return selectedLayer;
  }

  public FeatureTemplate getselectedTemplate() {

    return selectedTemplate;
  }

  public void clearSelection() {
    this.selectedTemplate = null;
    this.selectedLayer = null;
  }

  public Symbol getSelectedSymbol() {
    return selectedSymbol;
  }

}
