package com.example.android_doc_app.handlers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Instrumented tests for the *digital PDF* path of [TextExtractionEngine]
 * (i.e. [TextExtractionEngine.extractText] when the PDF has embedded text).
 *
 * The test PDFs are generated in-memory with PDFBox so the tests are
 * self-contained and don't depend on any fixture file on the device.
 */
@RunWith(AndroidJUnit4::class)
class DigitalPdfExtractionTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var engine: TextExtractionEngine

    @Before
    fun setUp() {
        // Needed before PDFBox can load the standard fonts we use to build PDFs.
        PDFBoxResourceLoader.init(context)
        engine = TextExtractionEngine(context)
    }

    @Test
    fun extractsEmbeddedText_fromDigitalPdf() = runBlocking {
        val expected = "Hello PDF world"
        val pdf = pdfWithText(expected)

        val result = engine.extractText(pdf)

        assertTrue(
            "expected extracted text to contain \"$expected\" but was \"$result\"",
            result.contains(expected)
        )
    }

    @Test
    fun digitalPdfWins_overOcrFallback_whenPdfHasText() = runBlocking {
        // A valid text PDF is supplied AND a bitmap fallback is available.
        // The digital-PDF path should win, so OCR is never consulted.
        val expected = "Primary text"
        val pdf = pdfWithText(expected)
        val bitmap = blankBitmap()

        val result = engine.extractText(pdf, bitmapFallback = bitmap)

        assertTrue(result.contains(expected))
    }

    @Test
    fun returnsBlank_forNonPdfInput_withNoFallback() = runBlocking {
        // Garbage bytes make PDDocument.load throw; the engine swallows it and,
        // with no bitmap fallback, returns an empty string.
        val garbage: InputStream = ByteArrayInputStream("not a pdf".toByteArray())

        val result = engine.extractText(garbage)

        assertEquals("", result)
    }

    @Test
    fun returnsBlank_forEmptyStream_withNoFallback() = runBlocking {
        val empty: InputStream = ByteArrayInputStream(ByteArray(0))

        val result = engine.extractText(empty)

        assertEquals("", result)
    }

    // ---- helpers -------------------------------------------------------------

    /** Builds a one-page PDF containing [text] and returns it as a stream. */
    private fun pdfWithText(text: String): InputStream {
        val doc = PDDocument()
        val page = PDPage()
        doc.addPage(page)
        PDPageContentStream(doc, page).use { cs ->
            cs.beginText()
            cs.setFont(PDType1Font.HELVETICA, 12f)
            cs.newLineAtOffset(100f, 700f)
            cs.showText(text)
            cs.endText()
        }
        val bytes = ByteArrayOutputStream().also { doc.save(it); doc.close() }.toByteArray()
        return ByteArrayInputStream(bytes)
    }

    private fun blankBitmap(): Bitmap =
        Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
            Canvas(this).drawColor(Color.WHITE)
        }
}
