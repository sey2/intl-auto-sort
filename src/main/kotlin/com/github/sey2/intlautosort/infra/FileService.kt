package com.github.sey2.intlautosort.infra

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.nio.charset.StandardCharsets

class FileService {
    fun readFile(file: VirtualFile) = String(file.contentsToByteArray(), StandardCharsets.UTF_8)

    fun writeFile(project: Project, file: VirtualFile, content: String) =
        WriteCommandAction.runWriteCommandAction(project) {
            file.setBinaryContent(content.toByteArray(StandardCharsets.UTF_8))
        }
}