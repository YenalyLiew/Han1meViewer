@file:JvmName("ActivityUtil")

package com.yenaly.yenaly_libs.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction

inline fun AppCompatActivity.inFragmentManagerTransaction(
    action: FragmentTransaction.() -> Unit
) = supportFragmentManager.beginTransaction().apply(action).commit()