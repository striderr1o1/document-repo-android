package com.example.android_doc_app.handlers

import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [PdfHandler.extractPdfUri], run on a device/emulator
 * so the real Intent / Uri framework is available.
 */
@RunWith(AndroidJUnit4::class)
class ExtractPdfUriTest {

    @Test
    fun returnsUri_forSharedPdf() {
        val shared = Uri.parse("content://com.example.provider/doc.pdf")
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, shared)
        }

        assertEquals(shared, PdfHandler.extractPdfUri(intent))
    }

    @Test
    fun returnsNull_forWrongAction() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, Uri.parse("content://x/doc.pdf"))
        }

        assertNull(PdfHandler.extractPdfUri(intent))
    }

    @Test
    fun returnsNull_forWrongMimeType() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, Uri.parse("content://x/pic.png"))
        }

        assertNull(PdfHandler.extractPdfUri(intent))
    }

    @Test
    fun returnsNull_whenNoStreamExtra() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            // No EXTRA_STREAM.
        }

        assertNull(PdfHandler.extractPdfUri(intent))
    }
}
