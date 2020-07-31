package docker.volumes.ui.utils

import com.intellij.ui.components.JBList
import docker.data.DockerVolume
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Icon
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.SwingUtilities
import kotlin.properties.Delegates

fun JBList<DockerVolume>.addRightClickPopupActions(vararg actions: ActionDefinition) {
    val list = this

    this.addMouseListener(object : MouseAdapter() {
        override fun mousePressed(e: MouseEvent?) {
            if (SwingUtilities.isRightMouseButton(e)) {
                JPopupMenu().apply {
                    actions.forEach { a ->
                        this.add(JMenuItem(a.title, a.icon)).apply {
                            addActionListener { a.action(list, e, this) }
                        }
                    }
                }.show(list, e?.point?.x ?: 0, e?.point?.y ?: 0)
            }
        }
    })
}

class ActionDefinition() {
    constructor(init: ActionDefinition.() -> Unit) : this() {
        init()
    }

    var title: String by Delegates.notNull()
    var icon: Icon by Delegates.notNull()
    var action: ((list: JBList<DockerVolume>, e: MouseEvent?, menuItem: JMenuItem) -> Unit) by Delegates.notNull()
}