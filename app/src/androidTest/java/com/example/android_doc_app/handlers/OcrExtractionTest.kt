package com.example.android_doc_app.handlers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * Instrumented tests for the *OCR fallback* path of [TextExtractionEngine]
 * (i.e. [TextExtractionEngine.extractText] when the PDF yields no text and a
 * bitmap fallback is supplied).
 *
 * NOTE: OCR uses ML Kit, whose text-recognition model is downloaded on first
 * use via Google Play services. These tests therefore require an emulator/device
 * with Google Play + network, and the very first run may be slow (or briefly
 * fail) while that model downloads. Run once to warm the model up if needed.
 */
@RunWith(AndroidJUnit4::class)
class OcrExtractionTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var engine: TextExtractionEngine

    /** Empty stream so the digital-PDF path returns blank and OCR is triggered. */
    private val noPdf: InputStream
        get() = ByteArrayInputStream(ByteArray(0))

    @Before
    fun setUp() {
        engine = TextExtractionEngine(context)
    }

    @Test
    fun recognizesText_fromBitmapFallback() = runBlocking {
        val bitmap = bitmapOfText("HELLO")

        val result = engine.extractText(noPdf, bitmapFallback = bitmap)

        assertTrue(
            "expected OCR to read \"HELLO\" but got \"$result\"",
            result.uppercase().contains("HELLO")
        )
    }

    @Test
    fun returnsBlank_whenBitmapHasNoText() = runBlocking {
        // Blank white image: no PDF text and nothing for OCR to find -> "".
        val blank = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888).apply {
            Canvas(this).drawColor(Color.WHITE)
        }

        val result = engine.extractText(noPdf, bitmapFallback = blank)

        assertEquals("", result)
    }

    // ---- helpers -------------------------------------------------------------

    /** Renders [text] as large black text on a white bitmap for OCR to read. */
    private fun bitmapOfText(text: String): Bitmap {
        val bmp = Bitmap.createBitmap(600, 200, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.WHITE)
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 96f
            isAntiAlias = true
        }
        canvas.drawText(text, 40f, 130f, paint)
        return bmp
    }
}
