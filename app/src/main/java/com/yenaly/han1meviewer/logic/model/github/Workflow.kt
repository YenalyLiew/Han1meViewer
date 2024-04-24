package com.yenaly.han1meviewer.logic.model.github

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2024/03/21 021 09:03
 */
@Serializable
data class WorkflowRuns(
    @SerialName("workflow_runs") val workflowRuns: List<WorkflowRun>,
) {
    @Serializable
    data class WorkflowRun(
        @SerialName("head_sha") val headSha: String,
        @SerialName("display_title") val title: String,
        @SerialName("artifacts_url") val artifactsUrl: String,
    )
}


@Serializable
data class Artifacts(
    @SerialName("artifacts") val artifacts: List<Artifact>,
) {
    val downloadLink: String get() = artifacts.first().downloadLink
    val nodeId: String get() = artifacts.first().nodeId

    @Serializable
    data class Artifact(
        val name: String,
        @SerialName("archive_download_url") val downloadLink: String,
        @SerialName("node_id") val nodeId: String,
    )
}