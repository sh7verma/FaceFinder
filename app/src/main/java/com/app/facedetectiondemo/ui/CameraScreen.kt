package com.app.facedetectiondemo.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import com.app.facedetectiondemo.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.facedetectiondemo.viewmodel.FaceViewModel
import java.io.File

@Composable
fun CameraScreen(
    viewModel: FaceViewModel = viewModel(),
    onNavigateToPhotoList: () -> Unit = {},
    onNavigateToComparison: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var croppedFaceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isFrontCamera by remember { mutableStateOf(true) }

    val croppedFaceCallback = rememberUpdatedState { bitmap: Bitmap? ->
        croppedFaceBitmap = bitmap
    }

    LaunchedEffect(viewModel) {
        viewModel.croppedFaceCallback = croppedFaceCallback.value
    }

    LaunchedEffect(viewModel) {
        viewModel.navigateToComparison.collect {
            onNavigateToComparison()
        }
    }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    LaunchedEffect(isFrontCamera) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }


        val preferredCameraSelector = if (isFrontCamera) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }


        val fallbackCameraSelector = if (isFrontCamera) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }

        cameraProvider.unbindAll()

        try {

            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                preferredCameraSelector,
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            Log.e("CameraScreen", "Failed to bind preferred camera: ${e.message}")
            try {

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    fallbackCameraSelector,
                    preview,
                    imageCapture
                )

                isFrontCamera = !isFrontCamera
                Toast.makeText(
                    context,
                    "Switched to ${if (isFrontCamera) "front" else "back"} camera",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {

                Log.e("CameraScreen", "Failed to bind any camera: ${e.message}")
                Toast.makeText(
                    context,
                    "No available camera found on this device",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                var rotationDegrees = 0
                try {
                    val exifInterface = context.contentResolver.openInputStream(it)?.use { stream ->
                        ExifInterface(stream)
                    }

                    val orientation = exifInterface?.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )

                    rotationDegrees = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }

                    Log.d(
                        "CameraScreen",
                        "Gallery image orientation: $orientation, rotation: $rotationDegrees"
                    )
                } catch (e: Exception) {
                    Log.e("CameraScreen", "Error reading EXIF data", e)
                }

                val rotatedBitmap = if (rotationDegrees != 0) {
                    Bitmap.createBitmap(
                        bitmap, 0, 0,
                        bitmap.width, bitmap.height,
                        Matrix().apply { postRotate(rotationDegrees.toFloat()) },
                        true
                    )
                } else {
                    bitmap
                }

                capturedBitmap = rotatedBitmap
                viewModel.processImage(rotatedBitmap)
            } catch (e: Exception) {
                Log.e("CameraScreen", "Error loading gallery image", e)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())

        // Camera flip button at top left corner
        IconButton(
            onClick = {
                isFrontCamera = !isFrontCamera
            },
            modifier = Modifier
                .padding(36.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_camera_flip),
                contentDescription = "Flip Camera",
                tint = Color.White
            )
        }

        // Photos list button at top right corner
        IconButton(
            onClick = onNavigateToPhotoList,
            modifier = Modifier
                .padding(36.dp)
                .align(Alignment.TopEnd)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_photos_list),
                contentDescription = "Photos List",
                tint = Color.White
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        ) {
            // Gallery Button
            Button(
                onClick = {
                    galleryLauncher.launch("image/*")
                }
            ) {
                Text("Gallery")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(
                        File.createTempFile("face_", ".jpg", context.cacheDir)
                    ).build()

                    imageCapture.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                val unrotatedBitmap = image.toBitmap()
                                val rotationDegrees = image.imageInfo.rotationDegrees
                                image.close()

                                val rotatedBitmap = Bitmap.createBitmap(
                                    unrotatedBitmap, 0, 0,
                                    unrotatedBitmap.width, unrotatedBitmap.height,
                                    Matrix().apply { postRotate(rotationDegrees.toFloat()) },
                                    true
                                )

                                capturedBitmap = rotatedBitmap
                                viewModel.processImage(rotatedBitmap)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e("CameraScreen", "Capture failed", exception)
                            }
                        }
                    )
                }
            ) {
                Text("Capture")
            }

        }

        capturedBitmap?.let { bitmap ->
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .background(Color.Black, shape = RoundedCornerShape(8.dp))
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Captured Face",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        croppedFaceBitmap?.let { faceBitmap ->
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .background(Color.Yellow, shape = RoundedCornerShape(8.dp))
            ) {
                Image(
                    bitmap = faceBitmap.asImageBitmap(),
                    contentDescription = "Cropped Face",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
