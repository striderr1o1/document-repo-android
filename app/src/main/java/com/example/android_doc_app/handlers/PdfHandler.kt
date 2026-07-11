package com.example.android_doc_app.handlers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import java.io.File

/**
 * Handles PDFs shared into the app from other apps (via the Android share sheet).
 *
 * `object` (instead of `class`) makes this a singleton: there is exactly one
 * PdfHandler instance, and its functions are called as `PdfHandler.foo()`
 * without constructing anything.
 */
object PdfHandler {

    /**
     * Checks whether [intent] is a "share a PDF" intent and, if so, returns the
     * content Uri of the shared file. Returns null for any other kind of launch
     * (e.g. a normal tap on the app icon).
     */
    fun extractPdfUri(intent: Intent): Uri? {
        // Only handle intents that are (a) a share action and (b) carrying a PDF.
        // Anything else (like the MAIN/LAUNCHER intent) is not our business.
        if (intent.action != Intent.ACTION_SEND || intent.type != "application/pdf") return null

        // The shared file arrives as a Uri stored in the EXTRA_STREAM extra.
        // Android 13 (TIRAMISU, API 33) added a type-safe getParcelableExtra
        // overload and deprecated the old one, so we branch on the OS version.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }
    }

    /**
     * Copies the file behind [uri] into the app's private storage and returns
     * the new File, or null if the source couldn't be opened.
     *
     * Why copy at all: the Uri from a share intent comes with a *temporary*
     * read permission that dies when the activity does. Copying the bytes into
     * our own folder is what makes the PDF permanently ours.
     */
    fun copyToAppStorage(context: Context, uri: Uri): File? {
        // Ask the ContentResolver to route our read request to whichever app
        // owns this content:// Uri. Null means it refused or the file is gone
        // ("?:" = elvis operator: fall back to returning null from the function).
        val input = context.contentResolver.openInputStream(uri) ?: return null

        // Build a destination path inside our private folder
        // (/data/data/<package>/files/). The timestamp keeps names unique.
        // Note: this only creates the path object, not the file itself yet.
        val outFile = File(context.filesDir, "received_${System.currentTimeMillis()}.pdf")

        // `use { }` closes each stream automatically when the block ends,
        // even if an exception is thrown mid-copy (like try-with-resources).
        input.use { inStream ->
            outFile.outputStream().use { outStream ->
                // Pump all bytes from the source into our file, chunk by chunk.
                inStream.copyTo(outStream)
            }
        }
        return outFile
    }
}