package docker.volumes.toolWindow

import com.google.gson.GsonBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBList
import com.intellij.util.IconUtil
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.components.BorderLayoutPanel
import docker.communicator.Failure
import docker.communicator.Success
import docker.communicator.createVolume
import docker.communicator.removeVolume
import docker.communicator.volumePrune
import docker.communicator.volumesList
import docker.data.DockerVolume
import docker.volumes.DockerVolumesBundle.messagePointer
import docker.volumes.notifyAboutError
import icons.Icons
import org.jetbrains.concurrency.runAsync
import javax.swing.DefaultListModel
import javax.swing.Icon

class DockerVolumesToolWindow : SimpleToolWindowPanel(true, true) {
    private val myListModel: DefaultListModel<DockerVolume> = DefaultListModel()
    private val myList: JBList<DockerVolume> = createList(myListModel)

    init {
        myList.cellRenderer = SimpleListCellRenderer.create { jbLabel, t, _ ->
            jbLabel.toolTipText = """
                <html>
                   <pre>${GsonBuilder().setPrettyPrinting().create().toJson(t)}</pre>
                </html>
            """

            jbLabel.icon = Icons.DOCKER_ICON
            jbLabel.text = t.Name
        }

        toolbar = createToolbar()
        setContent(ScrollPaneFactory.createScrollPane(myList))
    }

    private fun createList(model: DefaultListModel<DockerVolume>): JBList<DockerVolume> {
        model.setNewList(volumesList())
        return JBList(model)
    }

    private fun createToolbar(): BorderLayoutPanel {
        val group = DefaultActionGroup()

        with(group) {
            add(CreateAction())
            add(RemoveAction())
            add(RefreshAction())
            add(PruneAction())
        }

        val actionToolBar = ActionManager.getInstance().createActionToolbar("DockerVolumesToolbar", group, true)
        return JBUI.Panels.simplePanel(actionToolBar.component)
    }

    /**
     * ACTIONS
     */
    private abstract class DockerAction(text: String, icon: Icon) : AnAction({ text }, icon), DumbAware

    private inner class CreateAction : DockerAction(messagePointer("create.action"), IconUtil.getAddIcon()) {
        override fun actionPerformed(event: AnActionEvent) {
            val wrapper = CreateVolumeDialogWrapper()

            wrapper.applyAction = {
                runAsync {
                    when (val result = createVolume(it)) {
                        is Success -> if (!myListModel.contains(result.value)) {
                            myListModel.addElement(result.value)
                        }
                        is Failure -> notifyAboutError(result.reason, event.project)
                    }
                }
            }

            wrapper.show()
        }
    }

    private inner class RemoveAction : DockerAction(messagePointer("remove.action"), IconUtil.getRemoveIcon()) {
        override fun actionPerformed(event: AnActionEvent) {
            runAsync {
                myList.selectedValuesList.forEach {
                    when (val result = removeVolume(it.Name)) {
                        is Success -> myListModel.removeElement(it)
                        is Failure -> notifyAboutError(result.reason, event.project)
                    }
                }
            }
        }
    }

    private inner class RefreshAction : DockerAction(messagePointer("refresh.action"), AllIcons.Actions.Refresh) {
        override fun actionPerformed(event: AnActionEvent) {
            runAsync { myListModel.setNewList(volumesList()) }
        }
    }

    private inner class PruneAction : DockerAction(messagePointer("prune.action"), AllIcons.Actions.GC) {
        override fun actionPerformed(event: AnActionEvent) {
            runAsync {
                volumePrune()
                myListModel.setNewList(volumesList())
            }
        }
    }
}

private fun <T> DefaultListModel<T>.setNewList(other: List<T>) {
    this.clear()
    other.forEach { this.addElement(it) }
}
