package com.ramdhanyss.myloover

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(UnstableApi::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MylooverApp()
                }
            }
        }
    }

    @Composable
    private fun MylooverApp() {

        var videoUri by remember { mutableStateOf<Uri?>(null) }
        var audioUri by remember { mutableStateOf<Uri?>(null) }

        var videoDuration by remember { mutableStateOf<Long?>(null) }
        var audioDuration by remember { mutableStateOf<Long?>(null) }

        var isExporting by remember { mutableStateOf(false) }
        var exportFinished by remember { mutableStateOf(false) }

        val videoPicker =
            rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument()
            ) { uri ->
                if (uri != null) {
                    videoUri = uri
                    videoDuration = getDuration(this@MainActivity, uri)
                }
            }

        val audioPicker =
            rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument()
            ) { uri ->
                if (uri != null) {
                    audioUri = uri
                    audioDuration = getDuration(this@MainActivity, uri)
                }
            }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Top
        ) {

            Text(
                text = "Myloover",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Video Music Looper",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "1. Video",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            videoPicker.launch(
                                arrayOf(
                                    "video/mp4",
                                    "video/*"
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pilih Video")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (videoUri != null) {
                            "Video: ${formatDuration(videoDuration)}"
                        } else {
                            "Belum ada video"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "2. Musik",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            audioPicker.launch(
                                arrayOf(
                                    "audio/*"
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pilih Musik")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (audioUri != null) {
                            "Musik: ${formatDuration(audioDuration)}"
                        } else {
                            "Belum ada musik"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (videoDuration != null && audioDuration != null) {

                Text(
                    text = "Video akan di-loop sampai durasi musik.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Durasi output: ${formatDuration(audioDuration)}",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {

                    val video = videoUri
                    val audio = audioUri

                    if (video == null || audio == null) {
                        Toast.makeText(
                            this@MainActivity,
                            "Pilih video dan musik terlebih dahulu.",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@Button
                    }

                    isExporting = true
                    exportFinished = false

                    exportVideo(
                        videoUri = video,
                        audioUri = audio,
                        onCompleted = {
                            runOnUiThread {
                                isExporting = false
                                exportFinished = true

                                Toast.makeText(
                                    this@MainActivity,
                                    "Video berhasil dibuat.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        onError = { error ->
                            runOnUiThread {
                                isExporting = false

                                Toast.makeText(
                                    this@MainActivity,
                                    "Export gagal: ${error.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )
                },
                enabled = videoUri != null &&
                        audioUri != null &&
                        !isExporting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isExporting) {
                        "MEMPROSES..."
                    } else {
                        "BUAT VIDEO"
                    }
                )
            }

            if (isExporting) {

                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sedang membuat video. Jangan tutup aplikasi."
                )
            }

            if (exportFinished) {

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "✓ Video selesai dibuat.",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "File tersimpan di folder Movies/Myloover."
                )
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun exportVideo(
        videoUri: Uri,
        audioUri: Uri,
        onCompleted: () -> Unit,
        onError: (ExportException) -> Unit
    ) {

        try {

            val outputDirectory =
                File(
                    getExternalFilesDir(Environment.DIRECTORY_MOVIES),
                    "Myloover"
                )

            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs()
            }

            val timestamp =
                SimpleDateFormat(
                    "yyyyMMdd_HHmmss",
                    Locale.US
                ).format(Date())

            val outputFile =
                File(
                    outputDirectory,
                    "Myloover_$timestamp.mp4"
                )

            /*
             * VIDEO SEQUENCE
             *
             * Hanya mengambil video.
             * Audio asli dari video tidak digunakan.
             */
            val videoItem =
                EditedMediaItem.Builder(
                    MediaItem.fromUri(videoUri)
                ).build()

            val videoSequence =
                EditedMediaItemSequence
                    .withVideoFrom(
                        listOf(videoItem)
                    )
                    .buildUpon()
                    .setIsLooping(true)
                    .build()

            /*
             * AUDIO SEQUENCE
             *
             * Musik tidak di-loop.
             * Karena sequence video di-loop dan musik tidak,
             * durasi musik menjadi batas akhir output.
             */
            val audioItem =
                EditedMediaItem.Builder(
                    MediaItem.fromUri(audioUri)
                ).build()

            val audioSequence =
                EditedMediaItemSequence
                    .withAudioFrom(
                        listOf(audioItem)
                    )

            /*
             * COMPOSITION
             *
             * Video dan musik berada pada timeline yang sama.
             */
            val composition =
                Composition.Builder(
                    videoSequence,
                    audioSequence
                ).build()

            val transformer =
                Transformer.Builder(this)
                    .addListener(
                        object : Transformer.Listener {

                            override fun onCompleted(
                                composition: Composition,
                                exportResult: ExportResult
                            ) {
                                onCompleted()
                            }

                            override fun onError(
                                composition: Composition,
                                exportResult: ExportResult,
                                exportException: ExportException
                            ) {
                                onError(exportException)
                            }
                        }
                    )
                    .build()

            transformer.start(
                composition,
                outputFile.absolutePath
            )

        } catch (exception: Exception) {

            onError(
                ExportException.createForUnexpected(
                    exception
                )
            )
        }
    }

    private fun getDuration(
        context: Context,
        uri: Uri
    ): Long? {

        return try {

            val retriever =
                MediaMetadataRetriever()

            retriever.setDataSource(
                context,
                uri
            )

            val duration =
                retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION
                )?.toLongOrNull()

            retriever.release()

            duration

        } catch (exception: Exception) {
            null
        }
    }

    private fun formatDuration(
        duration: Long?
    ): String {

        if (duration == null) {
            return "--:--"
        }

        val totalSeconds =
            duration / 1000

        val hours =
            totalSeconds / 3600

        val minutes =
            (totalSeconds % 3600) / 60

        val seconds =
            totalSeconds % 60

        return if (hours > 0) {
            String.format(
                Locale.US,
                "%02d:%02d:%02d",
                hours,
                minutes,
                seconds
            )
        } else {
            String.format(
                Locale.US,
                "%02d:%02d",
                minutes,
                seconds
            )
        }
    }
}
