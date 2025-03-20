package com.yenaly.han1meviewer.ui.view.funcbar

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @since 2025/3/11 22:06
 */
class Hanidontroller {

    /**
     * 当前层级
     *
     */
    var level = 0
        private set

    private val navigationStack = ArrayDeque<Hanidokitem>()
    private val pastStack = ArrayDeque<Hanidokitem?>()

    /**
     * 初始化
     */
    fun initialize(hanidokitems: List<Hanidokitem>) {
        navigationStack.clear()
        pastStack.clear()
        navigationStack.addAll(hanidokitems)
    }

    /**
     * item 点击事件
     *
     * @param item item
     * @return true 如果存在子项，且 UI 需要更新。true if there are sub-items and the UI needs to be updated.
     */
    fun onItemClicked(item: Hanidokitem): Boolean {
        if (item.subitems.isNotEmpty()) {
            // Add a null to the past stack to indicate that we are going to a new item
            pastStack.addLast(null)
            while (navigationStack.isNotEmpty()) {
                pastStack.addLast(navigationStack.removeLast())
            }
            level++
            navigationStack.addLast(item.copy(_isBack = true))
            navigationStack.addAll(item.subitems)
            return true
        }
        return false
    }

    /**
     * 返回事件
     *
     * @return true 如果存在上一个层级，且 UI 需要更新。true if there is a previous level and the UI needs to be updated.
     */
    fun onBackPressed(): Boolean {
        if (level > 0) {
            navigationStack.clear()

            while (pastStack.isNotEmpty()) {
                // Find the last null marker in the past stack
                val item = pastStack.removeLast() ?: break
                navigationStack.addLast(item)
            }
            level--

            return true
        }
        return false
    }

    /**
     * 当前层级的 Hanidokitem 列表
     */
    val currentHanidokitems: List<Hanidokitem> get() = navigationStack.toList()
}