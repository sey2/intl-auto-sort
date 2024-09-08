package com.github.sey2.intlautosort.actions

import com.github.sey2.intlautosort.domain.json.JsonFixer
import com.github.sey2.intlautosort.domain.json.JsonSorter
import com.github.sey2.intlautosort.domain.usecase.SortJsonFileUseCase
import com.github.sey2.intlautosort.enums.FileExtensionType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.github.sey2.intlautosort.infra.FileService
import com.github.sey2.intlautosort.utils.NotificationService

class SortIntlAction(
    private val sortJsonFileUseCase: SortJsonFileUseCase = SortJsonFileUseCase(
        JsonSorter(),
        FileService(),
        JsonFixer(),
    )
) : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return

        e.getData(CommonDataKeys.VIRTUAL_FILE)?.run {
            if (this.isDirectory) {
                this.children.forEach { childFile ->
                    handleToSortFiles(project, childFile)
                }
            } else {
                handleToSortFiles(project, this)
            }
        }
    }

    private fun handleToSortFiles(project: Project, file: VirtualFile) {
        when (file.extension) {
            FileExtensionType.arb.name, FileExtensionType.json.name -> {
                sortJsonFileUseCase.execute(project, file)
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
