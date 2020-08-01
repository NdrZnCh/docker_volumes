package docker.volumes.ui.toolWindow.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.ui.components.JBList
import com.intellij.util.IconUtil
import docker.communicator.*
import docker.data.DockerVolume
import docker.volumes.DockerVolumesBundle.messagePointer
import docker.volumes.notifyAboutError
import docker.volumes.ui.toolWindow.CreateVolumeDialogWrapper
import docker.volumes.ui.utils.setNewList
import javax.swing.DefaultListModel
import javax.swing.Icon
import org.jetbrains.concurrency.runAsync

class DockerVolumesToolBarActionGroup(val list: JBList<DockerVolume>) : DefaultActionGroup() {

    private val listModel: DefaultListModel<DockerVolume> = list.model as DefaultListModel<DockerVolume>

    init {
        add(CreateAction())
        add(RemoveAction())
        add(RefreshAction())
        add(PruneAction())
    }

    private abstract class DockerAction(text: String, icon: Icon) : AnAction({ text }, icon), DumbAware

    private inner class CreateAction : DockerAction(messagePointer("create.action"), IconUtil.getAddIcon()) {
        override fun actionPerformed(event: AnActionEvent) {
            val wrapper = CreateVolumeDialogWrapper(listModel.elements().toList().map { it.Name })

            wrapper.applyAction = {
                runAsync {
                    when (val result = createVolume(it)) {
                        is Success -> if (!listModel.contains(result.value)) {
                            listModel.addElement(result.value)
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
                list.selectedValuesList.forEach {
                    when (val result = removeVolume(it.Name)) {
                        is Success -> listModel.removeElement(it)
                        is Failure -> notifyAboutError(result.reason, event.project)
                    }
                }
            }
        }
    }

    private inner class RefreshAction : DockerAction(messagePointer("refresh.action"), AllIcons.Actions.Refresh) {
        override fun actionPerformed(event: AnActionEvent) {
            runAsync { listModel.setNewList(volumesList()) }
        }
    }

    private inner class PruneAction : DockerAction(messagePointer("prune.action"), AllIcons.Actions.GC) {
        override fun actionPerformed(event: AnActionEvent) {
            runAsync {
                volumePrune()
                listModel.setNewList(volumesList())
            }
        }
    }
}
