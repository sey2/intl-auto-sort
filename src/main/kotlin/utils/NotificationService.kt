package utils

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project

object NotificationService {
    fun showErrorNotification(project: Project, title: String, content: String) {
        val notification = Notification(
            "IntlAutoSort",
            title,
            content,
            NotificationType.ERROR
        )
        Notifications.Bus.notify(notification, project)
    }
}