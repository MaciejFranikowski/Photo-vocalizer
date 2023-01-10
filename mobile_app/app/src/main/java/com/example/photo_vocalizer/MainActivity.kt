package com.example.photo_vocalizer

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.photo_vocalizer.bitmapTransformation.BitmapTransformation
import com.example.photo_vocalizer.classification.Classification
import com.example.photo_vocalizer.interfaces.RecognitionCallback
import com.example.photo_vocalizer.speechRecognition.SpeechRecognition
import com.example.photo_vocalizer.viewModel.RecognitionResultViewModel
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity(), RecognitionCallback {
    private lateinit var photoButton : Button
    private lateinit var galleryButton : Button
    private lateinit var listenButton : Button
    private lateinit var imageView : ImageView
    private lateinit var resultText : TextView
    private lateinit var rescaledBitmap: Bitmap
    private lateinit var photo: File
    private lateinit var classifier: Classification
    private lateinit var bitmapTransformation : BitmapTransformation
    private lateinit var viewModel: RecognitionResultViewModel
    private lateinit var speechRecognition: SpeechRecognition
    private val imageSize = 32
    private val cameraRequestPermissionCode = 101
    private val audioRequestPermissionCode = 102

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
                photo = File(getExternalFilesDir(null), viewModel.photoFileName)
                val bitmap = bitmapTransformation.getInitialRotation(photo)
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
                speechRecognition = SpeechRecognition(this,
                    viewModel,
                    bitmapTransformation,
                    imageView,
                    rescaledBitmap,
                this)
                setOnTouchListener()
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), audioRequestPermissionCode)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setOnTouchListener(){
        listenButton.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    speechRecognition.startListening()
                }
                MotionEvent.ACTION_UP -> {
                    speechRecognition.stopListening()
                }
            }
            v?.onTouchEvent(event) ?: true
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Suppress("UNUSED_PARAMETER")
    override fun takePhoto(view: View?){
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            viewModel.photoFileName = "${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}.jpg"
            photo = File(getExternalFilesDir(null), viewModel.photoFileName)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(
                MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(
                    this, BuildConfig.APPLICATION_ID + ".provider",
                    photo
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
            photo = File(getExternalFilesDir(null), viewModel.photoFileName)
            val bitmap = bitmapTransformation.getInitialRotation(photo)
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
    override fun pickFromGallery(view: View?){
        val cameraIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickFromGalleryIntentLauncher.launch(cameraIntent)
    }

    @Suppress("UNUSED_PARAMETER")
    override fun classifyImage(view: View?) {
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