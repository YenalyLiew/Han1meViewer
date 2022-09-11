# Han1meViewer

这是一个 Hanime1.me Android 平台的浏览器。

A Hanime1.me Application for Android.

## 功能

支持高级查询、观看（可切换分辨率与语言）、下载、历史记录以及收藏等功能。

暂不支持评论等功能，且目前可能会存在一系列bug。

Supports advanced query, watch (be able to switch resolution and language), download, history and add to favorite list.

Comments and some other functions are not supported TEMPORARILY, and there may be a series of bugs at present.

## 存在问题

> 这网站前端写的是真不行，我已经尽力挖关键信息了，但还是很难实现部分功能。
>
> 而且只要该网站大改，该软件可能要推倒重做。
>
> 最后还是建议跟 E-hentai 学一学。

1. 在全屏模式下观看视频切换分辨率后，退出全屏会导致视频加载重置。

   When you watch video and switch resolution in fullscreen mode, video loading will reset after you quit fullscreen mode.

2. 部分界面优化不足，存在卡顿。

   Some user interfaces having insufficient optimization might be laggy.

3. 下载界面暂时只有“已下载”列表，“正在下载”列表暂未实现。

   There is only a Downloaded list in Download UI, I have not implemented the Downloading list.

4. 暂时无法使用待看列表以及自定义列表。

   No watch later list and your own list temporarily.

## 更新内容

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