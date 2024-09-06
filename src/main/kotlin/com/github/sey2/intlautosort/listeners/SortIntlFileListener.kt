package com.github.sey2.intlautosort.listeners

import com.github.sey2.intlautosort.actions.SortIntlAction
import com.github.sey2.intlautosort.enums.FileExtensionType
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager

class SaveDocumentListener(private val project: Project) : DocumentListener {
    override fun documentChanged(event: DocumentEvent) {
        val document: Document = event.document
        val virtualFile = VirtualFileManager.getInstance().findFileByUrl(document.toString())

        if (virtualFile != null && (virtualFile.extension == FileExtensionType.arb.name || virtualFile.extension == FileExtensionType.json.name)) {
            SortIntlAction().sortJsonFile(project, virtualFile)
        }
    }
}
