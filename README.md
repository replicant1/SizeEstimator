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
- Open the file `build.gradle.kts(:app)` and change the value of `buildConfigField` called `REFERENCE_OBJECT_WIDTH_MM` to the width of your reference object in millimetres.
- Build and start the app and hold the device in landscape orientation.
- Hold the device so that it looks down on a plane such as a table top
- Position the reference object at the bottom of the preview frame. The reference object should be something long and thin like a pen. The true length of the reference object should already have been assigned to the constant `REFERENCE_OBJECT_WIDTH_MM` in `MainActivity.kt`
- The reference object should be below the vertical mid-point of the preview image.
- The target object (to be measured) should be placed above the reference object
- When both reference and target objects are correctly positioned, tap the `Measure` button and the estimated size of the target object in millimetres will be printed on the screen underneath the `Measure` button.

# Algorithm

- `Tensor Flow Lite` is used to generate a set of bounding boxes for all the objects in the image.
- Each bounding box has a `score` associated with it
- The bounding box for the reference object is found by identifying the bounding box with the highest score that is below the midpoint of the preview image.
- The bounding box for the target object is the bounding box with the highest score that is above the reference object's bounding box.
- The reference object's width in millimetres is divided by its bounding box's width in pixels to get a scaling factor.
- That scaling factor is applied to the pixel width and height of the target object's bounding box to calculate the target object's real size in millimetres.

# Artifacts

The algorithm above can be visualized by looking at the following samples. The app generates three images as a side-effect of each measurement taken:
- The raw camera image in landscape orientation
- The second image is obtained by cropping the camera image to square, then scaling it down to 300 x 300 pixels, which is the size expected by the Tensor Flow model. 
- The model is then applied to the 300 x 300 image, which results in a set of bounding boxes for each object found. Those boxes and an accompanying legend appear below. The two boxes that are drawn with a solid line correspond to the reference and target objects. The rest are drawn with a dashed line.

## Sample 1

To view these artifacts you need to look in the cache dir for the app. eg. `/data/data/com.example.sizeestimator/cache`. Use the `Device Explorer` in Android Studio to do this.

![Cropped](/doc/sample1/cropped.jpg)
![Markedup](/doc/sample1/marked_up.jpg)

Actual Size: 99 x 71 mm,
Estimated Size: 103 x 85 mm,
Note: Reference object is 123mm long

## Sample 2

![Cropped](/doc/sample2/cropped.jpg)
![Markedup](/doc/sample2/marked_up.jpg)

Actual Size: 100 x 100 mm,
Estimated Size: 114 x 120 mm

## Sample 3

![Cropped](/doc/sample3/cropped.jpg)
![Markedup](/doc/sample3/marked_up.jpg)

Actual Size: 128 x 77 mm,
Estimated Size: 135 x 71 mm

## Sample 4

![Cropped](/doc/sample4/cropped.jpg)
![Markedup](/doc/sample4/marked_up.jpg)

Actual SizeL: 89 x 93 mm,
Estimated Size: 96 x 104 mm

## Discussion

Due to time limitations there are a few changes I would have liked to make but was not able to:
- There is a problem with the orientation of images from Camera X. When the device is in portrait orientation, the captured images are in landscape orientation. I would like to investigate this more thoroughly. I side-stepped it by locking the app to landscape orientation, in which mode this bug does not appear.
- The files `lores.jpg` and `hires.jpg` are in the app's cache directory and are replaced every time the app is run, but they are not explicitly deleted at any time.
- Tests - only had time for one test class - needs instrumented tests.
- Accuracy of size estimates seems low - can anything be done to improve it? eg. lighting, contrast, alignment of objects.
- Some sort of overlay on the preview image to help users position the reference object.
- Ability to enter the real size of the reference object through the UI, rather than having to change the code.
- Having a large file like `ssd_mobilenet_v1.tflite` can create trouble for GIT operations timing out. Need to increase GIT postBuffer size.

