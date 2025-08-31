# FaceFinder

A modern Android application for face detection, recognition, and comparison using ML Kit and TensorFlow Lite.

## Overview

FaceFinder is a proof-of-concept application that demonstrates how to implement face detection and recognition capabilities in an Android app. It uses the device's camera to capture images, detect faces, and either recognize previously registered faces or register new ones. The app also provides a comparison feature to visually compare detected faces.

## Features

- **Face Detection**: Detect faces in real-time using the device's camera
- **Face Recognition**: Recognize previously registered faces using FaceNet embeddings
- **Face Comparison**: Compare detected faces with stored faces
- **Photo Management**: Save and manage detected faces
- **Modern UI**: Built with Jetpack Compose for a smooth and responsive user experience

## Technologies Used

- **Kotlin**: Primary programming language
- **Jetpack Compose**: Modern UI toolkit for building native Android UI
- **CameraX**: Camera API for capturing images
- **ML Kit**: Google's machine learning library for face detection
- **TensorFlow Lite**: For running the FaceNet model to generate face embeddings
- **DataStore**: For persistent data storage
- **Navigation Compose**: For in-app navigation
- **MVVM Architecture**: For clean separation of concerns

## Requirements

- Android Studio Arctic Fox or newer
- Android SDK 24 or higher (Android 7.0+)
- Device with a camera

## Setup

1. Clone the repository:
   ```
   git clone https://github.com/sh7verma/FaceFinder.git
   ```

2. Open the project in Android Studio

3. Sync Gradle and build the project

4. Run the app on a physical device (emulators may not support camera functionality)

## Usage

1. **Camera Screen**: The app opens with the camera screen. Point the camera at a face to detect it.

2. **Face Detection**: When a face is detected, it will be highlighted and processed.

3. **Face Recognition**: The app will attempt to recognize the face. If it's a new face, it will be registered.

4. **Comparison Screen**: After processing, the app navigates to the comparison screen to show the detected face and any matched face.

5. **Photo List**: Access the list of saved photos to view previously detected faces.

## Project Structure

- **MainActivity.kt**: Entry point of the application with navigation setup
- **FaceViewModel.kt**: Handles face detection and recognition logic
- **ComparisonViewModel.kt**: Manages face comparison functionality
- **CameraScreen.kt**: UI for camera capture and face detection
- **ComparisonScreen.kt**: UI for comparing detected faces
- **PhotoListScreen.kt**: UI for displaying saved photos
- **FaceDetectorHelper.kt**: Helper class for ML Kit face detection
- **FaceNetHelper.kt**: Helper class for TensorFlow Lite face recognition

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgements

- Google ML Kit for face detection capabilities
- FaceNet model for face recognition
- TensorFlow Lite for on-device machine learning
