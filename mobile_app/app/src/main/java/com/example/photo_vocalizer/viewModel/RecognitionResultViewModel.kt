package com.example.photo_vocalizer.viewModel
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel

class RecognitionResultViewModel: ViewModel() {
    var photoFileName = ""
    var textColor : Int = 0
    var textContents : String = ""
    var isImageSet : Boolean = false
    var isRecognizerSet : Boolean = false
//    var rescaledBitmap: Bitmap? = null
}