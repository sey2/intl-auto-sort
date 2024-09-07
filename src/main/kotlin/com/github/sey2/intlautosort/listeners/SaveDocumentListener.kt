package com.github.sey2.intlautosort.listeners

import com.github.sey2.intlautosort.domain.json.JsonFixer
import com.github.sey2.intlautosort.domain.json.JsonSorter
import com.github.sey2.intlautosort.domain.json.usecase.SortJsonFileUseCase
import com.github.sey2.intlautosort.enums.FileExtensionType
import com.github.sey2.intlautosort.infra.FileService
import com.github.sey2.intlautosort.utils.NotificationService
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager

class SaveDocumentListener(
    private val project: Project,
    private val sortJsonFileUseCase: SortJsonFileUseCase = SortJsonFileUseCase(
        JsonSorter(),
        FileService(),
        JsonFixer(),
    ),
) : DocumentListener {
    override fun documentChanged(event: DocumentEvent) {
        val document: Document = event.document
        val virtualFile = VirtualFileManager.getInstance().findFileByUrl(document.toString())

        if (virtualFile != null) {
            when (virtualFile.extension) {
                FileExtensionType.arb.name, FileExtensionType.json.name -> {
                    sortJsonFileUseCase.execute(project, virtualFile)
                        .onFailure { ex ->
                            NotificationService.showErrorNotification(
                                project,
                                "Error",
                                "An unexpected error occurred: ${ex.message}"
                            )
                        }
                }
            }
        }
    }
}
