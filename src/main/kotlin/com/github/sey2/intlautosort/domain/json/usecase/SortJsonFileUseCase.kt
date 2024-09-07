package com.github.sey2.intlautosort.domain.json.usecase

import com.github.sey2.intlautosort.domain.json.JsonFixer
import com.github.sey2.intlautosort.domain.json.JsonSorter
import com.google.gson.stream.MalformedJsonException
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.github.sey2.intlautosort.infra.FileService
import com.github.sey2.intlautosort.utils.NotificationService

class SortJsonFileUseCase(
    private val jsonSorter: JsonSorter,
    private val fileService: FileService,
    private val jsonFixer: JsonFixer,
) {
    fun execute(project: Project, file: VirtualFile) = runCatching {
        fileService.readFile(file).apply {
            jsonSorter.sortJson(this).apply {
                fileService.writeFile(project, file, this)
            }
        }
    }.onFailure { ex ->
        when (ex) {
            is MalformedJsonException -> {
                handleJsonSyntaxError(project, file, ex)
            }

            else -> {
                throw ex
            }
        }
    }

    private fun handleJsonSyntaxError(project: Project, file: VirtualFile, ex: MalformedJsonException) {
        NotificationService.showErrorNotification(
            project,
            "JSON Syntax Error",
            "There is a syntax error in the JSON file: ${ex.message}"
        )

        jsonFixer.fixMalformedJson(fileService.readFile(file)).run {
            onSuccess { content ->
                tryToSortFixedJson(project, file, content)
            }.onFailure {
                NotificationService.showErrorNotification(
                    project,
                    "Fix Unsuccessful",
                    "Unable to automatically fix the JSON syntax."
                )
            }
        }
    }

    private fun tryToSortFixedJson(project: Project, file: VirtualFile, fixedContent: String) {
        runCatching {
            jsonSorter.sortJson(fixedContent).run {
                fileService.writeFile(project, file, this)
            }
        }.onFailure { ex ->
            NotificationService.showErrorNotification(
                project,
                "Fix Failed",
                "Failed to fix the JSON file: ${ex.message}"
            )
        }
    }
}