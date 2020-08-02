package docker.volumes.ui.toolWindow.dialogs

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.LCFlags
import com.intellij.ui.layout.panel
import docker.communicator.VolumeCreateArguments
import docker.volumes.DockerVolumesBundle
import docker.volumes.ui.components.DockerVolumePairPanel
import javax.swing.JComponent

class CreateVolumeDialog(private val alreadyDefinedNames: List<String>) : DialogWrapper(true) {

    companion object {
        private const val PANEL_WIDTH: Int = 500
        private const val PANEL_HEIGHT: Int = 500
    }

    private var myVolumeName = ""
    private var myVolumeDriver = "local"

    private val optionsPanel = DockerVolumePairPanel("Options:", possibleValues = mapOf(
            "type" to arrayOf("tmpfs", "btrfs", "nfs"),
            "device" to arrayOf(),
            "o" to arrayOf()
    )
    )
    private val labelsPanel = DockerVolumePairPanel("Labels:")

    var applyAction: (VolumeCreateArguments) -> Unit = {}

    init {
        this.init()
        this.title = DockerVolumesBundle.messagePointer("new.volume.dialog.title")
    }

    override fun createCenterPanel(): JComponent? {
        return panel(LCFlags.fillX) {
            row("Name:") {
                textField({ myVolumeName }, { myVolumeName = it }).withValidationOnInput {
                    val value = it.text

                    when {
                        value.isBlank() -> null
                        value.length < 2 -> error("Only empty or with length greater than 1 names are allowed.")
                        !"([a-zA-Z0-9])+".toRegex().matches(value) -> {
                            error("Only '[a-zA-Z0-9]' are allowed.")
                        }
                        alreadyDefinedNames.contains(value) -> error("Volume with name '$value' already defined!")
                        else -> null
                    }
                }.focused()
            }
            row("Driver:") {
                textField({ myVolumeDriver }, { myVolumeDriver = it })
            }
            row { optionsPanel(growX, growY, pushY) }
            row { labelsPanel(growX, growY, pushY) }
        }.withPreferredSize(PANEL_WIDTH, PANEL_HEIGHT)
    }

    override fun doOKAction() {
        super.doOKAction()

        applyAction(VolumeCreateArguments {
            name = myVolumeName
            driver = myVolumeDriver
            options = optionsPanel.data.toMap()
            labels = labelsPanel.data.toMap()
        })
    }
}