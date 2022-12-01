package com.example.photo_vocalizer.speechRecognition

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class SpeechRecognition() {
    private lateinit var speechRecognizer : SpeechRecognizer
    private lateinit var speechRecognizerIntent : Intent
    private var isRecognizerSet : Boolean = false
    private val languageCode = "pl-PL"

    fun createSpeechRecognizer(context: Context) : Boolean{
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
//                if(data?.get(0)?.isNotEmpty() == true)
//                    useRecognitionResult(data[0])
            }
        })
        return true
    }

//    private fun useRecognitionResult(recognitionResult: String){
//        val regex0 = "obróć".toRegex(RegexOption.IGNORE_CASE)
//        val regexRight = "prawo|90".toRegex(RegexOption.IGNORE_CASE)
//        val regexLeft = "lewo|270".toRegex(RegexOption.IGNORE_CASE)
//        val regexFlip = "odwróć|180".toRegex(RegexOption.IGNORE_CASE)
//        val regexPick = "wybierz".toRegex(RegexOption.IGNORE_CASE)
//        val regexTake = "zrób|wykonaj".toRegex(RegexOption.IGNORE_CASE)
//        val regexClassify = "klasyfik".toRegex(RegexOption.IGNORE_CASE)
//        if(regex0.find(recognitionResult) != null){
//            if(regexRight.find(recognitionResult) != null)
//                rotateBitmapRight(null)
//            if(regexLeft.find(recognitionResult) != null)
//                rotateBitmapLeft(null)
//            return
//        }
//        if(regexFlip.find(recognitionResult) != null)
//            flipBitmap(null)
//
//        if(regexPick.find(recognitionResult) != null){
//            pickFromGallery(null)
//            return
//        }
//
//        if(regexTake.find(recognitionResult) != null){
//            takePhoto(null)
//            return
//        }
//        if(regexClassify.find(recognitionResult) != null){
//            classifyImage(null)
//            return
//        }
//    }
}