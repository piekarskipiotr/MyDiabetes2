package com.apps.bacon.mydiabetes

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_product_barcode.*

class ProductBarcodeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_barcode)
        val errorMessage = "Pole nie może być puste"

        saveButton.setOnClickListener {
            if(barcodeTextInput.text.isNullOrEmpty())
                barcodeTextInputLayout.error = errorMessage

            else{
                barcodeTextInputLayout.error = null
                intent.putExtra("BARCODE", barcodeTextInput.text.toString().trim())
                setResult(Activity.RESULT_OK, intent)
                finish()

            }
        }

        backButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }
}