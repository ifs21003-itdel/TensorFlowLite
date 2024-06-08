package com.dicoding.asclepius.view

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.icu.text.NumberFormat
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.yalantis.ucrop.UCrop

import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentImageUri: Uri? = null
    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private var resultLabel : String? = null
    private var resultPercentage : String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.galleryButton.setOnClickListener{
            startGallery()
        }

        binding.analyzeButton.setOnClickListener{
            analyzeImage()
        }
    }

    private fun startGallery() {
        // TODO: Mendapatkan gambar dari Gallery.
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            startUCrop()
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun startUCrop() {
        val outputUri = Uri.fromFile(File(cacheDir, "cropped_image.jpg"))
        currentImageUri?.let {
            UCrop.of(it, outputUri)
                .withOptions(UCrop.Options().apply {
                    setCompressionFormat(Bitmap.CompressFormat.JPEG)
                })
                .start(this)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            val uriResult = data?.let { UCrop.getOutput(it) }
            currentImageUri = uriResult
            showImage()
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = data?.let { UCrop.getError(it) }
            Log.e("ucrop", "CROP IMAGE FAILED, PLEASE TRY AGAIN!")
        }
    }



    private fun showImage() {
        // TODO: Menampilkan gambar sesuai Gallery yang dipilih.
        binding.previewImageView.setImageURI(currentImageUri)
    }
    private fun analyzeImage() {
        // TODO: Menganalisa gambar yang berhasil ditampilkan.
        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    runOnUiThread {
                        showToast("ERROR")
                    }
                }

                override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
                    runOnUiThread {
                        results?.let { it ->
                            if (it.isNotEmpty() && it[0].categories.isNotEmpty()) {
                                val sortedCategories =
                                    it[0].categories.sortedByDescending { it?.score }

                                val threshold = 0.5f

                                // Filter hasil yang di atas ambang batas
                                val filteredResults = sortedCategories.filter { it.score > threshold }

                                if (filteredResults.isNotEmpty()) {
//                                    val displayResult =
//                                        filteredResults.joinToString("\n") {
//                                            "${it.label} " + NumberFormat.getPercentInstance()
//                                                .format(it.score).trim()
                                    resultLabel = filteredResults.joinToString { it.label }
                                    resultPercentage = filteredResults.joinToString { NumberFormat.getPercentInstance().format(it.score).trim() }
                                    moveToResult()
                                }
                            } else {
                                showToast("Tidak Dapat Diklasifikasikan")
                            }
                        }
                    }
                }
            }
        )
        currentImageUri?.let { imageClassifierHelper.classifyStaticImage(it) }
    }

    private fun moveToResult() {
        val intent = Intent(this, ResultActivity::class.java)
        resultLabel.let {
            intent.putExtra(ResultActivity.EXTRA_RESULTLABEL, it)
        }
        resultPercentage.let {
            intent.putExtra(ResultActivity.EXTRA_RESULTPERCENTAGE, it)
        }
        intent.putExtra(ResultActivity.EXTRA_IMAGE, currentImageUri.toString())
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}