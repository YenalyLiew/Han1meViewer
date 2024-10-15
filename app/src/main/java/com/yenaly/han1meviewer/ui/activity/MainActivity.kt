package com.yenaly.han1meviewer.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.snackbar.Snackbar
import com.yenaly.han1meviewer.Preferences
import com.yenaly.han1meviewer.Preferences.isAlreadyLogin
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.VIDEO_CODE
import com.yenaly.han1meviewer.databinding.ActivityMainBinding
import com.yenaly.han1meviewer.hanimeSpannable
import com.yenaly.han1meviewer.logic.exception.CloudFlareBlockedException
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.logout
import com.yenaly.han1meviewer.ui.viewmodel.AppViewModel
import com.yenaly.han1meviewer.ui.viewmodel.MainViewModel
import com.yenaly.han1meviewer.util.logScreenViewEvent
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.han1meviewer.util.showUpdateDialog
import com.yenaly.han1meviewer.videoUrlRegex
import com.yenaly.yenaly_libs.base.YenalyActivity
import com.yenaly.yenaly_libs.utils.dp
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.showSnackBar
import com.yenaly.yenaly_libs.utils.startActivity
import com.yenaly.yenaly_libs.utils.textFromClipboard
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/08 008 17:35
 */
class MainActivity : YenalyActivity<ActivityMainBinding>(), DrawerListener {

    val viewModel by viewModels<MainViewModel>()

    lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController

    val currentFragment get() = navHostFragment.childFragmentManager.primaryNavigationFragment

