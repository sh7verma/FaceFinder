package com.app.facedetectiondemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.facedetectiondemo.ui.CameraScreen
import com.app.facedetectiondemo.ui.ComparisonScreen
import com.app.facedetectiondemo.ui.PhotoListScreen
import com.app.facedetectiondemo.ui.theme.FaceDetectionDemoTheme
import com.app.facedetectiondemo.viewmodel.ComparisonViewModel
import com.app.facedetectiondemo.viewmodel.FaceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceDetectionDemoTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val faceViewModel: FaceViewModel = viewModel()
    val comparisonViewModel: ComparisonViewModel = viewModel()

    NavHost(navController = navController, startDestination = "camera") {
        composable("camera") {
            CameraScreen(
                viewModel = faceViewModel,
                onNavigateToPhotoList = {
                    navController.navigate("photos")
                },
                onNavigateToComparison = {
                    navController.navigate("comparison")
                }
            )
        }

        composable("photos") {
            PhotoListScreen(
                viewModel = faceViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("comparison") {
            ComparisonScreen(
                comparisonViewModel = comparisonViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
