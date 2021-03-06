package com.apps.bacon.mydiabetes.ui.product

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.apps.bacon.mydiabetes.*
import com.apps.bacon.mydiabetes.adapters.ImageAdapter
import com.apps.bacon.mydiabetes.data.*
import com.apps.bacon.mydiabetes.data.entities.Image
import com.apps.bacon.mydiabetes.data.entities.Product
import com.apps.bacon.mydiabetes.databinding.*
import com.apps.bacon.mydiabetes.ui.barcode.ProductBarcodeActivity
import com.apps.bacon.mydiabetes.ui.camera.CameraActivity
import com.apps.bacon.mydiabetes.ui.camera.ScannerCameraActivity
import com.apps.bacon.mydiabetes.ui.change.product.name.ChangeProductNameActivity
import com.apps.bacon.mydiabetes.utilities.BaseActivity
import com.apps.bacon.mydiabetes.ui.settings.add.tag.AddTagActivity
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.database.DatabaseReference
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

@AndroidEntryPoint
class ProductActivity : BaseActivity(), ImageAdapter.OnImageClickListener {
    private lateinit var product: Product
    private val productViewModel: ProductViewModel by viewModels()
    private lateinit var imagesAdapter: ImageAdapter
    private lateinit var binding: ActivityProductBinding

    @Inject
    lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val bottomSheetDialogCameraViewBinding = DialogAddImageBinding.inflate(layoutInflater)
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val productId = intent.getIntExtra("PRODUCT_ID", -1)

        product = productViewModel.getProduct(productId)
        initRecyclerView()
        setProductInfo()

        productViewModel.getImageByProductId(product.id).observe(this, {
            imagesAdapter.updateData(it)
        })

        binding.productName.setOnClickListener {
            intent = Intent(this, ChangeProductNameActivity::class.java)
            intent.putExtra("CURRENT_NAME", product.name)
            getProductName.launch(intent)
        }

        binding.scanBarcodeButton.setOnClickListener {
            intent = Intent(this, ScannerCameraActivity::class.java)
            getBarcode.launch(intent)
        }

        binding.manualBarcode.setOnClickListener {
            intent = Intent(this, ProductBarcodeActivity::class.java)
            if (product.barcode != null)
                intent.putExtra("BARCODE", product.barcode)

            getBarcode.launch(intent)
        }

