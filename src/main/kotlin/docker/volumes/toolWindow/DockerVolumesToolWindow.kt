package docker.volumes.toolWindow

import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
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
import docker.volumes.NOTIFICATION_GROUP
import icons.Icons
import javax.swing.DefaultListModel

class DockerVolumesToolWindow : SimpleToolWindowPanel(true, true) {
    private val myListModel: DefaultListModel<DockerVolume> = DefaultListModel()
    private val myList: JBList<DockerVolume> = createList(myListModel)

    init {
        myList.cellRenderer = SimpleListCellRenderer.create { jbLabel, t, _ ->
            jbLabel.toolTipText = """
                <html>
                    <b>Created at:</b> ${t.CreatedAt}
                    <br>
                    <b>Labels:</b> ${t.Labels}
                    <br>
                    <b>Mountpoint:</b> ${t.Mountpoint}
                    <br>
                    <b>Driver:</b> ${t.Driver}
                    <br>
                    <b>Scope:</b> ${t.Scope}
                    <br>
                    <b>Options:</b> ${t.Options}
                </html>
            """

            jbLabel.icon = Icons.DOCKER_ICON
            jbLabel.text = t.Name
        }

        toolbar = createToolbar()
        setContent(ScrollPaneFactory.createScrollPane(myList))
    }

    private fun createList(model: DefaultListModel<DockerVolume>): JBList<DockerVolume> {
        /*
            For some unknown for me reason I can't use model.addAll in this place,
            because of failed build in github with "type mismatch" error, but in my
            machine (MacOS) everything looks OK.
         */
        volumesList().forEach { model.addElement(it) }
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

    private fun refreshList() {
        myListModel.clear()
        // see comment in createList function
        volumesList().forEach { myListModel.addElement(it) }
    }

    private fun notifyAboutError(exception: Exception, project: Project?) {
        val notification = NOTIFICATION_GROUP.createNotification(exception.message.orEmpty(), NotificationType.ERROR)

        notification.setTitle("Docker error")
        Notifications.Bus.notify(notification, project)
    }

    private inner class CreateAction : AnAction({ messagePointer("create.action") }, IconUtil.getAddIcon()) {
        override fun actionPerformed(event: AnActionEvent) {
            val wrapper = CreateVolumePopupWrapper()

            wrapper.applyAction = {
                when (val result = createVolume(it)) {
                    is Success -> if (!myListModel.contains(result.value)) {
                        myListModel.addElement(result.value)
                    }
                    is Failure -> notifyAboutError(result.reason, event.project)
                }
            }

            wrapper.showPopup(event.project)
        }
    }

    private inner class RemoveAction : AnAction({ messagePointer("remove.action") }, IconUtil.getRemoveIcon()) {
        override fun actionPerformed(event: AnActionEvent) {
            myList.selectedValuesList.forEach {
                when (val result = removeVolume(it.Name)) {
                    is Success -> myListModel.removeElement(it)
                    is Failure -> notifyAboutError(result.reason, event.project)
                }
            }
        }
    }

    private inner class RefreshAction : AnAction({ messagePointer("refresh.action") }, AllIcons.Actions.Refresh) {
        override fun actionPerformed(event: AnActionEvent) = refreshList()
    }

    private inner class PruneAction : AnAction({ messagePointer("prune.action") }, AllIcons.Actions.GC) {
        override fun actionPerformed(event: AnActionEvent) {
            volumePrune()
            refreshList()
        }
    }
}
