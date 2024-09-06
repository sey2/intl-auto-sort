package com.github.sey2.intlautosort.actions

import com.github.sey2.intlautosort.enums.FileExtensionType
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.nio.charset.StandardCharsets
import java.util.*

class SortIntlAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val virtualFile: VirtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        when (virtualFile.extension) {
            FileExtensionType.arb.name, FileExtensionType.json.name -> {
                sortJsonFile(project, virtualFile)
            }
        }
    }

    fun sortJsonFile(project: Project, file: VirtualFile) {
        val content = String(file.contentsToByteArray(), StandardCharsets.UTF_8)
        val jsonObject = JsonParser.parseString(content).asJsonObject

        val sorted = jsonObject.entrySet()
            .sortedBy { it.key.lowercase(Locale.getDefault()) }
            .associate { it.key to it.value }

        val gson = GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create()

        val sortedJson = gson.toJson(JsonObject().apply {
            sorted.forEach { (key, value) -> add(key, value) }
        })

        WriteCommandAction.runWriteCommandAction(project) {
            file.setBinaryContent(sortedJson.toByteArray(StandardCharsets.UTF_8))
        }
    }
}
