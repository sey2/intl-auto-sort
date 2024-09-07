package com.github.sey2.intlautosort.actions

import com.github.sey2.intlautosort.enums.FileExtensionType
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.MalformedJsonException
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import utils.NotificationService.showErrorNotification
import java.nio.charset.StandardCharsets
import java.util.*

class SortIntlAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val virtualFile: VirtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        when (virtualFile.extension) {
            FileExtensionType.arb.name, FileExtensionType.json.name -> {
                handleJsonSorting(project, virtualFile)
            }
        }
    }

    fun handleJsonSorting(project: Project, file: VirtualFile) {
        val content = String(file.contentsToByteArray(), StandardCharsets.UTF_8)

        sortJsonFileWithContent(project, file, content)
            .onFailure { ex ->
                when (ex) {
                    is MalformedJsonException -> {
                        handleJsonSyntaxError(project, file, ex)
                    }

                    else -> {
                        showErrorNotification(project, "Error", "An unexpected error occurred: ${ex.message}")
                    }
                }
            }
    }

    private fun handleJsonSyntaxError(project: Project, virtualFile: VirtualFile, ex: MalformedJsonException) {
        showErrorNotification(
            project,
            "JSON Syntax Error",
            "There is a syntax error in the JSON file: ${ex.message}"
        )

        fixMalformedJson(String(virtualFile.contentsToByteArray(), StandardCharsets.UTF_8))
            .onSuccess { fixedContent ->
                tryToSortFixedJson(project, virtualFile, fixedContent)
            }
            .onFailure {
                showErrorNotification(
                    project,
                    "Fix Unsuccessful",
                    "Unable to automatically fix the JSON syntax."
                )
            }
    }

    private fun tryToSortFixedJson(project: Project, virtualFile: VirtualFile, fixedContent: String) {
        sortJsonFileWithContent(project, virtualFile, fixedContent)
            .onFailure { ex ->
                showErrorNotification(
                    project,
                    "Fix Failed",
                    "Failed to fix the JSON file: ${ex.message}"
                )
            }
    }

    private fun sortJsonFileWithContent(project: Project, file: VirtualFile, content: String): Result<Unit> {
        return runCatching {
            JsonParser.parseString(content).asJsonObject.run {
                entrySet()
                    .sortedBy { it.key.lowercase(Locale.getDefault()) }
                    .associate { it.key to it.value }
                    .let { sortedEntries ->
                        GsonBuilder()
                            .setPrettyPrinting()
                            .disableHtmlEscaping()
                            .create()
                            .toJson(JsonObject().apply {
                                sortedEntries.forEach { (key, value) -> add(key, value) }
                            })
                    }.let { sortedJson ->
                        WriteCommandAction.runWriteCommandAction(project) {
                            file.setBinaryContent(sortedJson.toByteArray(StandardCharsets.UTF_8))
                        }
                    }
            }
        }
    }

    private fun fixMalformedJson(content: String): Result<String> {
        return runCatching {
            content
                .replace("\n", "")
                .replace(",}", "}")
                .apply { JsonParser.parseString(this) }
        }
    }
}
