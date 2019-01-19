# Change Atmosphere Effect
Change the appearance of the atmosphere in a scene view.

![Change Atmosphere Effect App](change-atmosphere-effect.png)

## How to use the sample
Select one of the three available atmosphere effects from the menu. The sky will change to display the selected atmosphere effect.

## How it works
To change the atmosphere effect:

1. Create an `ArcGISScene` and display it in a `SceneView`.
2. Change the atmosphere effect with `sceneView.setAtmosphereEffect`(atmosphereEffect).

## Relevant API

* ArcGISScene
* AtmosphereEffect
* SceneView

## Additional Information
There are three atmosphere effect options:

* Realistic - Atmosphere effect applied to both the sky and the surface as viewed from above.
* Horizon only - Atmosphere effect applied to the sky (horizon) only.
* None - No atmosphere effect. The sky is rendered black with a starfield consisting of randomly placed white dots.

#### Tags
Maps and Scenes
