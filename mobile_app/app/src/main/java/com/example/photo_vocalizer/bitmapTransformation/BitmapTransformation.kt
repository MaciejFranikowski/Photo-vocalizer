package com.example.photo_vocalizer.bitmapTransformation

import android.graphics.Bitmap
import android.graphics.Matrix
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap

class BitmapTransformation {
    fun rotateBitmap(imageView : ImageView, degree : Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val scaledBitmap = imageView.drawable.toBitmap()
        val rotatedBitmap = Bitmap.createBitmap(scaledBitmap,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height,
            matrix,
            true)
        imageView.setImageBitmap(rotatedBitmap)
        return rotatedBitmap;
    }
}