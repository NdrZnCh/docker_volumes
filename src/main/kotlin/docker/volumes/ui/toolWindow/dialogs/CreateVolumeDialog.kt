package docker.volumes.ui.toolWindow.dialogs

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.LCFlags
import com.intellij.ui.layout.panel
import docker.communicator.VolumeCreateArguments
import docker.volumes.DockerVolumesBundle.messagePointer
import docker.volumes.ui.components.DockerVolumePairPanel
import javax.swing.JComponent

class CreateVolumeDialog(private val alreadyDefinedNames: List<String>) : DialogWrapper(true) {

    companion object {
        private const val PANEL_WIDTH: Int = 500
        private const val PANEL_HEIGHT: Int = 500
    }

    private var myVolumeName = ""
    private var myVolumeDriver = "local"

    private val myOptionsValueMap = mapOf(
            "type" to arrayOf("tmpfs", "btrfs", "nfs"),
            "device" to arrayOf(),
            "o" to arrayOf()
    )

    private val myOptionsPanel = DockerVolumePairPanel(
            messagePointer("docker.dialogs.createVolume.options.title"),
            possibleValues = myOptionsValueMap)

    private val myLabelsPanel = DockerVolumePairPanel(messagePointer("docker.dialogs.createVolume.labels.title"))

    var applyAction: (VolumeCreateArguments) -> Unit = {}

    init {
        this.init()
        this.title = messagePointer("new.volume.dialog.title")
    }

    override fun createCenterPanel(): JComponent? {
        return panel(LCFlags.fillX) {
            row(messagePointer("docker.dialogs.createVolume.name.title")) {
                textField({ myVolumeName }, { myVolumeName = it }).withValidationOnInput {
                    val value = it.text

                    when {
                        value.isBlank() -> null
                        value.length < 2 -> error(messagePointer("docker.dialogs.createVolume.errors.nameLength"))
                        !"([a-zA-Z0-9])+".toRegex().matches(value) -> {
                            error(messagePointer("docker.dialogs.createVolume.errors.nameRegex"))
                        }
                        alreadyDefinedNames.contains(value) -> {
                            error(messagePointer("docker.dialogs.createVolume.errors.nameAlreadyDefined", value))
                        }
                        else -> null
                    }
                }.focused()
            }
            row(messagePointer("docker.dialogs.createVolume.driver.title")) {
                textField({ myVolumeDriver }, { myVolumeDriver = it })
            }
            row { myOptionsPanel(growX, growY, pushY) }
            row { myLabelsPanel(growX, growY, pushY) }
        }.withPreferredSize(PANEL_WIDTH, PANEL_HEIGHT)
    }

    override fun doOKAction() {
        super.doOKAction()

        applyAction(VolumeCreateArguments {
            name = myVolumeName
            driver = myVolumeDriver
            options = myOptionsPanel.data.toMap()
            labels = myLabelsPanel.data.toMap()
        })
    }
}
