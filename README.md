# Android Developer Code Challenge

Rod Bailey
Thursday 24 October 2024

# Summary

This is an Android technical exercise focussed on the use of `Tensor Flow Lite` and `Camera X` to produce an app that can optically measure the size of an object presented to the camera in the same plane as a reference object of known dimensions.

# Build

This Github repository contains a single Android studio project that is ready to build and install. It has been built with `Android Studio Koala Feature Drop | 2024.1.2`

# Architecture

To be decided.

# Screen

The app contains only a single screen which is locked to landscape orientation for simplicity.

![Screenshot](/doc/screenshot.png)

# Usage

![Usage](/doc/usage.jpg)

To use the app:
- Start the app and hold the device in landscape orientation.
- Hold the device so that it looks down on a plane such as a table top
- Position the reference object at the bottom of the preview frame. The reference object should be something long and thin like a pen. The true length of the reference object should already have been assigned to the constant `REFERENCE_OBJECT_WIDTH_MM` in `MainActivity.kt`
- The reference object should be below the vertical mid-point of the preview image.
- The target object (to be measured) should be placed above the reference object
- When both reference and target objects are correctly positioned, tap the `Measure` button and the estimated size of the target object in millimetres will be printed on the screen underneath the `Measure` button.

# Algorithm

- `Tensor Flow Lite` is used to generate a set of bounding boxes for all the objects in the image
- Each bounding box has a `score` associated with it
- The bounding box for the reference object is found by identifying the bounding box with the highest score that is below the midpoint of the preview image.
- The bounding box for the target object is the bounding box with the highest score that is above the reference object's bounding box.
- The reference object's width in millimetres, which is hardcoded into `MainActivity`, is divided by its bounding box's width in pixels to get a scaling factor.
- That scaling factor is applied to the pixel width and height of the target object's bounding box to calculate the target object's real size in millimetres.

# Artifacts

The algorithm above can be visualized by looking at the following samples. The app generates two images as a side-effect of each measurement taken. The first is obtained by cropping the camera image to square, then scaling it down to 300 x 300 pixels, which is the size expected by the Tensor Flow model. The model is then applied to the 300 x 300 image, which results in a set of bounding boxes for each object found. Those boxes and an accompanying legend appear below. The two boxes that are drawn with a solid line correspond to the reference and target objects. The rest are drawn with a dashed line.

## Sample 1

![Cropped](/doc/sample1/cropped.jpg)
![Markedup](/doc/sample1/marked_up.jpg)

## Sample 2

![Cropped](/doc/sample2/cropped.jpg)
![Markedup](/doc/sample2/marked_up.jpg)

## Sample 3

![Cropped](/doc/sample3/cropped.jpg)
![Markedup](/doc/sample3/marked_up.jpg)

## Sample 4

![Cropped](/doc/sample4/cropped.jpg)
![Markedup](/doc/sample4/marked_up.jpg)

