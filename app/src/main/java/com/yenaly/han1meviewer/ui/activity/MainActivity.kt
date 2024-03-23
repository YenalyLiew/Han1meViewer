package com.yenaly.han1meviewer.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.snackbar.Snackbar
import com.itxca.spannablex.spannable
import com.yenaly.han1meviewer.Preferences.isAlreadyLogin
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.VIDEO_CODE
import com.yenaly.han1meviewer.databinding.ActivityMainBinding
import com.yenaly.han1meviewer.hanimeSpannable
import com.yenaly.han1meviewer.logic.model.github.Release
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.logout
import com.yenaly.han1meviewer.ui.viewmodel.MainViewModel
import com.yenaly.han1meviewer.util.showAlertDialog
import com.yenaly.han1meviewer.util.showUpdateDialog
import com.yenaly.han1meviewer.videoUrlRegex
import com.yenaly.yenaly_libs.base.YenalyActivity
import com.yenaly.yenaly_libs.utils.*
import kotlinx.coroutines.launch

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/08 008 17:35
 */
class MainActivity : YenalyActivity<ActivityMainBinding, MainViewModel>() {

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

    override fun setUiStyle() {

    }

    /**
     * 初始化数据
     */
    override fun initData(savedInstanceState: Bundle?) {

        initHeaderView()
        initMenu()

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fcv_main) as NavHostFragment
        navController = navHostFragment.navController
        binding.nvMain.setupWithNavController(navController)

        binding.nvMain.getHeaderView(0)?.setPaddingRelative(0, window.currentStatusBarHeight, 0, 0)

        binding.nvMain.menu.findItem(R.id.nv_settings).setOnMenuItemClickListener {
            startActivity<SettingsActivity>()
            binding.dlMain.closeDrawers()
            return@setOnMenuItemClickListener false
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
            whenStarted {
                viewModel.versionFlow.collect {
                    if (it is WebsiteState.Success) {
                        it.info?.let { release ->
                            showUpdateDialog(release)
                        }
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    // todo: 有時間轉移到 strings.xml
    @Deprecated("To SnackBar")
    private fun showFindRelatedLinkDialog(videoCode: String) {
        showAlertDialog {
            setTitle("新發現！")
            setMessage("檢測到剪貼簿裏存在Hanime1相關連結")
            setPositiveButton("進入！") { _, _ ->
                startActivity<VideoActivity>(VIDEO_CODE to videoCode)
            }
            setNegativeButton("算了吧", null)
        }
    }

    // todo: 有時間轉移到 strings.xml
    private fun showFindRelatedLinkSnackBar(videoCode: String) {
        showSnackBar("檢測到剪貼簿裏存在Hanime1相關連結", Snackbar.LENGTH_LONG) {
            setAction("進入！") {
                startActivity<VideoActivity>(VIDEO_CODE to videoCode)
            }
        }
    }

    // todo: 有時間轉移到 strings.xml
    @Deprecated("No reasons.")
    private fun showUpdateDialog(releaseInfo: Release) {
        val msg = spannable {
            "檢測到新版本：".text()
            newline()
            releaseInfo.tagName.span {
                style(Typeface.BOLD)
            }
            newline()
            "更新内容：".text()
            newline()
            releaseInfo.body.span {
                style(Typeface.BOLD)
            }
        }
        showAlertDialog {
            setTitle("檢測到新版本！")
            setMessage(msg)
            setPositiveButton("去下載！") { _, _ ->
                browse(releaseInfo.assets.first().browserDownloadURL)
            }
            setNeutralButton("忽略", null)
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
                        setTitle("你確定要登出嗎？")
                        setPositiveButton("是的") { _, _ ->
                            logout()
                            initHeaderView()
                            initMenu()
                        }
                        setNegativeButton("算了吧", null)
                    }
                }
                lifecycleScope.launch {
                    whenStarted {
                        viewModel.homePageFlow.collect { state ->
                            if (state is WebsiteState.Success) {
                                headerAvatar.load(state.info.avatarUrl) {
                                    crossfade(true)
                                    transformations(CircleCropTransformation())
                                }
                                headerUsername.text = state.info.username
                            } else {
                                headerAvatar.load(R.mipmap.ic_launcher) {
                                    crossfade(true)
                                    transformations(CircleCropTransformation())
                                }
                                headerUsername.text = "Loading..."
                            }
                        }
                    }
                }
            } else {
                headerAvatar.load(R.mipmap.ic_launcher) {
                    crossfade(true)
                    transformations(CircleCropTransformation())
                }
                headerUsername.text = "未登錄"
                headerAvatar.setOnClickListener {
                    gotoLoginActivity()
                }
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
                    gotoLoginActivity()
                    return@setOnMenuItemClickListener false
                }
            }
        }
    }

    private fun gotoLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        loginDataLauncher.launch(intent)
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