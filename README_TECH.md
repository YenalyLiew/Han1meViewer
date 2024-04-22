# Han1meViewer 技术相关

> 抄是程序员进步的阶梯。

## 概括

本软件使用 MVVM 架构，Material 3 视觉风格，Jetpack 不用问肯定用，但未使用 Compose（有一说一不用 Compose
写 xml 真是写到吐）。网络请求使用 Retrofit，图片加载使用 Coil，视频播放使用 Jiaozi，Json 解析使用
Serialization，部分弹窗使用的 Xpopup。未使用 LiveData，全部改用功能更强大的 Flow。

## 受众人群

这篇文章主要给谁看的呢？一是那些刚学习 Android 的同学，想看看本项目是怎么写的，或者对其中某个功能很感兴趣，想学习一下并且快速集成于自己的
App 中；二是普通开发者感兴趣来捧个场，能学到东西更好，写的不对的来发 discussion 拷打我。

## 功能解析

### 断点续传下载

#### 你可以学到

1. WorkManager 使用，如何在 WorkManager 中对下载任务进行基础管理？
2. RecyclerView 使用，DiffUtil 使用，如何充分利用 `payload` 参数对某个特定的控件进行刷新？
3. Room 使用，如何通过数据库实现回调？

#### 关键文件

- [HanimeDownloadWorker.kt](app/src/main/java/com/yenaly/han1meviewer/worker/HanimeDownloadWorker.kt) - 关键作业类
- [HanimeDownloadEntity.kt](app/src/main/java/com/yenaly/han1meviewer/logic/entity/HanimeDownloadEntity.kt) - 下载 实体类
- [HanimeDownloadDao.kt](app/src/main/java/com/yenaly/han1meviewer/logic/dao/HanimeDownloadDao.kt) - 下载 Dao 类
- [DownloadDatabase.kt](app/src/main/java/com/yenaly/han1meviewer/logic/dao/DownloadDatabase.kt) - 下载 数据库类
- [HanimeDownloadingRvAdapter.kt](app/src/main/java/com/yenaly/han1meviewer/ui/adapter/HanimeDownloadingRvAdapter.kt) - 下载界面的 RecyclerView Adapter

#### 解释

你可能问我你就这几个文件就实现了？我接口呢，没接口你怎么回调的？

