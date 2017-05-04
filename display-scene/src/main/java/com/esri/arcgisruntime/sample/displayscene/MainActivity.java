/* Copyright 2017 Esri
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

package com.esri.arcgisruntime.sample.displayscene;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.SceneView;

public class MainActivity extends AppCompatActivity {

    private SceneView mSceneView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // create SceneView from layout
        mSceneView = (SceneView) findViewById(R.id.sceneView);
        // create a scene and add a basemap to it
        ArcGISScene agsScene = new ArcGISScene();
        agsScene.setBasemap(Basemap.createImagery());
        mSceneView.setScene(agsScene);

        // add base surface for elevation data
        ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource(
                getResources().getString(R.string.elevation_image_service));
        agsScene.getBaseSurface().getElevationSources().add(elevationSource);

        // add a camera and initial camera position
        Camera camera = new Camera(28.4, 83.9, 10010.0, 10.0, 80.0, 300.0);
        mSceneView.setViewpointCamera(camera);
    }

    @Override
    protected void onPause(){
        super.onPause();
        // pause SceneView
        mSceneView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // resume SceneView
        mSceneView.resume();
    }
}
