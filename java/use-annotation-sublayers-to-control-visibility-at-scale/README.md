# Control Annotation Sublayer visibility

Use annotation sublayers to control the scales at which different annotation is visible.

![](control-annotation-sublayer-visibility.png)

## Use case

Annotation, which differs from labels by having a fixed place and size, is typically only relevant at particular scales. Annotation sublayers allow for finer control of annotation by allowing properties (like visibility) to be set on subtypes of an annotation layer. For example, an annotation sublayer documenting smaller features may be presented at a smaller font size than larger features and therefore should not be displayed for the same scale range as the annotation sublayer holding larger features. 

## How to use the sample

Start the sample and take note of the visibility indicators for each sublayer. Zoom in and out and note that one sublayer is visible at scales between NUM and NUM and the other between NUM and NUM.

## How it works

1. Load the `MobileMapPackage` 
1. On changes to map view navigation, query whether the `AnnotationSublayer` is visible at the current scale and update the associated UI element.
 
## Relevant API

* AnnotationLayer
* AnnotationSublayer

## About the data

Utility network data forthcoming...

## Tags

Visualization, Annotation, utilities, text, scale