**先去看**我写的 [小白如何快速实现简单的可保存状态断点续传后台下载？一个 Jetpack 库搞定一切！](https://juejin.cn/post/7278929337067225149)，看完再看下面。

但是不要照搬，使用前要注意这么几点：

1. 你所下载的东西是否可以断点续传？对于视频类 App 来说，视频基本都是可以断点续传的，毕竟要播放嘛！所以我在实现下载的时候不必考虑那么多。
2. 是否要对每个下载任务进行很粒度的操作？不是说不行，但可能实现起来有点麻烦。
3. 一次性下载数目是否很多？如果使用上述文章的做法去下载极多文件可能会对手机性能造成一定压力，一会细说。

为什么说下载数目过多会造成一定压力？

聚焦于 [HanimeDownloadWorker.kt](app/src/main/java/com/yenaly/han1meviewer/worker/HanimeDownloadWorker.kt) 第 180 行左右：

```kotlin
const val RESPONSE_INTERVAL = 500L

if (System.currentTimeMillis() - delayTime > RESPONSE_INTERVAL) {
    val progress = entity.downloadedLength * 100 / entity.length
    setProgress(workDataOf(PROGRESS to progress.toInt()))
    setForeground(createForegroundInfo(progress.toInt()))
    DatabaseRepo.HanimeDownload.update(entity)
    delayTime = System.currentTimeMillis()
}
```

我在 App 里设置的是 500 ms 一更新，相当于 `2 次数据库更新操作/s/job`，加上通过 Flow/LiveData 回调，当数据库检测到数据更新，会立即返回全新的、拥有最新数据的列表，相当于又有 `回调 2 次/s/job`。如果一次性下载极多个文件，并且调低了 `RESPONSE_INTERVAL`，可能会对数据库造成一定负担。这个时候这种方法就不太好用了。

配置好了 RecyclerView，那刷新闪烁问题该如何解决？我在原文章中提供的方法并不好：

```kotlin
rv.itemAnimator?.changeDuration = 0
```

这句代码只是解决了表面问题，实际上背后还是接着“闪”。因为即使是通过了 DiffUtil 进行了差分刷新，但还仍是全局更新，这只是自我欺骗罢了。不信你可以试试 `holder.binding.pbProgress.setProgress(item.progress, true)` 能不能正常出现动态效果。那怎么实现，`isDownloading` 字段发生修改，就单独对暂停按钮修改；`downloadedLength` 字段发生修改，就单独对进度条修改？这时候就需要 `payload` 出场了。

与 `payload` 相关的文章真的挺多，StackOverflow 甚至 掘金 上不少介绍这个的文章，自己去搜一搜马上就能看懂，我就不赘述了。关键就是 `DiffUtil.ItemCallback` 中的 `getChangePayload` 方法和 `onBindViewHolder` 中的 `payloads` 参数。

**先去看** `payload` 使用相关文章，再看下面。

但我发现，很多人确实介绍了这种方法，但鲜少有人去介绍如何高效率实现一次性去处理多个字段。你可能想到了 `List<Int>` 或 `IntArray`，通过遍历对应去处理每一种情况。这样的话，时间复杂度和空间复杂度都是 `O(n)`，`n` 是你需要监听的数目；再聪明点也可以想到使用 `Set<Int>`，在 `onBindViewHolder` 中分别查询 set 中是否含有某个情况来对应处理，这时候时间复杂度降到了 `O(1)`。如果在刷新不频繁的情况下，这样做确实没什么不妥，但是高强度下，每次 new 一个数据结构确实是一个小负担，那应该怎么样做呢？

这时候可以选择简单的 Bitmap 数据结构。你可能刚听说，但它确实很常见，你在使用 `Intent#addFlags` 打开新 Activity 的时候，大概率会接触到这种数据结构。我们可以利用一个仅 4 个字节的 32-bit 整数值去实现查找 (`find`)、判空 (`isEmpty`)、添加 (`add`) 的功能（我们只需要这些功能，而且不同情况数量大概率不超过 32 个）。

聚焦于 [HanimeDownloadingRvAdapter.kt](app/src/main/java/com/yenaly/han1meviewer/ui/adapter/HanimeDownloadingRvAdapter.kt)

> 注意：我使用了 BRVAH 作为 RecyclerView 的代替，所以具体方法和 RecyclerView 不一定一致，但使用方法基本一致。

```kotlin
companion object {
    private const val DOWNLOADING = 1 // 0000 0001
    private const val PAUSE = 1 shl 1 // 0000 0010

    val COMPARATOR = object : DiffUtil.ItemCallback<HanimeDownloadEntity>() {
        override fun areContentsTheSame(
            oldItem: HanimeDownloadEntity,
            newItem: HanimeDownloadEntity,
        ): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(
            oldItem: HanimeDownloadEntity,
            newItem: HanimeDownloadEntity,
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun getChangePayload(
            oldItem: HanimeDownloadEntity,
            newItem: HanimeDownloadEntity,
        ): Any {
            // 假设当前只有 progress 和原来不一样
            var bitset = 0
            // bitset == 0000 0000
            if (oldItem.progress != newItem.progress || oldItem.downloadedLength != newItem.downloadedLength)
                bitset = bitset or DOWNLOADING
            	// bitset == 0000 0001
            if (oldItem.isDownloading != newItem.isDownloading)
                bitset = bitset or PAUSE
            	// 不经过这里
            return bitset
            // return 0000 0001
        }
    }
}
```

```kotlin
override fun onBindViewHolder(
    holder: DataBindingHolder<ItemHanimeDownloadingBinding>,
    position: Int,
    item: HanimeDownloadEntity?,
    payloads: List<Any>,
) {
    // 如果 payloads 列表为空，或者为 0000 0000，说明不需要修改
    if (payloads.isEmpty() || payloads.first() == 0)
        return super.onBindViewHolder(holder, position, item, payloads)
    item.notNull()
    val bitset = payloads.first() as Int
    // 0000 0001 & 0000 0001 = 0000 0001 != 0000 0000
    // 对进度相关控件进行修改
    if (bitset and DOWNLOADING != 0) {
        holder.binding.tvSize.text = spannable {
            item.downloadedLength.formatFileSize().text()
            " | ".span { color(Color.RED) }
            item.length.formatFileSize().span { style(Typeface.BOLD) }
        }
        holder.binding.tvProgress.text = "${item.progress}%"
        holder.binding.pbProgress.setProgress(item.progress, true)
    }
    // 0000 0001 & 0000 0010 = 0000 0000 == 0000 0000
    // 不经过下面
    if (bitset and PAUSE != 0) {
        holder.binding.btnStart.handleStartButton(item.isDownloading)
    }
}
```

就这样实现了效率比较高的差分刷新。

### CI 更新渠道

#### 你可以学到

#### 关键文件

#### 解释

当你的软件拓展性比较高，但受限于题材内容或者单纯懒，不方便自建服务器去读取这些拓展文件。但你又希望能让用户通过其他渠道实时的获取到更新（比如好心人上传了拓展文件，我合并到主分支之后，几分钟后用户就可以获得更新，而不用我自己做包），但又不是所有人需要这些拓展功能（要是人家不愿用你那功能，又一会一个 Release，用户也会烦；你自己一会发一个包你也会烦）。所以能不能给用户提供两种渠道？一个是稳定更新渠道，自己发版本；另一个是开发版，GitHub 自动构建，保证最新功能（最新拓展功能立即集成）但不保证稳定性。

答案是肯定的。其实我之前也不知道怎么做，但是 @NekoOuO 给我发了 [Foolbar/EhViewer](https://github.com/FooIbar/EhViewer/) 的做法，我想都没想就抄过来了。但没人详细教怎么做，我今天就来讲讲。

**先去看** GitHub CI 基础用法。

谷歌、掘金上全是教程。你先去查一查用法然后配置一下，刚开始的要求不多，你上传 commit 之后，GitHub CI 开始工作并成功 Build，就算入门了，先不用管 Build 之后干什么或者别的。如果你操作非常顺利，再看以下步骤。

待更...

### 共享关键H帧

#### 你可以学到

1. 如何充分利用 Kotlin 的集合操作函数，将一个个单独的 JSON 文件进行排序、分类甚至扁平化？

   相关函数：`groupBy`、`flatMap`、`sortedWith` `=>` `compareBy`、`thenBy`

#### 关键文件

- [HKeyframes 文件夹](app/src/main/assets/h_keyframes) - 存放所有共享关键H帧
- [DatabaseRepo.kt](app/src/main/java/com/yenaly/han1meviewer/logic/DatabaseRepo.kt) - 处理共享关键H帧
- [SharedHKeyframesRvAdapter.kt](app/src/main/java/com/yenaly/han1meviewer/ui/adapter/SharedHKeyframesRvAdapter.kt) - 界面 Adapter
- [HKeyframeEntity.kt](app/src/main/java/com/yenaly/han1meviewer/logic/entity/HKeyframeEntity.kt) - 相关实体类

#### 解释

很多人看到 [HKeyframes 文件夹](app/src/main/assets/h_keyframes) 先笑了，所有 JSON 文件都放一块，作者是个傻宝吧，这都不知道分文件夹来分类？

你以为我没想到吗？首先分文件夹为什么不太行：

1. 分文件夹无法一次性读取到对应影片的关键H帧。比如你正在看 `videoCode` 为 `114514` 的影片，我不分文件夹直接读取文件夹下的对应文件即可，不需要遍历各个文件夹去寻找，相当于 List 和 Map 的区别。
2. 假设分文件夹后，在根目录创建 JSON 来写好哪个文件夹包含哪些影片的代号，也不是不行，但是会增加其他想提供共享H帧的人的负担。

主要还是历史遗留问题，我懒得改了😄。Kotlin 这么多集合操作函数，分个组排个序不轻轻松松？

我现在给你一个关键H帧的 JSON，你来考虑考虑怎么转化为以下格式：

格式：

```
- 系列 1
	- 系列 1 第一集
	- 系列 1 第二集
	- 系列 1 第三集
- 系列 2
	- 系列 2 第一集
	- 系列 2 第二集
```

随机一段关键H帧：

> 你要注意，该网站的 `videoCode` 不是按照顺序排列的，第一集和第二集中间可能会夹带一个其他系列的影片。

```json
{
  "videoCode": "114514",
  "group": "系列 2",
  "title": "系列 2 第二集",
  "episode": 2,
  "author": "Bekki Chen",
  "keyframes": [
    {
      "position": 482500,
      "prompt": null
    },
    {
      "position": 500500,
      "prompt": null
    },
    {
      "position": 556000,
      "prompt": null
    },
    {
      "position": 777300,
      "prompt": null
    }
  ]
}
```

你可能想用 Map 分类，但是 RecyclerView 可是传不了 Map 的，那怎么才能扁平化成一个 List，并且能实现  RecyclerView 多布局呢？如果是两种截然不同的两个数据去实现 RecyclerView 多布局，不得不依靠接口，比如说本 App 中共享关键H帧界面中数据不一样的标题和内容。

聚焦于 [HKeyframeEntity.kt](app/src/main/java/com/yenaly/han1meviewer/logic/entity/HKeyframeEntity.kt)

```kotlin
interface MultiItemEntity {
    val itemType: Int
}

interface HKeyframeType : MultiItemEntity {
    companion object {
        const val H_KEYFRAME = 0
        const val HEADER = 1
    }
}
```

然后 HKeyframeEntity 和 HKeyframeHeader 我就不多说了，把正确的 `itemType` override 给对应的 `itemType` 字段就好。

现在问题是怎么读取那些共享关键H帧并将其扁平化？

聚焦于 [DatabaseRepo.kt](app/src/main/java/com/yenaly/han1meviewer/logic/DatabaseRepo.kt)

```kotlin
@OptIn(ExperimentalSerializationApi::class)
fun loadAllShared(): Flow<List<HKeyframeType>> = flow {
    val res = applicationContext.assets.let { assets ->
        // assets.list 方法获取到文件夹所有文件的 List
        assets.list("h_keyframes")?.asSequence() // 将其转化为一个序列
            ?.filter { it.endsWith(".json") } // 把其中结尾为 json 的挑出来
            ?.mapNotNull { fileName -> // 将 文件名 映射 为 文件，再通过 文件 转化为 实体
                try {
                    // assets.open 方法打开文件
                    assets.open("h_keyframes/$fileName").use { inputStream ->
                        Json.decodeFromStream<HKeyframeEntity>(inputStream)
                    }
                } catch (e: Exception) { // 出现问题返回 null
                    e.printStackTrace()
                    null
                }
            }
            ?.sortedWith(
                compareBy<HKeyframeEntity> { it.group }.thenBy { it.episode }
            ) // 排序，先以 group 进行排序，然后对 episode 进行排序
            ?.groupBy { it.group ?: "???" } // 分组，以 group 为 key，以 group 下的所有影片的列表为 value 建立 Map，若 group 为 null，加入组 ??? 里
            ?.flatMap { (group, entities) -> // 提供两个参数，分别为 key 和 value
                listOf(HKeyframeHeader(title = group, attached = entities)) + entities
            } // 关键：扁平化，group 与 entities 由主从关系变为并列关系
            .orEmpty() // 若 list 为 null，返回一个长度为 0 的空列表
    }
    emit(res)
}
```

然后在对应 RecyclerView 中设置好 `itemType`，再分 `itemType` 配置相关函数就可以了。

具体查看 [SharedHKeyframesRvAdapter.kt](app/src/main/java/com/yenaly/han1meviewer/ui/adapter/SharedHKeyframesRvAdapter.kt) 
