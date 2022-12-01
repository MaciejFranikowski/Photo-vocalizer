package com.example.photo_vocalizer.bitmapTransformation

import android.graphics.Bitmap
import android.graphics.Matrix
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap

class BitmapTransformation {
    fun rotateBitmap90(imageView : ImageView){
        val matrix = Matrix()
        matrix.postRotate(90F)
        val scaledBitmap = imageView.drawable.toBitmap()
        val rotatedBitmap = Bitmap.createBitmap(scaledBitmap,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height,
            matrix,
            true)
        imageView.setImageBitmap(rotatedBitmap)
    }

    fun rotateBitmap270(imageView : ImageView){
        val matrix = Matrix()
        matrix.postRotate(270F)
        val scaledBitmap = imageView.drawable.toBitmap()
        val rotatedBitmap = Bitmap.createBitmap(scaledBitmap,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height,
            matrix,
            true)
        imageView.setImageBitmap(rotatedBitmap)
    }

    fun rotateBitmap180(imageView : ImageView){
        val matrix = Matrix()
        matrix.postRotate(180F)
        val scaledBitmap = imageView.drawable.toBitmap()
        val rotatedBitmap = Bitmap.createBitmap(scaledBitmap,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height,
            matrix,
            true)
        imageView.setImageBitmap(rotatedBitmap)
    }
}