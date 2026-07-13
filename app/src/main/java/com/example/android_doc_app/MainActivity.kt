package com.example.android_doc_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.android_doc_app.handlers.PdfHandler
import com.example.android_doc_app.handlers.TextExtractionEngine
import com.example.android_doc_app.ui.ExtractedTextScreen
import com.example.android_doc_app.ui.theme.AndroiddocappTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        // The engine that turns a PDF's bytes into text. Constructed once here.
        val textEngine = TextExtractionEngine(this)

        // Observable holder for the extracted text, read by the UI below:
        //   null -> still extracting, "" -> nothing found, else -> the text.
        // will only extract text, will not work for scanned pdfs or images currently
        val extractedText = mutableStateOf<String?>(null)

        // Extract the text here (outside setContent), off the main thread.
        // lifecycleScope ties the work to this activity's lifecycle so it's
        // cancelled if the activity is destroyed.
        if (savedPdf != null) {
            lifecycleScope.launch {
                extractedText.value = withContext(Dispatchers.IO) {
                    savedPdf.inputStream().use { textEngine.extractText(it) }
                }
            }
        }

        // Let the app draw behind the status/navigation bars; Scaffold's
        // innerPadding below keeps content from hiding under them.
        enableEdgeToEdge()

        // Bridge into Compose. All rendering lives in ExtractedTextScreen; here
        // we only feed it the file name and the observed extraction result.
        setContent {
            AndroiddocappTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ExtractedTextScreen(
                        pdfName = savedPdf?.name,
                        extractedText = extractedText.value,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