        binding.takePhotoButton.setOnClickListener {
            bottomSheetDialog.setContentView(bottomSheetDialogCameraViewBinding.root)
            bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetDialog.show()

            bottomSheetDialogCameraViewBinding.cameraButton.setOnClickListener {
                intent = Intent(this, CameraActivity::class.java)
                getImage.launch(intent)
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialogCameraViewBinding.galleryButton.setOnClickListener {
                val gallery = Intent(Intent.ACTION_PICK)
                gallery.type = "image/*"

                getImageFromGallery.launch(gallery)
                bottomSheetDialog.dismiss()
            }
        }

        if (product.inFoodPlate) {
            binding.addButton.isClickable = false
            binding.addButton.alpha = 0.8f
        } else {
            binding.addButton.setOnClickListener {
                productViewModel.update(product.apply {
                    inFoodPlate = true
                })
                binding.addButton.isClickable = false
                binding.addButton.alpha = 0.8f
                Toast.makeText(
                    this,
                    resources.getString(R.string.product_added),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.moreButton.setOnClickListener {
            dialogMore()
        }
    }

    private fun initRecyclerView() {
        binding.photosRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            imagesAdapter = ImageAdapter(this@ProductActivity)
            adapter = imagesAdapter
        }
    }

    private fun setProductInfo() {
        binding.productName.text = product.name
        val measureText: String = if (product.weight != null)
            "(${resources.getString(R.string.for_smth)} ${product.weight} g/ml)"
        else
            "(${resources.getString(R.string.for_smth)} ${product.pieces} ${resources.getString(R.string.pieces_shortcut)})"

        binding.measureOfProductValues.text = measureText
        binding.measureOfProductExchangers.text = measureText

        binding.carbohydrates.text = product.carbohydrates.toString()
        binding.calories.text = product.calories.toString()
        if (product.protein != null) {
            binding.protein.text = product.protein.toString()
            binding.fat.text = product.fat.toString()
        } else {
            binding.proteinContainer.visibility = View.GONE
            binding.fatContainer.visibility = View.GONE
        }

        pieChart(product.carbohydrateExchangers, product.proteinFatExchangers)

        if (product.tag == null) {
            addChip(resources.getString(R.string.set_the_tag), 0)
        } else {
            val tag = productViewModel.getTagById(product.tag!!)
            addChip(tag.name, tag.id)
        }

        if (product.barcode == null) {
            binding.manualBarcode.text = resources.getString(R.string.barcode_manually)
        } else {
            binding.manualBarcode.text = product.barcode
        }
    }

    private fun addChip(label: String, ID: Int) {
        binding.tagChipContainer.removeAllViewsInLayout()
        binding.tagChipContainer.addChip(label, ID)
        binding.tagChipContainer[0].setOnClickListener {
            if (product.tag == null) {
                intent = Intent(this, AddTagActivity::class.java)
                intent.putExtra("TAG_MANAGER", true)
                getTag.launch(intent)
            } else
                dialogManagerTag(label)
        }
    }

    private fun ChipGroup.addChip(label: String, ID: Int) {
        Chip(this@ProductActivity, null, R.attr.CustomChip).apply {
            id = ID
            text = label
            isClickable = true
            isCheckedIconVisible = false
            isFocusable = true
            addView(this)
        }
    }

    private fun dialogDeleteProduct() {
        val alertDialog: AlertDialog
        val builder = AlertDialog.Builder(this, R.style.DialogStyle)
        val dialogBinding = DialogDeleteProductBinding.inflate(LayoutInflater.from(this))
        builder.setView(dialogBinding.root)
        alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)

        dialogBinding.productNameText.text = product.name

        dialogBinding.backButton.setOnClickListener {
            alertDialog.dismiss()
        }

        dialogBinding.deleteButton.setOnClickListener {
            productViewModel.delete(product)
            finish()
        }
        alertDialog.show()
    }

    private fun dialogMore() {
        val alertDialog: AlertDialog
        val builder = AlertDialog.Builder(this, R.style.DialogStyle)
        val dialogBinding = DialogMoreBinding.inflate(LayoutInflater.from(this))
        builder.setView(dialogBinding.root)
        alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)

        dialogBinding.exportButton.setOnClickListener {
            dialogExport()
        }

        dialogBinding.deleteButton.setOnClickListener {
            if (productViewModel.isProductInMeal(product.name))
                Toast.makeText(
                    applicationContext,
                    resources.getString(R.string.delete_product_in_meal_message),
                    Toast.LENGTH_SHORT
                ).show()
            else
                dialogDeleteProduct()
        }

        dialogBinding.backButton.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun dialogExport() {
        val alertDialog: AlertDialog
        val builder = AlertDialog.Builder(this, R.style.DialogStyle)
        val dialogBinding = DialogShareBinding.inflate(LayoutInflater.from(this))
        builder.setView(dialogBinding.root)
        alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)

        dialogBinding.shareButton.setOnClickListener {
            val productReference = database.child("Product/${product.name}")
            productReference.child(System.currentTimeMillis().toString()).setValue(product)
            alertDialog.dismiss()
        }

        dialogBinding.backButton.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun dialogManagerTag(tagName: String) {
        val alertDialog: AlertDialog
        val builder = AlertDialog.Builder(this, R.style.DialogStyle)
        val dialogBinding = DialogManagerTagBinding.inflate(LayoutInflater.from(this))
        builder.setView(dialogBinding.root)
        alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)

        dialogBinding.tagNameText.text = tagName

        dialogBinding.backButton.setOnClickListener {
            alertDialog.dismiss()
        }

        dialogBinding.deleteButton.setOnClickListener {
            product.tag = null
            productViewModel.update(product)
            setProductInfo()
            alertDialog.dismiss()
        }

        dialogBinding.changeButton.setOnClickListener {
            intent = Intent(this, AddTagActivity::class.java)
            intent.putExtra("TAG_MANAGER", true)
            getTag.launch(intent)
            alertDialog.dismiss()

        }
        alertDialog.show()
    }

    private fun dialogImageManager(image: Image) {
        val alertDialog: AlertDialog
        val builder = AlertDialog.Builder(this, R.style.DialogStyle)
        val dialogBinding = DialogManagerImageBinding.inflate(LayoutInflater.from(this))
        builder.setView(dialogBinding.root)
        alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)

