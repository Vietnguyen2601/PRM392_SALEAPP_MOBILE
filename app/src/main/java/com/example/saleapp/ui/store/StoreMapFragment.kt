package com.example.saleapp.ui.store

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.saleapp.core.utils.UiState
import com.example.saleapp.data.model.StoreDto
import com.example.saleapp.databinding.FragmentStoreMapBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

@AndroidEntryPoint
class StoreMapFragment : Fragment() {

    private var _binding: FragmentStoreMapBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StoreMapViewModel by viewModels()

    private lateinit var stores: List<StoreDto>
    private var isWebMapReady = false
    private var pendingStores: List<StoreDto>? = null

    companion object {
        private const val TAG = "StoreMapFragment"
    }

    private inner class WebMapBridge {
        @JavascriptInterface
        fun onStoreMarkerClick(locationId: Int) {
            activity?.runOnUiThread {
                val store = stores.firstOrNull { it.locationId == locationId }
                if (store != null) {
                    showStoreDetails(store)
                }
            }
        }

        @JavascriptInterface
        fun openExternalMap(latitude: Double, longitude: Double, label: String) {
            activity?.runOnUiThread {
                val encodedLabel = Uri.encode(label)
                val gmmIntentUri = Uri.parse("geo:0,0?q=$latitude,$longitude($encodedLabel)")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                    setPackage("com.google.android.apps.maps")
                }

                if (mapIntent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    val fallbackIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://maps.google.com/maps?q=$latitude,$longitude")
                    )
                    startActivity(fallbackIntent)
                }
            }
        }

        @JavascriptInterface
        fun reportMapLoadFailure() {
            viewLifecycleOwner.lifecycleScope.launch {
                val details = diagnoseMapConnectivity()
                val safe = details.replace("'", "\\'")
                binding.mapWebView.evaluateJavascript("window.setDiagnosticStatus('$safe');", null)
                Log.e(TAG, "Map diagnostics: $details")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupWebMapView()
        observeViewModelStates()
        loadStores()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupWebMapView() {
        binding.mapView.visibility = View.GONE
        binding.mapWebView.visibility = View.VISIBLE

        val webView = binding.mapWebView
        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = false
        webView.settings.allowContentAccess = false
                webView.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isWebMapReady = true
                                pendingStores?.let {
                                        renderStoresOnWebMap(it)
                                        pendingStores = null
                                }
                        }

                        override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                        ) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                        Toast.makeText(requireContext(), "Không tải được trang bản đồ", Toast.LENGTH_SHORT).show()
                                }
                        }
                }
        webView.addJavascriptInterface(WebMapBridge(), "AndroidBridge")

        webView.loadDataWithBaseURL(
            "https://localhost/",
                        buildNoKeyMapHtml(),
            "text/html",
            "UTF-8",
            null
        )
    }

        private fun buildNoKeyMapHtml(): String {
        return """
            <!doctype html>
            <html>
            <head>
              <meta charset=\"utf-8\" />
              <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />
              <style>
                                html, body {
                                    height: 100%;
                                    margin: 0;
                                    padding: 0;
                                    font-family: sans-serif;
                                    background: #f2f2f2;
                                }
                                #wrap {
                                    display: flex;
                                    flex-direction: column;
                                    height: 100%;
                                }
                                #status {
                                    padding: 8px 10px;
                                    font-size: 12px;
                                    color: #333;
                                    background: #ffffff;
                                    border-bottom: 1px solid #e0e0e0;
                                }
                                #mapframe {
                                    flex: 1;
                                    width: 100%;
                                    border: none;
                                    background: #ddd;
                                }
                                #store-list {
                                    max-height: 140px;
                                    overflow-y: auto;
                                    margin: 0;
                                    padding: 6px;
                                    list-style: none;
                                    background: #fff;
                                    border-top: 1px solid #e0e0e0;
                                }
                                #store-list li {
                                    padding: 8px;
                                    margin-bottom: 6px;
                                    background: #f7f7f7;
                                    border-radius: 6px;
                                    font-size: 12px;
                                }
                                #store-list button {
                                    border: none;
                                    background: #1e88e5;
                                    color: white;
                                    padding: 6px 8px;
                                    border-radius: 6px;
                                }
              </style>
            </head>
            <body>
                            <div id="wrap">
                                <div id="status">Dang tai ban do...</div>
                                <iframe id="mapframe" referrerpolicy="no-referrer-when-downgrade" allowfullscreen></iframe>
                                <ul id="store-list"></ul>
                            </div>
              <script>
                                var storesCache = [];

                                function buildGoogleEmbedUrl(lat, lon, zoom) {
                                    return 'https://maps.google.com/maps?q=' + lat + ',' + lon + '&z=' + zoom + '&output=embed';
                                }

                                function buildOsmEmbedUrl(lat, lon, zoom) {
                                    var dLat = 0.02;
                                    var dLon = 0.02;
                                    var bbox = (lon - dLon).toFixed(6) + ','
                                             + (lat - dLat).toFixed(6) + ','
                                             + (lon + dLon).toFixed(6) + ','
                                             + (lat + dLat).toFixed(6);
                                    return 'https://www.openstreetmap.org/export/embed.html?bbox=' + bbox + '&layer=mapnik&marker=' + lat + ',' + lon;
                                }

                                function focusStore(index) {
                                    if (!storesCache[index]) return;
                                    var s = storesCache[index];
                                    updateMapImage(s.latitude, s.longitude, 14, index);
                                    if (window.AndroidBridge && window.AndroidBridge.onStoreMarkerClick) {
                                        window.AndroidBridge.onStoreMarkerClick(s.locationId);
                                    }
                                }

                                function openInExternalMap(index) {
                                    if (!storesCache[index]) return;
                                    var s = storesCache[index];
                                    if (window.AndroidBridge && window.AndroidBridge.openExternalMap) {
                                        window.AndroidBridge.openExternalMap(s.latitude, s.longitude, 'Chi nhanh ' + s.locationId);
                                    }
                                }

                                function setDiagnosticStatus(message) {
                                    document.getElementById('status').innerText = message;
                                }

                                function updateMapImage(lat, lon, zoom, focusIndex) {
                                    var frame = document.getElementById('mapframe');
                                    var sources = [
                                        buildGoogleEmbedUrl(lat, lon, zoom),
                                        buildOsmEmbedUrl(lat, lon, zoom)
                                    ];
                                    var index = 0;
                                    var timer = null;

                                    function tryLoadNext() {
                                        if (timer) {
                                            clearTimeout(timer);
                                            timer = null;
                                        }

                                        if (index >= sources.length) {
                                            document.getElementById('status').innerText = 'Khong tai duoc map online tren emulator hien tai.';
                                            if (window.AndroidBridge && window.AndroidBridge.reportMapLoadFailure) {
                                                window.AndroidBridge.reportMapLoadFailure();
                                            }
                                            return;
                                        }

                                        var current = sources[index++];
                                        document.getElementById('status').innerText = 'Dang tai anh ban do (nguon ' + index + '/' + sources.length + ')...';

                                        frame.onload = function() {
                                            if (timer) clearTimeout(timer);
                                            document.getElementById('status').innerText = 'Ban do da hien thi.';
                                        };

                                        timer = setTimeout(function() {
                                            tryLoadNext();
                                        }, 6000);

                                        frame.src = current;
                                    }

                                    tryLoadNext();
                                }

                window.addStores = function(stores) {
                                    storesCache = stores || [];
                                    if (storesCache.length === 0) {
                                        document.getElementById('status').innerText = 'Khong co cua hang de hien thi.';
                                        return;
                  }

                                    var first = storesCache[0];
                                    updateMapImage(first.latitude, first.longitude, 12, 0);
                                    document.getElementById('status').innerText = 'Dang tai anh ban do...';

                                    var list = document.getElementById('store-list');
                                    list.innerHTML = '';
                                    storesCache.forEach(function(s, idx) {
                                        var li = document.createElement('li');
                                        li.innerHTML = '<div><b>Chi nhanh ' + s.locationId + '</b></div>' +
                                                                     '<div>' + s.address + '</div>' +
                                                                     '<div style="margin-top:6px">'
                                                                     + '<button onclick="focusStore(' + idx + ')">Xem va mo chi tiet</button>'
                                                                     + '<button style="margin-left:6px;background:#2e7d32" onclick="openInExternalMap(' + idx + ')">Mo Google Maps</button>'
                                                                     + '</div>';
                                        list.appendChild(li);
                                    });
                };

                                document.getElementById('status').innerText = 'Dang cho du lieu cua hang...';
              </script>
            </body>
            </html>
        """.trimIndent()
    }

    private fun loadStores() {
        Log.d(TAG, "Calling viewModel.getStores()")
        viewModel.getStores()
    }

    private fun observeViewModelStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.storesState.collect { state ->
                Log.d(TAG, "State: $state")
                when (state) {
                    is UiState.Loading -> {
                        Log.d(TAG, "Loading stores...")
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Success -> {
                        Log.d(TAG, "Success: ${state.data.size} stores loaded")
                        binding.progressBar.visibility = View.GONE
                        stores = state.data
                        if (stores.isEmpty()) {
                            Log.w(TAG, "No stores found")
                            Toast.makeText(requireContext(), "Không tìm thấy cửa hàng nào", Toast.LENGTH_SHORT).show()
                        } else {
                            displayStoresOnWebMap(stores)
                        }
                    }
                    is UiState.Error -> {
                        Log.e(TAG, "Error: ${state.message}")
                        binding.progressBar.visibility = View.GONE
                        val message = "Không tải được cửa hàng: ${state.message}"
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Log.d(TAG, "Idle state")
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun displayStoresOnWebMap(stores: List<StoreDto>) {
        if (!isWebMapReady) {
            pendingStores = stores
            return
        }

        renderStoresOnWebMap(stores)
    }

    private fun renderStoresOnWebMap(stores: List<StoreDto>) {
        val payload = JSONArray()
        stores.forEach { store ->
            payload.put(
                JSONObject().apply {
                    put("locationId", store.locationId)
                    put("address", store.address)
                    put("latitude", store.latitude)
                    put("longitude", store.longitude)
                }
            )
        }

        val js = "window.addStores(${payload});"
        binding.mapWebView.evaluateJavascript(js, null)
    }

    private fun showStoreDetails(store: StoreDto) {
        val dialog = StoreDetailBottomSheetFragment.newInstance(store)
        dialog.show(parentFragmentManager, "store_detail")
    }

    private suspend fun diagnoseMapConnectivity(): String = withContext(Dispatchers.IO) {
        val urls = listOf(
            "https://maps.google.com",
            "https://www.openstreetmap.org",
            "https://staticmap.openstreetmap.de/staticmap.php?center=10.7769,106.6966&zoom=12&size=200x200"
        )

        val results = urls.map { endpoint ->
            val result = try {
                val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 5000
                    readTimeout = 5000
                    setRequestProperty("User-Agent", "SaleApp-Map-Diagnostic")
                }
                val code = connection.responseCode
                connection.disconnect()
                "OK($code)"
            } catch (e: UnknownHostException) {
                "DNS_FAIL"
            } catch (e: SSLHandshakeException) {
                "SSL_FAIL"
            } catch (e: SocketTimeoutException) {
                "TIMEOUT"
            } catch (e: ConnectException) {
                "CONNECT_FAIL"
            } catch (e: Exception) {
                "ERR:${e.javaClass.simpleName}"
            }
            "$endpoint => $result"
        }

        "Map network check: " + results.joinToString(" | ")
    }

    override fun onDestroyView() {
        isWebMapReady = false
        pendingStores = null
        binding.mapWebView.removeJavascriptInterface("AndroidBridge")
        binding.mapWebView.stopLoading()
        binding.mapWebView.loadUrl("about:blank")
        binding.mapWebView.clearHistory()
        binding.mapWebView.destroy()
        _binding = null
        super.onDestroyView()
    }
}

