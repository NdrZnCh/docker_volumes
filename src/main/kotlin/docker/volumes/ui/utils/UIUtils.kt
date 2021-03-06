package docker.volumes.ui.utils

import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.ui.components.JBList
import docker.data.DockerVolume
import java.awt.event.ItemEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import kotlin.properties.Delegates

fun <T> DefaultListModel<T>.setNewList(other: List<T>) {
    this.clear()
    other.forEach { this.addElement(it) }
}

fun JCheckBox.bind(property: GraphProperty<Boolean>) {
    addItemListener { property.set(it.stateChange == ItemEvent.SELECTED) }
}

fun JBList<DockerVolume>.addRightClickActions(vararg actions: ActionDefinition) {
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