    // 登錄完了後讓activity刷新主頁
    private val loginDataLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                viewModel.getHomePage()
                initHeaderView()
                initMenu()
            }
        }

    override fun getViewBinding(layoutInflater: LayoutInflater): ActivityMainBinding =
        ActivityMainBinding.inflate(layoutInflater)

    override fun setUiStyle() {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
    }

    override val onFragmentResumedListener: (Fragment) -> Unit = { fragment ->
        logScreenViewEvent(fragment)
    }

    /**
     * 初始化数据
     */
    override fun initData(savedInstanceState: Bundle?) {

        initHeaderView()
        initNavActivity()
        initMenu()

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fcv_main) as NavHostFragment
        navController = navHostFragment.navController
        binding.nvMain.setupWithNavController(navController)
        binding.dlMain.addDrawerListener(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.nvMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onStart() {
        super.onStart()
        binding.root.post {
            textFromClipboard?.let {
                videoUrlRegex.find(it)?.groupValues?.get(1)?.let { videoCode ->
                    showFindRelatedLinkSnackBar(videoCode)
                }
            }
        }
    }

    override fun bindDataObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                AppViewModel.versionFlow.collect { state ->
                    if (state is WebsiteState.Success && Preferences.isUpdateDialogVisible) {
                        state.info?.let { release ->
                            Preferences.lastUpdatePopupTime = Clock.System.now().epochSeconds
                            showUpdateDialog(release)
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.homePageFlow.collect { state ->
                    if (state is WebsiteState.Error) {
                        if (state.throwable is CloudFlareBlockedException) {
                            // TODO: 被屏蔽时的处理
                        }
                    }
                }
            }
        }

        binding.nvMain.getHeaderView(0)?.let { header ->
            val headerAvatar = header.findViewById<ImageView>(R.id.header_avatar)
            val headerUsername = header.findViewById<TextView>(R.id.header_username)
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    viewModel.homePageFlow.collect { state ->
                        if (state is WebsiteState.Success) {
                            if (isAlreadyLogin) {
                                if (state.info.username == null) {
                                    headerAvatar.load(R.mipmap.ic_launcher) {
                                        crossfade(true)
                                        transformations(CircleCropTransformation())
                                    }
                                    headerUsername.setText(R.string.refresh_page_or_login_expired)
                                } else {
                                    headerAvatar.load(state.info.avatarUrl) {
                                        crossfade(true)
                                        transformations(CircleCropTransformation())
                                    }
                                    headerUsername.text = state.info.username
                                }
                            } else {
                                initHeaderView()
                            }
                        } else {
                            headerAvatar.load(R.mipmap.ic_launcher) {
                                crossfade(true)
                                transformations(CircleCropTransformation())
                            }
                            headerUsername.setText(R.string.loading)
                        }
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (slideOffset > 0f) {
                binding.fcvMain.setRenderEffect(
                    RenderEffect.createBlurEffect(
                        6.dp * slideOffset,
                        6.dp * slideOffset,
                        Shader.TileMode.CLAMP
                    )
                )
            }
        }
    }

    override fun onDrawerClosed(drawerView: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding.fcvMain.setRenderEffect(null)
        }
    }

    override fun onDrawerOpened(drawerView: View) {

    }

    override fun onDrawerStateChanged(newState: Int) {

    }

    private fun showFindRelatedLinkSnackBar(videoCode: String) {
        showSnackBar(R.string.detect_ha1_related_link_in_clipboard, Snackbar.LENGTH_LONG) {
            setAction(R.string.enter) {
                startActivity<VideoActivity>(VIDEO_CODE to videoCode)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initHeaderView() {
        binding.nvMain.getHeaderView(0)?.let { view ->
            val headerAvatar = view.findViewById<ImageView>(R.id.header_avatar)
            val headerUsername = view.findViewById<TextView>(R.id.header_username)
            if (isAlreadyLogin) {
                headerAvatar.setOnClickListener {
                    showAlertDialog {
                        setTitle(R.string.sure_to_logout)
                        setPositiveButton(R.string.sure) { _, _ ->
                            logoutWithRefresh()
                        }
                        setNegativeButton(R.string.no, null)
                    }
                }
            } else {
                headerAvatar.load(R.mipmap.ic_launcher) {
                    crossfade(true)
                    transformations(CircleCropTransformation())
                }
                headerUsername.setText(R.string.not_logged_in)
                headerAvatar.setOnClickListener {
                    gotoLoginActivity()
                }
            }
        }
    }

    // #issue-225: 侧滑选单双重点击异常，不能从 xml 里直接定义 activity 块，需要在代码里初始化
    private fun initNavActivity() {
        binding.nvMain.menu.apply {
            findItem(R.id.nv_settings).setOnMenuItemClickListener {
                startActivity<SettingsActivity>()
                return@setOnMenuItemClickListener false
            }
            findItem(R.id.nv_h_keyframe_settings).setOnMenuItemClickListener {
                startActivity<SettingsActivity>(SettingsActivity.H_KEYFRAME_SETTINGS to true)
                return@setOnMenuItemClickListener false
            }
        }
    }

    private val loginNeededFragmentList =
        intArrayOf(R.id.nv_fav_video, R.id.nv_watch_later, R.id.nv_playlist)

    private fun initMenu() {
        if (isAlreadyLogin) {
            loginNeededFragmentList.forEach {
                binding.nvMain.menu.findItem(it).setOnMenuItemClickListener(null)
            }
        } else {
            loginNeededFragmentList.forEach {
                binding.nvMain.menu.findItem(it).setOnMenuItemClickListener {
                    showShortToast(R.string.login_first)
                    return@setOnMenuItemClickListener false
                }
            }
        }
    }

    private fun gotoLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        loginDataLauncher.launch(intent)
    }

    private fun logoutWithRefresh() {
        logout()
        initHeaderView()
        initMenu()
    }

    /**
     * 设置toolbar与navController关联
     *
     * 必须最后调用！先设置好toolbar！
     */
    fun Toolbar.setupWithMainNavController() {
        supportActionBar!!.title = hanimeSpannable
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.nv_home_page), binding.dlMain)
        this.setupWithNavController(navController, appBarConfiguration)
    }
}