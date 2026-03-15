package com.example.saleapp.ui.store

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.saleapp.R
import com.example.saleapp.data.model.StoreDto
import com.example.saleapp.databinding.BottomSheetStoreDetailBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class StoreDetailBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetStoreDetailBinding? = null
    private val binding get() = _binding!!

    private var store: StoreDto? = null

    companion object {
        private const val STORE_KEY = "store"

        fun newInstance(store: StoreDto): StoreDetailBottomSheetFragment {
            return StoreDetailBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(STORE_KEY, store)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            @Suppress("DEPRECATION")
            store = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                it.getSerializable(STORE_KEY, StoreDto::class.java)
            } else {
                it.getSerializable(STORE_KEY) as? StoreDto
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetStoreDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        store?.let { storeData ->
            setupUI(storeData)
        }
    }

    private fun setupUI(store: StoreDto) {
        // Set store ID as name
        binding.storeName.text = "Chi nhánh ${store.locationId}"

        // Set address
        binding.storeAddress.text = store.address

        // Hide image, phone, email, rating, hours, description (không có dữ liệu từ backend)
        binding.storeImage.visibility = View.GONE
        binding.storePhone.visibility = View.GONE
        binding.storeEmail.visibility = View.GONE
        binding.ratingContainer.visibility = View.GONE
        binding.hoursContainer.visibility = View.GONE
        binding.descriptionContainer.visibility = View.GONE

        // Ẩn các button actions vì không có phone/email
        binding.callButton.visibility = View.GONE
        binding.emailButton.visibility = View.GONE

        // Setup Google Maps button (chỉ dùng location)
        binding.locationButton.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:${store.latitude},${store.longitude}?z=15&q=${store.address}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            try {
                startActivity(mapIntent)
            } catch (e: Exception) {
                // Google Maps không cài đặt, dùng intent mặc định
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/maps?q=${store.latitude},${store.longitude}"))
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

