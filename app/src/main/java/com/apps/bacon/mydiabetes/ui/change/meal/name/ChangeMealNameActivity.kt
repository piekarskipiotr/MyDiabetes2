package com.apps.bacon.mydiabetes.ui.change.meal.name

import android.app.Activity
import android.os.Bundle
import androidx.activity.viewModels
import com.apps.bacon.mydiabetes.R
import com.apps.bacon.mydiabetes.databinding.ActivityChangeMealNameBinding
import com.apps.bacon.mydiabetes.utilities.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangeMealNameActivity : BaseActivity() {
    private lateinit var binding: ActivityChangeMealNameBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeMealNameBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val currentName = intent.getStringExtra("CURRENT_NAME") as String
        if (currentName.isNotEmpty()) {
            binding.mealNameTextInput.setText(currentName)
        }

        val errorEmptyMessage = resources.getString(R.string.empty_field_message_error)
        val errorAlreadyExistsNameMessage =
            resources.getString(R.string.meal_name_exist_error_message)

        val changeMealNameViewModel: ChangeMealNameViewModel by viewModels()
        binding.changeNameButton.setOnClickListener {
            when {
                binding.mealNameTextInput.text.isNullOrEmpty() -> binding.mealNameTextInputLayout.error =
                    errorEmptyMessage
                changeMealNameViewModel.checkForMealExist(
                    binding.mealNameTextInput.text.toString().trim().lowercase(), currentName
                ) -> binding.mealNameTextInputLayout.error = errorAlreadyExistsNameMessage
                binding.mealNameTextInput.text.toString().trim() == currentName -> onBackPressed()
                else -> {
                    binding.mealNameTextInputLayout.error = null
                    intent.putExtra("MEAL_NAME", binding.mealNameTextInput.text.toString().trim())
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }

        binding.backButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED, intent)
        super.onBackPressed()
        this.finish()
    }
}