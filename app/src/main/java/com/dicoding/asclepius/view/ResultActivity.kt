package com.dicoding.asclepius.view

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private var imageURI : Uri? = null
    private var resultLabel : String? = null
    private var resultPercentage : String? = null

    companion object {
        const val EXTRA_RESULTLABEL = "extra_result"
        const val EXTRA_RESULTPERCENTAGE = "extra_percentage"
        const val EXTRA_IMAGE = "extra_image"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // TODO: Menampilkan hasil gambar, prediksi, dan confidence score.
        imageURI = Uri.parse(intent.getStringExtra(EXTRA_IMAGE))
        resultLabel = intent.getStringExtra(EXTRA_RESULTLABEL)
        resultPercentage = intent.getStringExtra(EXTRA_RESULTPERCENTAGE)
        if (resultLabel == "Cancer"){
            binding.resultPercentage.setTextColor(ContextCompat.getColor(this, R.color.red))
        } else{
            binding.resultPercentage.setTextColor(ContextCompat.getColor(this, R.color.green))
        }
        displayResult()
    }

    private fun displayResult(){
        binding.resultText.text = resultLabel
        binding.resultPercentage.text = resultPercentage
        binding.resultImage.setImageURI(imageURI)
    }


}