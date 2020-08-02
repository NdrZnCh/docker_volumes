package docker.volumes.ui.toolWindow.actions

import com.google.gson.GsonBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.observable.properties.GraphPropertyImpl.Companion.graphProperty
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.applyToComponent
import com.intellij.ui.layout.panel
import docker.data.DockerVolume
import docker.volumes.ui.utils.ActionDefinition
import docker.volumes.ui.utils.bind
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import javax.swing.JComponent
import javax.swing.JPopupMenu
import javax.swing.JTextPane

val volumeInspectPopupAction = ActionDefinition {
    title = "Volume inspect..."
    icon = AllIcons.Actions.Show
    action = { list, e, _ ->
        JPopupMenu().apply {
            add(JTextPane().apply {
                isEditable = false
                contentType = "text/html"
                text = """
                    <html>
                        <pre>${GsonBuilder().setPrettyPrinting().create().toJson(list.selectedValue)}</pre>
                    </html>
                """
            })
        }.show(list, e?.point?.x ?: 0, e?.point?.y ?: 0)
    }
}

val volumeGenerateMountCommand = ActionDefinition {
    title = "Generate Mount Option..."
    icon = AllIcons.Actions.Compile
    action = { list, _, _ ->
        MyGenerateMountAttributeDialog(list.selectedValue).show()
    }
}

private class MyGenerateMountAttributeDialog(private val selectedValue: DockerVolume) : DialogWrapper(false) {

    private val propertyGraph = PropertyGraph()

    private val mountDestination = propertyGraph.graphProperty { '/' + selectedValue.Name }
    private val isReadonlyMount = propertyGraph.graphProperty { false }
    private val mountAttribute = propertyGraph.graphProperty { buildMountOption() }

    companion object {
        private const val PANEL_WIDTH: Int = 500
        private const val PANEL_HEIGHT: Int = 100
    }

    init {
        mountAttribute.dependsOn(mountDestination, ::buildMountOption)
        mountAttribute.dependsOn(isReadonlyMount, ::buildMountOption)

        title = "Generate Mount Option"
        setOKButtonText("Copy to clipboard")
        init()
    }

    override fun createCenterPanel(): JComponent? = panel {
        row("Destination:") {
            textField(mountDestination)
        }
        row {
            checkBox("Readonly", isReadonlyMount::get, isReadonlyMount::set).apply {
                withGraphProperty(isReadonlyMount)
                applyToComponent { bind(isReadonlyMount) }
            }
        }
        row {
            textField(mountAttribute).apply {
                component.isEditable = false
            }
        }
    }.withPreferredSize(PANEL_WIDTH, PANEL_HEIGHT)

    private fun buildMountOption(): String {
        return mutableListOf<String>().apply {
            add("--mount type=volume")
            add("source=${selectedValue.Name}")
            add("destination=${mountDestination.get()}")
            if (isReadonlyMount.get()) add("readonly")
        }.joinToString()
    }

    override fun doOKAction() {
        super.doOKAction()
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(mountAttribute.get()), null)
    }
}
