package com.example.photo_vocalizer.speechRecognition

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.ImageView
import android.widget.Toast
import com.example.photo_vocalizer.bitmapTransformation.BitmapTransformation
import com.example.photo_vocalizer.interfaces.RecognitionCallback
import com.example.photo_vocalizer.viewModel.RecognitionResultViewModel

class SpeechRecognition(
    context: Context,
    viewModel: RecognitionResultViewModel,
    bitmapTransformation: BitmapTransformation,
    imageView: ImageView,
    rescaledBitmap: Bitmap,
    callback: RecognitionCallback
) {
    private lateinit var speechRecognizer : SpeechRecognizer
    private lateinit var speechRecognizerIntent : Intent
    private val languageCode = "pl-PL"
    private val regex0 = "obróć".toRegex(RegexOption.IGNORE_CASE)
    private val regexRight = "prawo|90".toRegex(RegexOption.IGNORE_CASE)
    private val regexLeft = "lewo|270".toRegex(RegexOption.IGNORE_CASE)
    private val regexFlip = "odwróć|180".toRegex(RegexOption.IGNORE_CASE)
    private val regexPick = "wybierz".toRegex(RegexOption.IGNORE_CASE)
    private val regexTake = "zrób|wykonaj".toRegex(RegexOption.IGNORE_CASE)
    private val regexClassify = "klasyfik".toRegex(RegexOption.IGNORE_CASE)
    private val regexConfirm = "Potwierdzam".toRegex(RegexOption.IGNORE_CASE)
    private val generalRecognitionCode = 0
    private val askForDegreeCode = 1
    private val askForConfirmationCode = 2
    private var recognitionStatus = 0
    private val imageSize = 32
    private val context : Context
    private var viewModel : RecognitionResultViewModel
    private var bitmapTransformation : BitmapTransformation
    private var imageView : ImageView
    private var rescaledBitmap: Bitmap
    private val callback : RecognitionCallback


    init {
        this.context = context
        this.viewModel = viewModel
        this.bitmapTransformation = bitmapTransformation
        this.imageView = imageView
        this.rescaledBitmap = rescaledBitmap
        this.callback = callback
        createSpeechRecognizer()
    }

    private fun createSpeechRecognizer() : Boolean{
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(bytes: ByteArray) {}
            override fun onEndOfSpeech() {}
            override fun onError(i: Int) {}
            override fun onPartialResults(bundle: Bundle) {}
            override fun onEvent(i: Int, bundle: Bundle) {}
            override fun onResults(bundle: Bundle) {
                val data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if(data?.get(0)?.isNotEmpty() == true)
                    useRecognitionResult(data[0])
            }
        })
        return true
    }

    private fun useRecognitionResult(recognitionResult: String){
        when(recognitionStatus) {
            generalRecognitionCode -> generalRecognition(recognitionResult)
            askForDegreeCode -> askForDegreeRecognition(recognitionResult)
            askForConfirmationCode -> askForConfirmationRecognition(recognitionResult)
        }
    }

    private fun generalRecognition(recognitionResult: String){
        if(regex0.find(recognitionResult) != null){
            if(regexRight.find(recognitionResult) != null && viewModel.isImageSet){
                val rotatedMap : Bitmap? = bitmapTransformation.rotateBitmap(imageView, 90F)
                if(rotatedMap != null)
                    rescaledBitmap = Bitmap.createScaledBitmap(rotatedMap, imageSize, imageSize, false)
                return
            }
            if(regexLeft.find(recognitionResult) != null && viewModel.isImageSet){
                val rotatedMap : Bitmap? = bitmapTransformation.rotateBitmap(imageView, 270F)
                if(rotatedMap != null)
                    rescaledBitmap = Bitmap.createScaledBitmap(rotatedMap, imageSize, imageSize, false)
                return
            }
            if(regexFlip.find(recognitionResult) != null && viewModel.isImageSet){
                val rotatedMap : Bitmap? = bitmapTransformation.rotateBitmap(imageView, 180F)
                if(rotatedMap != null)
                    rescaledBitmap = Bitmap.createScaledBitmap(rotatedMap, imageSize, imageSize, false)
                return
            }
            askForDegree()
            return
        }
        if(regexFlip.find(recognitionResult) != null && viewModel.isImageSet){
            val rotatedMap : Bitmap? = bitmapTransformation.rotateBitmap(imageView, 180F)
            if(rotatedMap != null)
                rescaledBitmap = Bitmap.createScaledBitmap(rotatedMap, imageSize, imageSize, false)
        }

        if(regexPick.find(recognitionResult) != null){
            callback.pickFromGallery(null)
            return
        }

        if(regexTake.find(recognitionResult) != null){
            askForConfirmation()
            return
        }
        if(regexClassify.find(recognitionResult) != null){
            callback.classifyImage(null)
            return
        }
    }

    private fun askForDegreeRecognition(recognitionResult: String) {
        if(regexRight.find(recognitionResult) != null && viewModel.isImageSet){
            val rotatedMap : Bitmap? =  bitmapTransformation.rotateBitmap(imageView, 90F)
            if(rotatedMap != null)
                rescaledBitmap = Bitmap.createScaledBitmap(rotatedMap, imageSize, imageSize, false)
        }

        if(regexLeft.find(recognitionResult) != null && viewModel.isImageSet) {
            val rotatedMap : Bitmap? =  bitmapTransformation.rotateBitmap(imageView, 270F)
            if(rotatedMap != null)
                rescaledBitmap = Bitmap.createScaledBitmap(rotatedMap, imageSize, imageSize, false)
        }
        if(regexFlip.find(recognitionResult) != null && viewModel.isImageSet){
            val rotatedMap : Bitmap? =  bitmapTransformation.rotateBitmap(imageView, 180F)
            if(rotatedMap != null)
                rescaledBitmap = Bitmap.createScaledBitmap(rotatedMap, imageSize, imageSize, false)
        }
        recognitionStatus = generalRecognitionCode
    }

    fun startListening(){
        speechRecognizer.startListening(speechRecognizerIntent)
    }

    fun stopListening(){
        speechRecognizer.stopListening()
    }

    private fun askForConfirmationRecognition(recognitionResult: String){
        if(regexConfirm.find(recognitionResult) != null){
            callback.takePhoto(null)
        }
        recognitionStatus = generalRecognitionCode
    }

    private fun askForDegree(){
        Toast.makeText(context, "Powiedz jak obrócić obraz", Toast.LENGTH_LONG).show()
        recognitionStatus = askForDegreeCode
    }

    private fun askForConfirmation(){
        Toast.makeText(context, "Powtwierdź mówiąc 'Potwierdzam'", Toast.LENGTH_LONG).show()
        recognitionStatus = askForConfirmationCode
    }

}