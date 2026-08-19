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
import androidx.compose.runtime.Composable
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

        var videoUri by remember {
            mutableStateOf<Uri?>(null)
        }

        var audioUri by remember {
            mutableStateOf<Uri?>(null)
        }

        var videoDuration by remember {
            mutableStateOf<Long?>(null)
        }

        var audioDuration by remember {
            mutableStateOf<Long?>(null)
        }

        var isExporting by remember {
            mutableStateOf(false)
        }

        var exportFinished by remember {
            mutableStateOf(false)
        }

        /*
         * VIDEO PICKER
         */
        val videoPicker =
            rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument()
            ) { uri ->

                if (uri != null) {

                    videoUri = uri

                    videoDuration =
                        getDuration(
                            this@MainActivity,
                            uri
                        )

                    exportFinished = false
                }
            }

        /*
         * AUDIO PICKER
         */
        val audioPicker =
            rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument()
            ) { uri ->

                if (uri != null) {

                    audioUri = uri

                    audioDuration =
                        getDuration(
                            this@MainActivity,
                            uri
                        )

                    exportFinished = false
                }
            }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Top
        ) {

            /*
             * TITLE
             */
            Text(
                text = "Myloover",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "Video Music Looper",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            /*
             * VIDEO CARD
             */
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

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

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

                        Text(
                            text = "Pilih Video"
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            if (videoUri != null) {
                                "Video: ${formatDuration(videoDuration)}"
                            } else {
                                "Belum ada video"
                            }
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            /*
             * MUSIC CARD
             */
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

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

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

                        Text(
                            text = "Pilih Musik"
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            if (audioUri != null) {
                                "Musik: ${formatDuration(audioDuration)}"
                            } else {
                                "Belum ada musik"
                            }
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            /*
             * OUTPUT INFORMATION
             */
            if (
                videoDuration != null &&
                audioDuration != null
            ) {

                Text(
                    text =
                        "Video akan di-loop sampai musik selesai.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text =
                        "Durasi output: ${formatDuration(audioDuration)}",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }

            /*
             * EXPORT BUTTON
             */
            Button(
                onClick = {

                    val selectedVideo =
                        videoUri

                    val selectedAudio =
                        audioUri

                    if (
                        selectedVideo == null ||
                        selectedAudio == null
                    ) {

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
                        videoUri = selectedVideo,
                        audioUri = selectedAudio,

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

                enabled =
                    videoUri != null &&
                    audioUri != null &&
                    !isExporting,

                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text =
                        if (isExporting) {
                            "MEMPROSES..."
                        } else {
                            "BUAT VIDEO"
                        }
                )
            }

            /*
             * EXPORT PROGRESS
             */
            if (isExporting) {

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text =
                        "Sedang membuat video. Jangan tutup aplikasi."
                )
            }

            /*
             * FINISHED MESSAGE
             */
            if (exportFinished) {

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = "✓ Video selesai dibuat.",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text =
                        "File tersimpan di folder Movies/Myloover."
                )
            }
        }
    }

    /*
     * EXPORT VIDEO
     *
     * VIDEO:
     * - hanya mengambil gambar/video
     * - audio asli video dibuang
     * - video di-loop
     *
     * AUDIO:
     * - mengambil musik
     * - tidak di-loop
     *
     * HASIL:
     * video akan terus mengulang
     * sampai musik selesai.
     */
    @OptIn(UnstableApi::class)
    private fun exportVideo(
        videoUri: Uri,
        audioUri: Uri,
        onCompleted: () -> Unit,
        onError: (ExportException) -> Unit
    ) {

        try {

            /*
             * OUTPUT DIRECTORY
             */
            val outputDirectory =
                File(
                    getExternalFilesDir(
                        Environment.DIRECTORY_MOVIES
                    ),
                    "Myloover"
                )

            if (!outputDirectory.exists()) {

                outputDirectory.mkdirs()
            }

            /*
             * FILE NAME
             */
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
             * VIDEO ITEM
             *
             * setRemoveAudio(true)
             * memastikan audio bawaan video
             * tidak ikut masuk ke output.
             */
            val videoItem =
                EditedMediaItem.Builder(
                    MediaItem.fromUri(videoUri)
                )
                    .setRemoveAudio(true)
                    .build()

            /*
             * VIDEO SEQUENCE
             *
             * Video dibuat looping.
             */
            val videoSequence =
                EditedMediaItemSequence
                    .withVideoFrom(
                        listOf(videoItem)
                    )
                    .buildUpon()
                    .setIsLooping(true)
                    .build()

            /*
             * AUDIO ITEM
             */
            val audioItem =
                EditedMediaItem.Builder(
                    MediaItem.fromUri(audioUri)
                )
                    .build()

            /*
             * AUDIO SEQUENCE
             *
             * Tidak looping.
             *
             * Karena audio merupakan sequence
             * non-looping terpanjang, durasinya
             * menjadi batas akhir composition.
             */
            val audioSequence =
                EditedMediaItemSequence
                    .withAudioFrom(
                        listOf(audioItem)
                    )

            /*
             * COMPOSITION
             */
            val composition =
                Composition.Builder(
                    videoSequence,
                    audioSequence
                )
                    .build()

            /*
             * TRANSFORMER
             */
            val transformer =
                Transformer.Builder(
                    this@MainActivity
                )
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

                                onError(
                                    exportException
                                )
                            }
                        }
                    )
                    .build()

            /*
             * START EXPORT
             */
            transformer.start(
                composition,
                outputFile.absolutePath
            )

        } catch (exception: Exception) {

            /*
             * UBAH EXCEPTION BIASA
             * MENJADI ExportException
             */
            onError(
                ExportException.createForUnexpected(
                    exception
                )
            )
        }
    }

    /*
     * GET MEDIA DURATION
     */
    private fun getDuration(
        context: Context,
        uri: Uri
    ): Long? {

        val retriever =
            MediaMetadataRetriever()

        return try {

            retriever.setDataSource(
                context,
                uri
            )

            retriever
                .extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION
                )
                ?.toLongOrNull()

        } catch (exception: Exception) {

            null

        } finally {

            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }
    }

    /*
     * FORMAT DURATION
     */
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
