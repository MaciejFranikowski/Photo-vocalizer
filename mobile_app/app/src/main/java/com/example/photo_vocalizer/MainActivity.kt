package com.example.photo_vocalizer

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Base64
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.photo_vocalizer.bitmapTransformation.BitmapTransformation
import com.example.photo_vocalizer.classification.Classification
import com.example.photo_vocalizer.viewModel.RecognitionResultViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var photoButton : Button
    private lateinit var galleryButton : Button
    private lateinit var listenButton : Button
    private lateinit var imageView : ImageView
    private lateinit var resultText : TextView
    private lateinit var rescaledBitmap: Bitmap
    private lateinit var photoFile: File
    private lateinit var classifier: Classification
    private lateinit var bitmapTransformation : BitmapTransformation
    private lateinit var speechRecognizer : SpeechRecognizer
    private lateinit var speechRecognizerIntent : Intent
    private lateinit var viewModel: RecognitionResultViewModel
    private val imageSize = 32
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
        viewModel = ViewModelProvider(this)[RecognitionResultViewModel::class.java]
        classifier = Classification(this, imageSize, resultText, viewModel)
        setUpSpeechRecognition()
        restoreApp()
    }

    private fun restoreApp(){
        if(viewModel.photoFileName!=""){
            if(viewModel.photoFileName.contains("jpg")){
                photoFile = File(getExternalFilesDir(null), viewModel.photoFileName)
                val bitmap = bitmapTransformation.getImageOriginalOrientation(photoFile)
                imageView.setImageBitmap(bitmap)
                rescaledBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, false)
            } else {
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, Uri.parse(viewModel.photoFileName))
                    imageView.setImageBitmap(bitmap)
                    rescaledBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, false)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        if (viewModel.textColor!=0) {
            resultText.setTextColor(viewModel.textColor)
        }
        if (viewModel.textContents != "") {
            resultText.text = viewModel.textContents
        }
    }

    private fun setUpSpeechRecognition(){
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
            if (!viewModel.isRecognizerSet) {
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

    @SuppressLint("SimpleDateFormat")
    @Suppress("UNUSED_PARAMETER")
    fun takePhoto(view: View?){
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            viewModel.photoFileName = "$timeStamp.jpg"
            photoFile = File(getExternalFilesDir(null), viewModel.photoFileName)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(
                MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(
                    this, BuildConfig.APPLICATION_ID + ".provider",
                    photoFile
                )
            )
            takePhotoIntentLauncher.launch(intent)
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), cameraRequestPermissionCode)
        }
    }

    private val takePhotoIntentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            photoFile = File(getExternalFilesDir(null), viewModel.photoFileName)
            val bitmap = bitmapTransformation.getImageOriginalOrientation(photoFile)
            imageView.setImageBitmap(bitmap)
            rescaledBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, false)
            viewModel.isImageSet = true
        } else {
            viewModel.photoFileName = ""
        }
    }

    private val pickFromGalleryIntentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult())
    {
        if (it.resultCode == Activity.RESULT_OK) {
            val data: Uri? = it.data?.data
            viewModel.photoFileName = data.toString()
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, data)
                imageView.setImageBitmap(bitmap)
                // TODO: try to integrate rescaled bitmap into the view model
                rescaledBitmap = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, false)
                viewModel.isImageSet = true
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            viewModel.photoFileName = ""
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun pickFromGallery(view: View?){
        val cameraIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickFromGalleryIntentLauncher.launch(cameraIntent)
    }

    @Suppress("UNUSED_PARAMETER")
     fun classifyImage(view: View?) {
         if(viewModel.isImageSet){
             classifier.classifyImage(this, rescaledBitmap)
         } else {
            Toast.makeText(this@MainActivity, "No image loaded", Toast.LENGTH_LONG).show()
         }
    }

    @Suppress("UNUSED_PARAMETER")
    fun rotate90(view: View){
        if(viewModel.isImageSet){
            val rotatedMap = bitmapTransformation.rotateBitmap(imageView, 90F)
            if(rotatedMap != null)
                rescaledBitmap = Bitmap.createScaledBitmap(rotatedMap, imageSize, imageSize, false)
        }
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