        dialogBinding.imagePreview.setImageURI(Uri.parse(image.image))

        dialogBinding.backButton.setOnClickListener {
            alertDialog.dismiss()
        }

        dialogBinding.deleteButton.setOnClickListener {
            product.icon = null
            productViewModel.update(product)
            productViewModel.delete(image)

            alertDialog.dismiss()
        }

        dialogBinding.setAsIconButton.setOnClickListener {
            product.icon = image.image
            productViewModel.update(product)
            alertDialog.dismiss()

        }

        alertDialog.show()
    }

    private fun pieChart(carbohydrateExchangers: Double, proteinFatExchangers: Double) {
        val pieChart: PieChart = binding.pieChart
        val data = ArrayList<PieEntry>()
        if (carbohydrateExchangers != 0.0)
            data.add(
                PieEntry(
                    carbohydrateExchangers.toFloat(),
                    resources.getString(R.string.pie_label_carbohydrate)
                )
            )
        if (proteinFatExchangers != 0.0)
            data.add(
                PieEntry(
                    proteinFatExchangers.toFloat(),
                    resources.getString(R.string.pie_label_protein_fat)
                )
            )

        val dataSet = PieDataSet(data, "")
        dataSet.setColors(
            ContextCompat.getColor(this, R.color.strong_yellow),
            ContextCompat.getColor(this, R.color.blue_purple)
        )

        dataSet.valueTextColor = ContextCompat.getColor(this, R.color.black)
        dataSet.valueTextSize = 16f

        val pieData = PieData(dataSet)
        pieData.setValueFormatter(DefaultValueFormatter(1))

        pieChart.data = pieData
        pieChart.description.isEnabled = false

        pieChart.legend.textColor = ContextCompat.getColor(this, R.color.independent)

        pieChart.setDrawEntryLabels(false)
        pieChart.isDrawHoleEnabled = false

        pieChart.rotationAngle = 50f
        pieChart.animateY(1400, Easing.EaseInOutQuad)
        pieChart.animate()
    }

    private val getProductName =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                activityResult.data?.let {
                    val oldName = product.name
                    val newName = it.getStringExtra("PRODUCT_NAME") as String
                    productViewModel.renamePMJProductName(product, oldName, newName)
                    binding.productName.text = newName
                }
            }
        }

    private val getTag =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                activityResult.data?.let {
                    if (it.getBooleanExtra("NEW_TAG", false))
                        product.tag = productViewModel.getLastId()
                    else
                        product.tag = it.getIntExtra("TAG_ID", -1)

                    productViewModel.update(product)
                    if (product.tag == null)
                        addChip(resources.getString(R.string.set_the_tag), 0)
                    else {
                        val tag = productViewModel.getTagById(product.tag!!)
                        addChip(tag.name, tag.id)
                    }
                }
            }
        }

    private val getBarcode =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                activityResult.data?.let {
                    if (it.getBooleanExtra("DELETE_BARCODE", false)) {
                        product.barcode = null
                        productViewModel.update(product)
                        setProductInfo()
                    } else {
                        val barcode = it.getStringExtra("BARCODE")!!
                        product.barcode = barcode
                        productViewModel.update(product)
                        setProductInfo()
                    }
                }
            }
        }

    private val getImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                activityResult.data?.let {
                    val imageUri = it.getStringExtra("IMAGE_URI").toString()
                    productViewModel.insert(
                        Image(0, product.id, null, imageUri)
                    )
                }
            }
        }

    private val getImageFromGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                activityResult.data?.let {
                    val photoFile = File(
                        getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "${System.currentTimeMillis()}.jpg"
                    )
                    photoFile.createNewFile()
                    val out = FileOutputStream(photoFile)
                    out.write(
                        getBytes(this.contentResolver.openInputStream(it.data!!)!!)
                    )
                    out.close()

                    productViewModel.insert(
                        Image(0, product.id, null, Uri.fromFile(photoFile).toString())
                    )
                }
            }
        }

    private fun getBytes(inputStream: InputStream): ByteArray {
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)

        while (true) {
            val len = inputStream.read(buffer)
            if (len != -1)
                byteBuffer.write(buffer, 0, len)
            else
                break
        }
        return byteBuffer.toByteArray()
    }

    override fun onImageLongClick(image: Image) {
        dialogImageManager(image)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
}