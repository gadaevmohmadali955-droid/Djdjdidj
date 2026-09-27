package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PhotoStorage {

    fun saveBitmapToFile(context: Context, bitmap: Bitmap, filename: String): String {
        return try {
            val file = File(context.filesDir, "$filename.jpg")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.flush()
            fos.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun loadBitmapFromFile(path: String?): Bitmap? {
        if (path.isNullOrBlank()) return null
        return try {
            val file = File(path)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }

    fun base64ToBitmap(base64Str: String?): Bitmap? {
        if (base64Str.isNullOrBlank()) return null
        return try {
            val bytes = Base64.decode(base64Str, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Creates a high-fidelity biometric portrait snapshot with security watermark,
     * biometric scan grid, angle label, and date stamp.
     */
    fun createBiometricFaceBitmap(name: String, age: Int, angleLabel: String): Bitmap {
        val width = 480
        val height = 640
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background dark gradient
        val bgPaint = Paint().apply {
            color = Color.parseColor("#15181C")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Subtle biometric grid lines
        val gridPaint = Paint().apply {
            color = Color.parseColor("#203545")
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        for (x in 0..width step 40) {
            canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), gridPaint)
        }
        for (y in 0..height step 40) {
            canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), gridPaint)
        }

        // Biometric Face Silhouette
        val facePaint = Paint().apply {
            color = Color.parseColor("#00A2FF")
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // Head oval
        val cx = width / 2f
        val cy = height * 0.42f
        val headRadiusX = 110f
        val headRadiusY = 145f

        // Head shadow / glow
        val glowPaint = Paint().apply {
            color = Color.parseColor("#2200A2FF")
            style = Paint.Style.STROKE
            strokeWidth = 12f
            isAntiAlias = true
        }
        canvas.drawOval(RectF(cx - headRadiusX - 10, cy - headRadiusY - 10, cx + headRadiusX + 10, cy + headRadiusY + 10), glowPaint)

        // Face Base
        val skinPaint = Paint().apply {
            color = Color.parseColor("#2C3A47")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawOval(RectF(cx - headRadiusX, cy - headRadiusY, cx + headRadiusX, cy + headRadiusY), skinPaint)

        // Shoulders
        val shoulders = RectF(cx - 190f, cy + 90f, cx + 190f, height.toFloat() - 40f)
        canvas.drawOval(shoulders, skinPaint)

        // Biometric Target Frame
        val framePaint = Paint().apply {
            color = Color.parseColor("#00E5FF")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        val frameRect = RectF(cx - 150f, cy - 180f, cx + 150f, cy + 180f)
        canvas.drawRoundRect(frameRect, 24f, 24f, framePaint)

        // Biometric scanning points
        val pointPaint = Paint().apply {
            color = Color.parseColor("#00E676")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(cx - 45f, cy - 30f, 6f, pointPaint) // Left eye point
        canvas.drawCircle(cx + 45f, cy - 30f, 6f, pointPaint) // Right eye point
        canvas.drawCircle(cx, cy + 15f, 5f, pointPaint)       // Nose point
        canvas.drawCircle(cx - 30f, cy + 65f, 5f, pointPaint) // Left mouth point
        canvas.drawCircle(cx + 30f, cy + 65f, 5f, pointPaint) // Right mouth point

        // Text & Watermark
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 24f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("БИОМЕТРИЧЕСКИЙ СКАНЕР BLOX", cx, 50f, textPaint)

        val subPaint = Paint().apply {
            color = Color.parseColor("#00E5FF")
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("$angleLabel • $age ЛЕТ", cx, 84f, subPaint)

        val timeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        val footerPaint = Paint().apply {
            color = Color.parseColor("#8E9297")
            textSize = 17f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Пользователь: $name • ${timeFormat.format(Date())}", cx, height - 20f, footerPaint)

        return bitmap
    }
}
