package com.example.photo_vocalizer.bitmapTransformation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import java.io.File

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

    fun getInitialRotation(photoFile: File): Bitmap {
        val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
        val exif = ExifInterface(photoFile.absolutePath)
        val rotation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        var rotationInDegrees: Int = 0
        when (rotation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotationInDegrees = 90
            ExifInterface.ORIENTATION_ROTATE_180 -> rotationInDegrees = 180
            ExifInterface.ORIENTATION_ROTATE_270 -> rotationInDegrees = 270
        }
        val matrix = Matrix()
        if (rotation != 0)
            matrix.preRotate(rotationInDegrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}