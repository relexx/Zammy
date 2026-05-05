package com.zammy.app.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

const val MAX_ATTACHMENT_BYTES = 10 * 1024 * 1024 // 10 MB

fun getFilenameFromUri(context: Context, uri: Uri): String? =
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        if (nameIndex >= 0) cursor.getString(nameIndex) else null
    }
