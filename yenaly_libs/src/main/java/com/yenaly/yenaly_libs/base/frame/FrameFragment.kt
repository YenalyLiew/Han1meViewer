package com.yenaly.yenaly_libs.base.frame

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yenaly.yenaly_libs.R
import com.yenaly.yenaly_libs.utils.dp

/**
 * @ProjectName : YenalyModule
 * @Author : Yenaly Liew
 * @Time : 2022/04/16 016 20:25
 * @Description : Description...
 */
abstract class FrameFragment : Fragment() {

    val window get() = requireActivity().window

    private lateinit var loadingDialog: AlertDialog

    @JvmOverloads
    fun showLoadingDialog(
        loadingText: String = getString(R.string.yenaly_loading),
        cancelable: Boolean = false,
        dialogWidth: Int = 260.dp,
        dialogHeight: Int = ViewGroup.LayoutParams.WRAP_CONTENT
    ) {
        val loadingDialogView =
            LayoutInflater.from(activity).inflate(R.layout.yenaly_dialog_loading, null)
        loadingDialogView.findViewById<TextView>(R.id.loading_text).text = loadingText
        loadingDialog = MaterialAlertDialogBuilder(requireContext())
            .setCancelable(cancelable)
            .setView(loadingDialogView)
            .create()
        loadingDialog.show()
        loadingDialog.window?.setLayout(dialogWidth, dialogHeight)
    }

    fun hideLoadingDialog() {
        if (this::loadingDialog.isInitialized) {
            loadingDialog.hide()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::loadingDialog.isInitialized) {
            loadingDialog.dismiss()
        }
    }
}