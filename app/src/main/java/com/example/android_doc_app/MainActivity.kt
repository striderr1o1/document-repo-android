package com.example.android_doc_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.example.android_doc_app.handlers.PdfHandler
import com.example.android_doc_app.ui.theme.AndroiddocappTheme

class MainActivity : ComponentActivity() {

    // Android calls onCreate when the activity is being created — we never call
    // it ourselves. savedInstanceState is a Bundle (key-value container) holding
    // state from a previous life of this activity: null on a fresh launch,
    // non-null when the system recreates us (e.g. after rotation).
    override fun onCreate(savedInstanceState: Bundle?) {
        // Run the framework's own setup first (window attachment, state
        // restoration). Skipping this crashes with SuperNotCalledException.
        super.onCreate(savedInstanceState)

        // `intent` is the Intent that launched this activity, filled in by
        // Android. If we were opened via the share sheet with a PDF, this
        // returns its content Uri; on a normal launch it returns null.
        val pdfUri = PdfHandler.extractPdfUri(intent)

        // `?.let { }` runs the block only when pdfUri is non-null. We copy the
        // PDF into our private storage *now*, while the share intent's temporary
        // read permission is still valid. `this` works as the Context argument
        // because an Activity is a Context.
        val savedPdf = pdfUri?.let { PdfHandler.copyToAppStorage(this, it) }

        // Let the app draw behind the status/navigation bars; Scaffold's
        // innerPadding below keeps content from hiding under them.
        enableEdgeToEdge()

        // Bridge into Compose: the lambda describes this activity's entire UI.
        setContent {
            AndroiddocappTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // savedPdf is a File? — non-null only when a PDF was shared
                    // in and copied successfully, so it doubles as our status.
                    Text(
                        text = if (savedPdf != null) "Received PDF: ${savedPdf.name}" else "No PDF received",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
