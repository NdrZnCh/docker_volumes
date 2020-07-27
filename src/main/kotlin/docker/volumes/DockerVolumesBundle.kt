package docker.volumes

import com.intellij.AbstractBundle
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val BUNDLE = "messages.DockerVolumesBundle"

@NonNls
private val NOTIFICATION_GROUP = NotificationGroup("Docker volumes", NotificationDisplayType.BALLOON, true)

object DockerVolumesBundle : AbstractBundle(BUNDLE) {

    @Suppress("SpreadOperator")
    @JvmStatic
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) = getMessage(key, *params)

    @Suppress("SpreadOperator")
    @JvmStatic
    fun messagePointer(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) = run {
        message(key, *params)
    }
}

fun notifyAboutError(errorMessage: String, project: Project?) {
    val notification = NOTIFICATION_GROUP.createNotification(errorMessage, NotificationType.ERROR)

    notification.setTitle("Docker error")
    Notifications.Bus.notify(notification, project)
}
