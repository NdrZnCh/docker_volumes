package docker.volumes.toolWindow

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.LCFlags
import com.intellij.ui.layout.panel
import docker.volumes.DockerVolumesBundle
import org.jetbrains.concurrency.runAsync
import javax.swing.JComponent


class CreateVolumeDialogWrapper : DialogWrapper(true) {
    private var myVolumeName = ""
    private var myVolumeDriver = "local"
    private var myVolumeIsReadonly: Boolean = false

    private val optionsPanel = DockerVolumeOptionsPanel()
    private val labelsPanel = DockerVolumeLabelsPanel()

    var applyAction: (String) -> Unit = {}

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
                        !"([a-zA-Z0-9_.-])+".toRegex().matches(value) -> {
                            error("Only '[a-zA-Z0-9_.-]' are allowed.")
                        }
                        else -> null
                    }
                }.focused()
            }
            row("Driver:") {
                textField({ myVolumeDriver }, { myVolumeDriver = it })
            }
            row {
                checkBox("Readonly", { myVolumeIsReadonly }, { myVolumeIsReadonly = it })
            }
            row { optionsPanel(growX, growY, pushY) }
            row { labelsPanel(growX, growY, pushY) }
        }
    }

    override fun doOKAction() {
        super.doOKAction()

        runAsync { applyAction(myVolumeName) }
    }
}
