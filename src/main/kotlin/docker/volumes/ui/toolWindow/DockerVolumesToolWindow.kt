package docker.volumes.ui.toolWindow

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBList
import com.intellij.util.ui.JBUI
import docker.communicator.volumesList
import docker.data.DockerVolume
import docker.volumes.ui.toolWindow.actions.DockerVolumesToolBarActionGroup
import docker.volumes.ui.toolWindow.actions.volumeGenerateMountCommand
import docker.volumes.ui.toolWindow.actions.volumeInspectPopupAction
import docker.volumes.ui.utils.addRightClickPopupActions
import docker.volumes.ui.utils.setNewList
import icons.Icons
import java.awt.Font
import javax.swing.DefaultListModel

class DockerVolumesToolWindow : SimpleToolWindowPanel(true, true) {

    private val myList: JBList<DockerVolume> = createList()

    init {
        myList.cellRenderer = SimpleListCellRenderer.create { jbLabel, t, _ ->
            jbLabel.font = jbLabel.font.deriveFont(jbLabel.font.style or Font.BOLD)
            jbLabel.icon = Icons.DOCKER_VOLUME
            jbLabel.text = t.Name
        }

        myList.addRightClickPopupActions(volumeInspectPopupAction, volumeGenerateMountCommand)

        toolbar = JBUI.Panels.simplePanel(ActionManager.getInstance().run {
            createActionToolbar("DockerVolumesToolbar", DockerVolumesToolBarActionGroup(myList), true)
        }.component)

        setContent(ScrollPaneFactory.createScrollPane(myList))
    }

    private fun createList(): JBList<DockerVolume> {
        val model: DefaultListModel<DockerVolume> = DefaultListModel()
        model.setNewList(volumesList())
        return JBList(model)
    }
}
