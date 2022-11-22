package com.dtysky.Awaken

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {
    var mainWebView: WebView? = null
    private var jsb: AwakenJSB? = null
    private var selectFilesCallback: ((files: Array<String>) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hideStatusAndTitleBar()

        WebView.setWebContentsDebuggingEnabled(true)
        mainWebView = findViewById(R.id.main)
        initWebViewSetting(mainWebView)
        var base = getExternalFilesDir(null)
        jsb = AwakenJSB(this)
        mainWebView?.addJavascriptInterface(jsb!!,"Awaken")
        mainWebView?.loadUrl("http://192.168.2.208:8888")
    }

    private fun initWebViewSetting(webView: WebView?) {
        webView?.run {
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.domStorageEnabled = true
//            settings.allowContentAccess = true
//            settings.allowFileAccess = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            settings.javaScriptEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.setSupportMultipleWindows(true)

            webViewClient = object: WebViewClient() {
                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                    request?.run {
                        if (url.host.equals("awaken.api")) {
                            var method: String = url.path.toString().substring(1)
                            var params: MutableMap<String, String> = mutableMapOf()
                            url.queryParameterNames.forEach {
                                params.put(it, url.getQueryParameter(it).toString())
                            }

                            return jsb!!.callMethod(method, params)
                        }
                    }
                    return super.shouldInterceptRequest(view, request)
                }
            }
        }
    }

    fun selectFiles(
        mimeTypes: String,
        callback: (files: Array<String>) -> Unit
    ) {
        selectFilesCallback = callback
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeTypes
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }

        startActivityForResult(intent, 4)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == 4) {
            if (resultCode != RESULT_OK) {
                selectFilesCallback?.invoke(arrayOf())
            } else {
                resultData?.data?.also {uri ->
                    // Perform operations on the document using its URI.
                    Log.d("Awaken", uri.toString())
                    selectFilesCallback?.invoke(arrayOf(uri.toString()))
                }
            }
            selectFilesCallback = null
            return
        }

        super.onActivityResult(requestCode, resultCode, resultData)
    }

    override fun onResume() {
        super.onResume()
        mainWebView?.run {
            onResume()
            resumeTimers()
        }
    }

    override fun onPause() {
        super.onPause()
        mainWebView?.run {
            onPause()
            pauseTimers()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideStatusAndTitleBar()
    }

    private fun hideStatusAndTitleBar() {
        supportActionBar?.hide()

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.bar))
//        val decorView = window.decorView
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            val windowInsetsController: WindowInsetsController = decorView.getWindowInsetsController()
//                ?: return
//            windowInsetsController.systemBarsBehavior =
//                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//            windowInsetsController.hide(WindowInsets.Type.systemBars())
//        } else {
//            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
//        }
    }
}