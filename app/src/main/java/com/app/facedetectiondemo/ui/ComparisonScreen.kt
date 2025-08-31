package com.app.facedetectiondemo.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.facedetectiondemo.data.FaceMatchSingleton
import com.app.facedetectiondemo.data.SavedPhoto
import com.app.facedetectiondemo.viewmodel.ComparisonViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ComparisonScreen(
    comparisonViewModel: ComparisonViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val comparisonState by comparisonViewModel.comparisonState.collectAsState()
    val savedPhotos by comparisonViewModel.savedPhotos.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (comparisonState == null) {
            // No state yet
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = "Initializing...",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        } else if (comparisonState!!.isLoading) {
            // Loading state
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = "Loading comparison...",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            // Determine if this is a new face or a recognized face
            val state = comparisonState!!
            val isNewFace = state.matchedPhoto == null

            Text(
                text = if (isNewFace) "New Face Detected" else "Face Recognized",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Current Photo Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Current Photo",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier.size(80.dp),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        val file = File(state.currentPhoto.filePath)
                        if (file.exists()) {
                            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Current Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // Matched Photo Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (state.matchedPhoto != null) "Matched Photo" else "Matched Photo",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier.size(80.dp),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        if (state.matchedPhoto != null) {
                            val file = File(state.matchedPhoto.filePath)
                            if (file.exists()) {
                                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Matched Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Gray)
                                ) {
                                    Text(
                                        text = "Photo not found",
                                        modifier = Modifier.align(Alignment.Center),
                                        color = Color.White
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Gray)
                            ) {
                                Text(
                                    text = "No match",
                                    modifier = Modifier.align(Alignment.Center),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add divider and title for saved photos section
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Display saved photos in a grid
        if (savedPhotos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No saved photos yet.\nCapture some photos first!",
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(savedPhotos) { photo ->
                    SavedPhotoItem(photo = photo)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                comparisonViewModel.resetState()
                onNavigateBack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Back to Camera")
        }
    }
}

@Composable
fun SavedPhotoItem(photo: SavedPhoto) {
    val context = LocalContext.current
    val bitmap = remember(photo.filePath) {
        try {
            val file = File(photo.filePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val formattedDate = remember(photo.timestamp) {
        dateFormat.format(Date(photo.timestamp))
    }

    // Get face matches from singleton
    val faceMatches = FaceMatchSingleton.faceMatches

    // Find if this photo's ID is in the matches
    val matchInfo = faceMatches.find { it.faceId == photo.id }

    // Determine if this is the best match
    val isBestMatch = if (matchInfo == null) {
        false
    } else {
        val bestMatch = faceMatches.maxByOrNull { it.similarity }
        bestMatch?.faceId == matchInfo.faceId
    }

    // Determine border color based on match status
    val borderColor = when {
        isBestMatch -> Color.Green
        matchInfo != null -> Color.Red
        else -> Color.Transparent
    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 3.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(4.dp)
                ) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Saved photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Text("Image not found")
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = formattedDate,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )

        // Display similarity percentage for all photos
        val similarityPercentage = if (matchInfo != null) {
            (matchInfo.similarity * 100).toInt()
        } else {
            0 // Default to 0% if no match found
        }

        Text(
            text = if (similarityPercentage == 0) {
                ""
            } else "Similarity: $similarityPercentage%",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = when {
                isBestMatch -> Color.Green
                similarityPercentage > 0 -> Color.Red
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }

}
