# Han1meViewer

这是一个 Hanime1.me Android 平台的非官方浏览器。

An unofficial Hanime1.me Application for Android.

## 功能

支持高级查询、观看（可切换分辨率与语言）、下载、历史记录以及收藏等功能。

支持评论但暂不支持评论点赞点踩等功能，且目前可能会存在一系列bug。

**请允许通知以便能正确收到下载状态通知！**

Supports advanced query, watch (be able to switch resolution and language), download, history and add to favorite list.

Comment is supported right now but comment like or dislike and some other functions are not supported TEMPORARILY, and there may be a series of bugs at present.

**Please allow notification in order to properly receive download status notification!**

## 存在问题

> 这网站前端写的是真不行，我已经尽力挖关键信息了，但还是很难实现部分功能。
>
> 而且只要该网站大改，该软件可能要推倒重做。
>
> 最后还是建议跟 E-hentai 学一学。

1. 部分界面优化不足，存在卡顿。

   Some user interfaces having insufficient optimization might be laggy.

2. 下载界面暂时只有“已下载”列表，“正在下载”列表暂未实现。

   There is only a Downloaded list in Download UI, I have not implemented the Downloading list.

3. 暂时无法使用待看列表以及自定义列表。

   No watch later list and your own list temporarily.

## 更新内容

### v0.5.0

调整部分 UI。适配 Android 13 通知权限。

修复删除下载影片后列表不能及时更新的问题。修复搜索栏部分逻辑。

修复从全屏切换分辨率后再返回正常界面，影片重置的问题。

修复新番导览日期显示错误的问题。

修改分辨率排列顺序，影片从最高画质开始播放，画质从高到低排列。

增加影片滑动阻尼系数，避免滑动过多导致不能微调。

### v0.4.1

修复未登入前无法查看评论的问题。

### v0.4.0

新增评论功能，包括影片评论，评论回复，子评论回复，但暂不支持点赞点踩。

新增清理缓存（快取）功能。

优化搜索体验，修复了一些小问题。

### v0.3.0

新增更新功能，不过依赖于 Github 的 API，可能有次数限制。

修复搜索时选择 Tag 后保存再打开变成全选的 bug。

优化用户体验。

### v0.2

修复旋转屏幕列表单列显示错乱的问题。

实现了下载功能，支持新番导览小图点击后打开大图的功能，支持保存。未测试过能否断点续传，貌似没实现，如果下载一半关闭程序可能会有 bug 产生，所以建议下载完了再关闭。

最低可用安卓版本从 Android 6.0 修改为 Android 7.0。

### v0.1

第一个版本，实现了基本使用，如观看视频，搜索，添加到历史记录等，暂不支持下载功能。