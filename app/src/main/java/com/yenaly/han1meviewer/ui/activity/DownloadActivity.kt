package com.yenaly.han1meviewer.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import com.yenaly.han1meviewer.databinding.ActivityDownloadBinding
import com.yenaly.yenaly_libs.base.YenalyActivity

class DownloadActivity : YenalyActivity<ActivityDownloadBinding>() {
    override fun getViewBinding(layoutInflater: LayoutInflater): ActivityDownloadBinding {
        return ActivityDownloadBinding.inflate(layoutInflater)
    }

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun bindDataObservers() {

    }
}