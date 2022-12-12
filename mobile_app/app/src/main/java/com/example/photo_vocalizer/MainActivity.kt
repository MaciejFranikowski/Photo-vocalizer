package com.example.photo_vocalizer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.photo_vocalizer.bitmapTransformation.BitmapTransformation
import com.example.photo_vocalizer.classification.Classification
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private lateinit var photoButton : Button
    private lateinit var galleryButton : Button
    private lateinit var listenButton : Button
    private lateinit var imageView : ImageView
    private lateinit var resultText : TextView
    private lateinit var bitmap: Bitmap
    private lateinit var rescaledBitmap: Bitmap
    private lateinit var classifier: Classification
    private lateinit var bitmapTransformation : BitmapTransformation
    private lateinit var speechRecognizer : SpeechRecognizer
    private lateinit var speechRecognizerIntent : Intent
    private var isRecognizerSet : Boolean = false
    private var isImageSet : Boolean = false
    private val imageSize = 32
    private val cameraRequestCode = 1
    private val galleryRequestCode = 3
    private val cameraRequestPermissionCode = 101
    private val audioRequestPermissionCode = 102
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        assignReferences()
        bitmapTransformation = BitmapTransformation()
        classifier = Classification(this, imageSize, resultText)
        setUpSpeechRecognition()
    }

    private fun setUpSpeechRecognition(){
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
            if (!isRecognizerSet) {
                createSpeechRecognizer()
                setOnTouchListener()
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), audioRequestPermissionCode)
        }
    }

    private fun createSpeechRecognizer() : Boolean{
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
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
            if(regexRight.find(recognitionResult) != null && isImageSet){
                bitmapTransformation.rotateBitmap90(imageView)
                return
            }
            if(regexLeft.find(recognitionResult) != null && isImageSet){
                bitmapTransformation.rotateBitmap270(imageView)
                return
            }
            if(regexFlip.find(recognitionResult) != null && isImageSet){
                bitmapTransformation.rotateBitmap180(imageView)
                return
            }
            askForDegree()
            return
        }
        if(regexFlip.find(recognitionResult) != null && isImageSet)
            bitmapTransformation.rotateBitmap180(imageView)

        if(regexPick.find(recognitionResult) != null){
            pickFromGallery(null)
            return
        }

        if(regexTake.find(recognitionResult) != null){
            askForConfirmation()
            return
        }
        if(regexClassify.find(recognitionResult) != null){
            classifyImage(null)
            return
        }
    }

    private fun askForDegreeRecognition(recognitionResult: String) {
        if(regexRight.find(recognitionResult) != null && isImageSet)
            bitmapTransformation.rotateBitmap90(imageView)
        if(regexLeft.find(recognitionResult) != null && isImageSet)
            bitmapTransformation.rotateBitmap270(imageView)
        if(regexFlip.find(recognitionResult) != null && isImageSet)
            bitmapTransformation.rotateBitmap180(imageView)

        recognitionStatus = generalRecognitionCode
    }

    private fun askForConfirmationRecognition(recognitionResult: String){
        if(regexConfirm.find(recognitionResult) != null){
            takePhoto(null)
        }
        recognitionStatus = generalRecognitionCode
    }

    private fun askForDegree(){
        Toast.makeText(this, "Powiedz jak obrócić obraz", Toast.LENGTH_LONG).show()
        recognitionStatus = askForDegreeCode
    }

    private fun askForConfirmation(){
        Toast.makeText(this, "Powtwierdź mówiąc 'Potwierdzam'", Toast.LENGTH_LONG).show()
        recognitionStatus = askForConfirmationCode
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setOnTouchListener(){
        listenButton.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    speechRecognizer.startListening(speechRecognizerIntent)
                }
                MotionEvent.ACTION_UP -> {
                    speechRecognizer.stopListening()
                }
            }
            v?.onTouchEvent(event) ?: true
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun takePhoto(view: View?){
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, cameraRequestCode)
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), cameraRequestPermissionCode)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun pickFromGallery(view: View?){
        val cameraIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(cameraIntent, galleryRequestCode)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == cameraRequestCode) {
                bitmap = (data?.extras!!["data"] as Bitmap?)!!
                val dimension = bitmap.width.coerceAtMost(bitmap.height)
                bitmap = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension)
                imageView.setImageBitmap(bitmap)
                rescaledBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, false)
                isImageSet = true
            }
            if (requestCode == galleryRequestCode) {
                val dat: Uri? = data?.data
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, dat)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                imageView.setImageBitmap(bitmap)
                rescaledBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, false)
                isImageSet = true
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
     fun classifyImage(view: View?) {
         if(isImageSet){
             classifier.classifyImage(this, rescaledBitmap)
         } else {
            Toast.makeText(this@MainActivity, "No image loaded", Toast.LENGTH_LONG).show()
         }
    }

    @Suppress("UNUSED_PARAMETER")
    fun rotate90(view: View){
        if(isImageSet)
            bitmapTransformation.rotateBitmap90(imageView)
    }

    private fun assignReferences(){
        photoButton = findViewById(R.id.photoButton)
        galleryButton = findViewById(R.id.galleryButton)
        imageView = findViewById(R.id.imageView)
        resultText = findViewById(R.id.resultTextView)
        listenButton = findViewById(R.id.listenButton)
        rescaledBitmap = Bitmap.createBitmap(32,32, Bitmap.Config.ARGB_8888)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            cameraRequestPermissionCode -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("TAG", "Permission has been denied by user")
                } else {
                    Log.i("TAG", "Permission has been granted by user")
                }
            }
            audioRequestPermissionCode -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("TAG", "Permission has been denied by user")
                } else {
                    Log.i("TAG", "Permission has been granted by user")
                }
            }
        }
    }

}