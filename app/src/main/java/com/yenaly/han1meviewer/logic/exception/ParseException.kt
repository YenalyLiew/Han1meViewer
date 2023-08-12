package com.yenaly.han1meviewer.logic.exception

/**
 * 解析錯誤
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/05 005 16:20
 */
class ParseException : RuntimeException {

    constructor(
        funcName: String,
        varName: String
    ) : super("[Parse::$funcName => $varName] parse error!")

    constructor(reason: String) : super(reason)
}