package com.yenaly.han1meviewer.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.itxca.spannablex.spannable
import com.yenaly.han1meviewer.*
import com.yenaly.han1meviewer.util.checkNeedUpdate
import com.yenaly.han1meviewer.databinding.ActivityMainBinding
import com.yenaly.han1meviewer.logic.model.VersionModel
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.logout
import com.yenaly.han1meviewer.ui.viewmodel.MainViewModel
import com.yenaly.yenaly_libs.base.YenalyActivity
import com.yenaly.yenaly_libs.utils.*
import kotlinx.coroutines.launch

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/08 008 17:35
 */
class MainActivity : YenalyActivity<ActivityMainBinding, MainViewModel>() {

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

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
        SystemStatusUtil.fullScreen(window, true)
    }

    /**
     * 初始化数据
     */
    override fun initData(savedInstanceState: Bundle?) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.title = spannable {
                "H".span {
                    style(Typeface.BOLD)
                    color(Color.RED)
                }
                "an1me".span {
                    style(Typeface.BOLD)
                }
                //"anime1".span {
                //    style(Typeface.BOLD)
                //}
                //".".span {
                //    style(Typeface.BOLD)
                //    color(Color.RED)
                //}
                //"me".text()
                "Viewer".text()
            }
        }

        initHeaderView()
        initMenu()

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fcv_main) as NavHostFragment
        navController = navHostFragment.navController
        binding.nvMain.setupWithNavController(navController)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.nv_home_page), binding.dlMain)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

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
                if (it.contains("hanime1.me/watch?v=")) {
                    val videoCode = it.substringAfter("watch?v=")
                    if (videoCode.isDigitsOnly()) {
                        showFindRelatedLinkSnackBar(videoCode)
                    } else {
                        val videoCodeReal = buildString {
                            videoCode.forEach { char ->
                                if (char.isDigit()) append(char) else return@buildString
                            }
                        }
                        if (videoCodeReal.isNotEmpty()) {
                            showFindRelatedLinkSnackBar(videoCodeReal)
                        }
                    }
                }
            }
        }
    }

    override fun liveDataObserve() {
        lifecycleScope.launch {
            whenStarted {
                viewModel.versionFlow.collect {
                    if (it is WebsiteState.Success) {
                        if (checkNeedUpdate(it.info.tagName)) {
                            showUpdateDialog(it.info)
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main_toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.tb_search -> {
                startActivity<SearchActivity>()
                return true
            }
            R.id.tb_previews -> {
                startActivity<PreviewActivity>()
                return true
            }
        }
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    // todo: 有時間轉移到 strings.xml
    @Deprecated("To SnackBar")
    private fun showFindRelatedLinkDialog(videoCode: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("新發現！")
            .setMessage("檢測到剪貼簿裏存在Hanime1相關連結")
            .setPositiveButton("進入！") { _, _ ->
                startActivity<VideoActivity>(VIDEO_CODE to videoCode)
            }
            .setNegativeButton("算了吧", null)
            .show()
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
    private fun showUpdateDialog(versionInfo: VersionModel) {
        val msg = spannable {
            "檢測到新版本：".text()
            newline()
            versionInfo.tagName.span {
                style(Typeface.BOLD)
            }
            newline()
            "更新内容：".text()
            newline()
            versionInfo.body.span {
                style(Typeface.BOLD)
            }
        }
        MaterialAlertDialogBuilder(this)
            .setTitle("檢測到新版本！")
            .setMessage(msg)
            .setPositiveButton("去下載！") { _, _ ->
                browse(versionInfo.assets.first().browserDownloadURL)
            }
            .setNeutralButton("忽略", null)
            .show()
    }

    @SuppressLint("SetTextI18n")
    private fun initHeaderView() {
        binding.nvMain.getHeaderView(0)?.let { view ->
            val headerAvatar = view.findViewById<ImageView>(R.id.header_avatar)
            val headerUsername = view.findViewById<TextView>(R.id.header_username)
            if (isAlreadyLogin) {
                headerAvatar.setOnClickListener {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("是否要登出")
                        .setPositiveButton("是的") { _, _ ->
                            logout()
                            initHeaderView()
                            initMenu()
                        }
                        .setNegativeButton("算了吧", null)
                        .show()
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
                    val intent = Intent(this, LoginActivity::class.java)
                    loginDataLauncher.launch(intent)
                }
            }
        }
    }

    private fun initMenu() {
        if (isAlreadyLogin) {
            binding.nvMain.menu.findItem(R.id.nv_fav_video).setOnMenuItemClickListener(null)
            binding.nvMain.menu.findItem(R.id.nv_watch_later).setOnMenuItemClickListener(null)
        } else {
            binding.nvMain.menu.findItem(R.id.nv_fav_video).setOnMenuItemClickListener {
                showShortToast(R.string.login_first)
                val intent = Intent(this, LoginActivity::class.java)
                loginDataLauncher.launch(intent)
                return@setOnMenuItemClickListener false
            }
            binding.nvMain.menu.findItem(R.id.nv_watch_later).setOnMenuItemClickListener {
                showShortToast(R.string.login_first)
                val intent = Intent(this, LoginActivity::class.java)
                loginDataLauncher.launch(intent)
                return@setOnMenuItemClickListener false
            }
        }
    }

    fun setToolbarSubtitle(subtitle: CharSequence?) {
        supportActionBar?.subtitle = subtitle
    }
}