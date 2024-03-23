package com.yenaly.han1meviewer.logic.model.github

import kotlinx.serialization.Serializable

@Serializable
data class CommitComparison(val commits: List<Commit>) {
    @Serializable
    data class Commit(val commit: CommitDetail) {
        @Serializable
        data class CommitDetail(val author: CommitAuthor, val message: String) {
            @Serializable
            data class CommitAuthor(val name: String)
        }
    }
}



