package com.example.android_doc_app.handlers

import android.content.Context
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Instrumented tests for [PdfHandler.copyToAppStorage], run on a device/emulator
 * so a real Context / ContentResolver / filesDir is available.
 */
@RunWith(AndroidJUnit4::class)
class CopyToAppStorageTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    /** Temp source file whose bytes we point a Uri at and later verify. */
    private lateinit var sourceFile: File

    @Before
    fun setUp() {
        sourceFile = File.createTempFile("pdfhandler_src", ".pdf", context.cacheDir)
    }

    @After
    fun tearDown() {
        // Keep tests independent: remove the source and anything we copied in.
        sourceFile.delete()
        context.filesDir.listFiles { f -> f.name.startsWith("received_") }
            ?.forEach { it.delete() }
    }

    @Test
    fun copiesBytesIntoFilesDir() {
        val payload = "%PDF-1.4 fake pdf bytes".toByteArray()
        sourceFile.writeBytes(payload)

        val outFile = PdfHandler.copyToAppStorage(context, Uri.fromFile(sourceFile))

        assertTrue("expected a copied file", outFile != null)
        outFile!!
        // Landed in the app's private files dir with the expected name shape.
        assertEquals(context.filesDir, outFile.parentFile)
        assertTrue(outFile.name.startsWith("received_"))
        assertTrue(outFile.name.endsWith(".pdf"))
        // Bytes were copied faithfully.
        assertArrayEquals(payload, outFile.readBytes())
    }

    @Test
    fun producesDistinctFiles_forRepeatedCalls() {
        sourceFile.writeBytes("data".toByteArray())
        val uri = Uri.fromFile(sourceFile)

        val first = PdfHandler.copyToAppStorage(context, uri)
        // Ensure the millisecond-based name differs between calls.
        Thread.sleep(2)
        val second = PdfHandler.copyToAppStorage(context, uri)

        assertTrue(first != null && second != null)
        assertTrue("copies should have unique paths", first!!.path != second!!.path)
    }
}
