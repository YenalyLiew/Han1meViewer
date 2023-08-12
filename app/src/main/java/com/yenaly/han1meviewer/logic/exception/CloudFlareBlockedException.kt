package com.yenaly.han1meviewer.logic.exception

/**
 * 检测到爬虫被封鎖
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/07 007 12:45
 */
open class CloudFlareBlockedException(reason: String) : RuntimeException(reason)