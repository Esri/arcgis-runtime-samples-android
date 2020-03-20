# Change atmosphere effect

Changes the appearance of the atmosphere in a scene.

![Image of change atmosphere effect](change-atmosphere-effect.png)

## Use case

Atmospheric effect can be used to make the scene view look more realistic.

## How to use the sample

Select one of the three available atmosphere effects. The sky will change to display the selected atmosphere effect.

## How it works

1. Create an `ArcGISScene` and display it in a `SceneView`.
2. Change the atmosphere effect with `sceneView.setAtmosphereEffect(...)`.

## Relevant API

- ArcGISScene
- AtmosphereEffect
- SceneView

## Additional information

There are three atmosphere effect options:

- **Realistic** - A realistic atmosphere effect is applied over the entire surface.
- **Horizon only** - Atmosphere effect applied to the sky (horizon) only.
- **None** - No atmosphere effect. The sky is rendered black with a starfield consisting of randomly placed white dots.

## Tags

atmosphere, horizon, sky
