package com.example.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageTileSlicer {

    suspend fun loadAndSliceImage(
        context: Context,
        imageIdentifier: String,
        gridSize: Int
    ): Map<Int, Bitmap> = withContext(Dispatchers.IO) {
        val originalBitmap = loadBitmap(context, imageIdentifier)
        val squareBitmap = makeSquare(originalBitmap)
        sliceBitmap(squareBitmap, gridSize)
    }

    private fun loadBitmap(context: Context, identifier: String): Bitmap {
        return try {
            val resId = context.resources.getIdentifier(identifier, "drawable", context.packageName)
            if (resId != 0) {
                val decoded = BitmapFactory.decodeResource(context.resources, resId)
                decoded ?: createFallbackBitmap(identifier)
            } else {
                createFallbackBitmap(identifier)
            }
        } catch (e: Throwable) {
            createFallbackBitmap(identifier)
        }
    }

    private fun makeSquare(src: Bitmap?): Bitmap {
        if (src == null) return createFallbackBitmap("Puzzle")
        val size = minOf(src.width, src.height)
        if (size <= 0) return createFallbackBitmap("Puzzle")
        val x = (src.width - size) / 2
        val y = (src.height - size) / 2
        return Bitmap.createBitmap(src, x, y, size, size)
    }

    private fun sliceBitmap(squareBitmap: Bitmap, gridSize: Int): Map<Int, Bitmap> {
        val tileMap = mutableMapOf<Int, Bitmap>()
        val tileSize = (squareBitmap.width / gridSize).coerceAtLeast(1)
        val totalTiles = gridSize * gridSize

        for (tileId in 0 until totalTiles - 1) {
            val r = tileId / gridSize
            val c = tileId % gridSize
            val x = c * tileSize
            val y = r * tileSize
            val tileBitmap = Bitmap.createBitmap(squareBitmap, x, y, tileSize, tileSize)
            tileMap[tileId] = tileBitmap
        }

        return tileMap
    }

    fun getFullBitmap(context: Context, imageIdentifier: String): Bitmap {
        return try {
            val resId = context.resources.getIdentifier(imageIdentifier, "drawable", context.packageName)
            if (resId != 0) {
                val bmp = BitmapFactory.decodeResource(context.resources, resId)
                makeSquare(bmp ?: createFallbackBitmap(imageIdentifier))
            } else {
                makeSquare(createFallbackBitmap(imageIdentifier))
            }
        } catch (e: Throwable) {
            makeSquare(createFallbackBitmap(imageIdentifier))
        }
    }

    private fun createFallbackBitmap(title: String): Bitmap {
        val size = 600
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Gradient background
        paint.color = Color.parseColor("#4A00E0")
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)

        paint.color = Color.parseColor("#8E2DE2")
        canvas.drawCircle(size / 2f, size / 2f, size / 3f, paint)

        paint.color = Color.WHITE
        paint.textSize = 48f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Puzzle Earn", size / 2f, size / 2f, paint)
        return bitmap
    }
}
