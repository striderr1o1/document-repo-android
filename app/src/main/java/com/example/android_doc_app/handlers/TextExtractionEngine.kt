package com.example.android_doc_app.handlers

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.InputStream
import kotlinx.coroutines.tasks.await 

class TextExtractionEngine(private val context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    init {
        PDFBoxResourceLoader.init(context)
    }

    suspend fun extractText(inputStream: InputStream, bitmapFallback: Bitmap? = null): String {
        var text = extractTextFromDigitalPdf(inputStream)

        if (text.isBlank() && bitmapFallback != null) {
            text = extractTextUsingOCR(bitmapFallback)
        }

        return text
    }

    private fun extractTextFromDigitalPdf(inputStream: InputStream): String {
        return try {
            val document: PDDocument = PDDocument.load(inputStream)
            val stripper = PDFTextStripper()
            val parsedText = stripper.getText(document)
            document.close()
            parsedText.trim()
        } catch (e: Exception) {
            e.printStackTrace()
            "" 
        }
    }

    private suspend fun extractTextUsingOCR(bitmap: Bitmap): String {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val visionText = recognizer.process(image).await()
            visionText.text.trim()
        } catch (e: Exception) {
            e.printStackTrace()
            "" 
        }
    }
}