package docker.volumes.ui.toolWindow.dialogs

import com.intellij.openapi.observable.properties.GraphPropertyImpl.Companion.graphProperty
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.applyToComponent
import com.intellij.ui.layout.panel
import docker.data.DockerVolume
import docker.volumes.DockerVolumesBundle.messagePointer
import docker.volumes.ui.utils.bind
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import javax.swing.JComponent

class GenerateMountOptionDialog(private val selectedValue: DockerVolume) : DialogWrapper(false) {

    private val propertyGraph = PropertyGraph()

    private val mountDestination = propertyGraph.graphProperty { '/' + selectedValue.Name }
    private val isReadonlyMount = propertyGraph.graphProperty { false }
    private val mountOption = propertyGraph.graphProperty { buildMountOption() }

    companion object {
        private const val COMMENT_LENGTH = 100

        private const val PANEL_WIDTH: Int = 500
        private const val PANEL_HEIGHT: Int = 100
    }

    init {
        mountOption.dependsOn(mountDestination, ::buildMountOption)
        mountOption.dependsOn(isReadonlyMount, ::buildMountOption)

        title = messagePointer("docker.dialogs.generateMountOption.title")
        setOKButtonText(messagePointer("docker.dialogs.generateMountOption.okButton"))
        init()
    }

    override fun createCenterPanel(): JComponent? = panel {
        row(messagePointer("docker.dialogs.generateMountOption.destination.title")) {
            textField(mountDestination)
        }
        row {
            val title = messagePointer("docker.dialogs.generateMountOption.readonly.title")
            checkBox(title, isReadonlyMount::get, isReadonlyMount::set).apply {
                withGraphProperty(isReadonlyMount)
                applyToComponent { bind(isReadonlyMount) }
            }
        }
        row {
            textField(mountOption).apply {
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
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(mountOption.get()), null)
    }
}
