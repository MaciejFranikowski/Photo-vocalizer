package com.example.photo_vocalizer.interfaces

import android.view.View

interface RecognitionCallback {
    fun takePhoto(view: View?)
    fun pickFromGallery(view: View?)
    fun classifyImage(view: View?)
}