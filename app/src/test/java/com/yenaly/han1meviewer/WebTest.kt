package com.yenaly.han1meviewer

import org.jsoup.Jsoup
import org.junit.Test
import java.io.File

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/07 007 16:28
 */
class WebTest {
    @Test
    fun get_brands_tag() {
        val list = mutableListOf<String>()
        val source = File("C:\\Users\\wrzg8\\Desktop\\CQUPTDox\\haniime.html").readText()
        val jsoup = Jsoup.parse(source)
        jsoup.body().select("input[name*=brands]").forEach {
            list.add(it.attr("value"))
        }
        var string = "arrayOf("
        list.forEach {
            string += "\"$it\","
        }
        string += ")"
        println(string)
    }

    @Test
    fun get_tags_tag() {
        val list = mutableListOf<String>()
        val source = File("C:\\Users\\wrzg8\\Desktop\\CQUPTDox\\haniime.html").readText()
        val parseBody = Jsoup.parse(source).body()
        val tagsMap = linkedMapOf<String, List<String>>()
        val tagsClass = parseBody.getElementById("tags")!!
        val tagItems = tagsClass.select("div[class=modal-body]")[0].children()
        var tagType: String
        val tags = mutableListOf<String>()
        var string = ""
        tagItems.forEach { tagItem ->
            /*
            if (tagItem.`is`("h5")) {
                tagType = tagItem.text().substringBefore("：")
                if (tags.isNotEmpty()) tagsMap[tagType] = tags
                tags.clear()
            } else if (tagItem.`is`("label")) {
                val tagName = tagItem.select("span")[0]
                tags.add(tagName.text())
            }

             */
            val tagName = tagItem.select("span").first()?.text()
            tagName?.let { string += "\"$it\", " }
        }
        println(string)
    }

    @Test
    fun get_video_comment() {
        val source = File("C:\\Users\\wrzg8\\Desktop\\CQUPTDox\\hanime_comment.html").readText()
            .replace("\\n", "").replace("\\t", "").replace("\\\"", "\"")
            .replace("&lt;", "<").replace("&gt;", ">").replace("\\/", "/")
        val parseBody = Jsoup.parse(source).body()
        val comments = parseBody.getElementsByClass("comment-index-text")
        comments.forEach {
            println(it.text())
        }
    }
}