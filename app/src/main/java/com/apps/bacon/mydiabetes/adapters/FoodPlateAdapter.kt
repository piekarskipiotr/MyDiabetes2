package com.apps.bacon.mydiabetes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.apps.bacon.mydiabetes.R
import com.apps.bacon.mydiabetes.data.Product
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.product_item_food_plate.view.*

class FoodPlateAdapter constructor(
    private val listener: OnProductClickListener
) : RecyclerView.Adapter<FoodPlateAdapter.ViewHolder>() {
    private var data: List<Product> = ArrayList()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val productName: TextView = view.productName
        val measure: TextInputEditText = view.measureTextInput
        val measureLayout: TextInputLayout = view.measureTextInputLayout
        val carbohydrateExchangers: TextView = view.carbohydrateExchangers
        val proteinFatExchangers: TextView = view.proteinFatExchangers
        val calories: TextView = view.calories

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            listener.onProductClick(data[adapterPosition].id)
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_item_food_plate, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.productName.text = data[position].name
        if(data[position].weight == null){
            holder.measure.setText(data[position].pieces.toString())
            holder.measureLayout.prefixText = "g/ml"
        }else{
            holder.measure.setText(data[position].weight.toString())
            holder.measureLayout.prefixText = "szt."
        }


        holder.carbohydrateExchangers.text = data[position].carbohydrateExchangers.toString()
        holder.proteinFatExchangers.text = data[position].proteinFatExchangers.toString()
        holder.calories.text = data[position].calories.toString()
    }

    override fun getItemCount(): Int = data.size

    fun updateData(dataList: List<Product>){
        data = dataList
        notifyDataSetChanged()
    }

    interface OnProductClickListener {
        fun onProductClick(productID: Int)

    }
}