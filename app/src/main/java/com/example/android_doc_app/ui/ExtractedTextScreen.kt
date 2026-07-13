package com.example.android_doc_app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Pure display for extracted PDF text — holds no extraction logic, so it can be
 * changed or removed without touching MainActivity's flow.
 *
 * @param pdfName       name of the received PDF, or null if none was received.
 * @param extractedText the extracted text; "" if none was found; null while
 *                      extraction is still running.
 */
@Composable
fun ExtractedTextScreen(
    pdfName: String?,
    extractedText: String?,
    modifier: Modifier = Modifier,
) {
    if (pdfName == null) {
        Text(text = "No PDF received", modifier = modifier.padding(16.dp))
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Received PDF: $pdfName",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))

        when (extractedText) {
            null -> CircularProgressIndicator()
            "" -> Text(text = "No text could be extracted from this PDF.")
            else -> Text(text = extractedText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
