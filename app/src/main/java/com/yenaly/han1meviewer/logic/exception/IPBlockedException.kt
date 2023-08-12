package com.yenaly.han1meviewer.logic.exception

/**
 * IP被封鎖
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/07 007 12:40
 */
class IPBlockedException(reason: String) : CloudFlareBlockedException(reason)