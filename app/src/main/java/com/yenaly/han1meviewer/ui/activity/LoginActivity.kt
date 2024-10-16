package com.yenaly.han1meviewer.ui.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.itxca.spannablex.spannable
import com.yenaly.han1meviewer.HANIME_ALTER_BASE_URL
import com.yenaly.han1meviewer.HANIME_LOGIN_URL
import com.yenaly.han1meviewer.HANIME_MAIN_BASE_URL
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.USER_AGENT
import com.yenaly.han1meviewer.databinding.ActivityLoginBinding
import com.yenaly.han1meviewer.logic.NetworkRepo
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.login
import com.yenaly.han1meviewer.util.createAlertDialog
import com.yenaly.han1meviewer.util.showWithBlurEffect
import com.yenaly.yenaly_libs.base.frame.FrameActivity
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.unsafeLazy
import kotlinx.coroutines.launch

class LoginActivity : FrameActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val dialog = unsafeLazy { LoginDialog(R.layout.dialog_login) }

    override fun setUiStyle() {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
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
        initWebView()
        binding.srlLogin.setOnRefreshListener {
            binding.wvLogin.loadUrl(HANIME_LOGIN_URL)
        }
        binding.srlLogin.autoRefresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (dialog.isInitialized()) {
            dialog.value.dismiss()
        }
        binding.wvLogin.removeAllViews()
        binding.wvLogin.destroy()
        binding.unbind()
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

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        binding.wvLogin.apply {
            // #issue-17: 谷歌登录需要开启JavaScript，但是谷歌拒絕這種登錄方式，遂放棄
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.userAgentString = USER_AGENT

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    binding.srlLogin.finishRefresh()
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest,
                ): Boolean {
                    val isSameUrl = request.url.toString() == HANIME_MAIN_BASE_URL ||
                            request.url.toString() == HANIME_ALTER_BASE_URL
                    if (request.isRedirect && isSameUrl) {
                        val url = request.url
                        val cookieManager = CookieManager.getInstance().getCookie(url.host)
                        Log.d("login_cookie", cookieManager.toString())
                        login(cookieManager)
                        setResult(RESULT_OK)
                        finish()
                        return true
                    }
                    return super.shouldOverrideUrlLoading(view, request)
                }

                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?,
                ) {
                    // #issue-146
                    // #issue-160: 修复字段销毁后调用引发的错误
                    if (!isDestroyed && !isFinishing) {
                        binding.srlLogin.finishRefresh()
                        dialog.value.show()
                    }
                }
            }
        }
    }

    inner class LoginDialog(@LayoutRes layoutRes: Int) {
        private val etUsername: TextInputEditText
        private val etPassword: TextInputEditText

        private val dialog: AlertDialog

        private val username get() = etUsername.text?.toString().orEmpty()
        private val password get() = etPassword.text?.toString().orEmpty()

        init {
            val view = View.inflate(this@LoginActivity, layoutRes, null)
            etUsername = view.findViewById(R.id.et_username)
            etPassword = view.findViewById(R.id.et_password)
            dialog = createAlertDialog {
                setView(view)
                setCancelable(false)
                setTitle(R.string.try_login_here)
                setPositiveButton(R.string.login, null)
                setNegativeButton(R.string.cancel, null)
            }
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    handleLogin()
                }
            }
        }

        private fun handleLogin() {
            lifecycleScope.launch {
                NetworkRepo.login(username, password).collect { state ->
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                        state !is WebsiteState.Loading
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled =
                        state !is WebsiteState.Loading
                    when (state) {
                        WebsiteState.Loading -> Unit

                        is WebsiteState.Error -> {
                            state.throwable.printStackTrace()
                            if (state.throwable is IllegalStateException) {
                                showShortToast(R.string.account_or_password_wrong)
                            } else {
                                showShortToast(R.string.login_failed)
                            }
                        }

                        is WebsiteState.Success -> {
                            login(state.info)
                            setResult(RESULT_OK)
                            dialog.dismiss()
                            showShortToast(R.string.login_success)
                            finish()
                        }
                    }
                }
            }
        }

        fun show() {
            dialog.showWithBlurEffect()
        }

        fun dismiss() {
            dialog.dismiss()
        }
    }
}