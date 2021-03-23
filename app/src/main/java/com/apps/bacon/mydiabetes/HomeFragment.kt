package com.apps.bacon.mydiabetes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.apps.bacon.mydiabetes.adapters.PagingProductsAdapter
import com.apps.bacon.mydiabetes.databinding.FragmentHomeBinding
import com.apps.bacon.mydiabetes.viewmodel.HomeViewModel
import com.apps.bacon.mydiabetes.viewmodel.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(), PagingProductsAdapter.OnProductClickListener {
    private lateinit var productsAdapter: PagingProductsAdapter
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val homeViewModel by activityViewModels<HomeViewModel>()
        val productViewModel: ProductViewModel by viewModels()

        initRecyclerView()

        homeViewModel.currentTag.observe(viewLifecycleOwner, { selectedTab ->
            if (selectedTab == 0) {
               viewLifecycleOwner.lifecycleScope.launch{
                    productViewModel.getPagingListOfProducts().collectLatest {
                        productsAdapter.submitData(it)
                    }
                }
            } else {
                viewLifecycleOwner.lifecycleScope.launch{
                    productViewModel.getPagingListOfProductsByTag(selectedTab).collectLatest {
                        productsAdapter.submitData(it)
                    }
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            productsAdapter = PagingProductsAdapter(this@HomeFragment)
            adapter = productsAdapter

        }
    }

    override fun onProductClick(productId: Int, isEditable: Boolean) {
        if (isEditable) {
            val intent = Intent(activity, ProductActivity::class.java)
            intent.putExtra("PRODUCT_ID", productId)
            startActivity(intent)
        } else {
            val intent = Intent(activity, StaticProductActivity::class.java)
            intent.putExtra("PRODUCT_ID", productId)
            startActivity(intent)
        }
    }
}