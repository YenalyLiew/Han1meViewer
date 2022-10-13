package com.yenaly.han1meviewer.ui.activity

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import com.itxca.spannablex.spannable
import com.yenaly.han1meviewer.HANIME_BASE_URL
import com.yenaly.han1meviewer.HANIME_LOGIN_URL
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.ActivityLoginBinding
import com.yenaly.han1meviewer.login
import com.yenaly.han1meviewer.util.CookieString
import com.yenaly.yenaly_libs.base.frame.FrameActivity
import com.yenaly.yenaly_libs.utils.SystemStatusUtil

class LoginActivity : FrameActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun setUiStyle() {
        SystemStatusUtil.fullScreen(window, true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.title = spannable {
                "H".span {
                    style(Typeface.BOLD)
                    color(Color.RED)
                }
                "anime1".span {
                    style(Typeface.BOLD)
                }
                ".".span {
                    style(Typeface.BOLD)
                    color(Color.RED)
                }
                "me ".text()
                getString(R.string.login).text()
            }
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeActionContentDescription(R.string.back)
        }
        binding.srlLogin.setOnRefreshListener {
            binding.wvLogin.loadUrl(HANIME_LOGIN_URL)
        }
        binding.srlLogin.autoRefresh()
        initWebView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && binding.wvLogin.canGoBack()) {
            binding.wvLogin.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun initWebView() {
        binding.wvLogin.apply {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    binding.srlLogin.finishRefresh()
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest
                ): Boolean {
                    if (request.isRedirect && request.url.toString().contains(HANIME_BASE_URL)) {
                        val url = request.url
                        val cookieManager = CookieManager.getInstance().getCookie(url.host)
                        Log.d("login_cookie", cookieManager.toString())
                        login(CookieString(cookieManager))
                        setResult(RESULT_OK)
                        finish()
                        return true
                    }
                    return super.shouldOverrideUrlLoading(view, request)
                }
            }
        }
    }
}