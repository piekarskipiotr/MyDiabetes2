package com.apps.bacon.mydiabetes.ui.settings.share

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.apps.bacon.mydiabetes.ui.meal.MealActivity
import com.apps.bacon.mydiabetes.ui.product.ProductActivity
import com.apps.bacon.mydiabetes.R
import com.apps.bacon.mydiabetes.adapters.ShareMealsAdapter
import com.apps.bacon.mydiabetes.adapters.ShareProductsAdapter
import com.apps.bacon.mydiabetes.databinding.ActivityShareBinding
import com.apps.bacon.mydiabetes.utilities.BaseActivity
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DatabaseReference
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShareActivity : BaseActivity(), ShareProductsAdapter.OnShareProductListener,
    ShareMealsAdapter.OnShareMealListener {
    private lateinit var binding: ActivityShareBinding

    @Inject
    lateinit var database: DatabaseReference
    private val shareViewModel: ShareViewModel by viewModels()
    val productsAdapter = ShareProductsAdapter(this@ShareActivity)
    val mealsAdapter = ShareMealsAdapter(this@ShareActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShareBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        shareViewModel.getAllLocalProducts().observe(this, {
            productsAdapter.updateData(it)
        })

        shareViewModel.getAllLocalMeals().observe(this, {
            mealsAdapter.updateData(it)
        })

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.recyclerView.apply {
                            adapter = productsAdapter
                        }

                        binding.selectAllButton.setOnClickListener {
                            productsAdapter.selectAllProducts()
                        }
                    }
                    1 -> {
                        binding.recyclerView.apply {
                            adapter = mealsAdapter
                        }

                        binding.selectAllButton.setOnClickListener {
                            mealsAdapter.selectAllMeals()
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        addTabs()

        binding.shareButton.setOnClickListener {
            val path = System.currentTimeMillis()
            val productReference = database.child("$path/Product")
            val mealReference = database.child("$path/Meal")
            val pmjReference = database.child("$path/PMJ")
            val hPMJReference = database.child("$path/HPMJ")

            val products = productsAdapter.getDataToShare()
            val meals = mealsAdapter.getDataToShare()
            for (product in products) {
                productReference.child(product.name).setValue(product)
            }

            for (meal in meals) {
                mealReference.child(meal.name).setValue(meal)
                shareViewModel.getProductsForMeal(meal.name).observe(this, {
                    if (!it.isNullOrEmpty()) {
                        hPMJReference.child(meal.name).setValue(it)
                    }
                })
                val pmJoinList = shareViewModel.getPMJbyMealName(meal.name)
                pmjReference.child(meal.name).setValue(pmJoinList)
            }
            
            Toast.makeText(this, R.string.sent, Toast.LENGTH_SHORT).show()
            onBackPressed()
        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun addTabs() {
        binding.tabLayout.removeAllTabs()

        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText(resources.getString(R.string.products)), 0, true
        )

        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText(resources.getString(R.string.meals)), 1
        )
    }

    override fun onProductClick(productId: Int) {
        intent = Intent(this, ProductActivity::class.java)
        intent.putExtra("PRODUCT_ID", productId)
        startActivity(intent)
    }

    override fun onMealClick(mealId: Int) {
        intent = Intent(this, MealActivity::class.java)
        intent.putExtra("MEAL_ID", mealId)
        startActivity(intent)
    }

    override fun onMealCheckBoxClick(name: String, isChecked: Boolean) {
        shareViewModel.getProductsForMeal(name).observe(this, {
            if (isChecked) {
                productsAdapter.addProductsThatAreConnectedWithMeal(it)
            } else {
                productsAdapter.removeProductsThatAreConnectedWithMeal(it)
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
}