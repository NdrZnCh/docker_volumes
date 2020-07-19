package docker.volumes

import com.intellij.AbstractBundle
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val BUNDLE = "messages.DockerVolumesBundle"

@NonNls
val NOTIFICATION_GROUP = NotificationGroup("Docker volumes", NotificationDisplayType.BALLOON, true)

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
