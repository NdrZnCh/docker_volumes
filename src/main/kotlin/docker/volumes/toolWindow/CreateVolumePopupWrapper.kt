package docker.volumes.toolWindow

import com.intellij.ide.ui.newItemPopup.NewItemPopupUtil
import com.intellij.ide.ui.newItemPopup.NewItemSimplePopupPanel
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import docker.volumes.DockerVolumesBundle.messagePointer
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JTextField

class CreateVolumePopupWrapper {
    private val myPopup: JBPopup
    private val myContent: NewItemSimplePopupPanel = NewItemSimplePopupPanel()
    private val myNameField: JTextField = myContent.textField

    var applyAction: (String) -> Unit = {}

    init {
        myNameField.removeKeyListener(myNameField.keyListeners.firstOrNull()) // Remove old key listener
        myNameField.addKeyListener(EnterPressedListener())

        myPopup = NewItemPopupUtil.createNewItemPopup(messagePointer("new.volume.popup.title"), myContent, myNameField)
    }

    fun showPopup(project: Project?) = if (project == null) {
        myPopup.showInFocusCenter()
    } else myPopup.showCenteredInCurrentWindow(project)

    private inner class EnterPressedListener : KeyListener {
        override fun keyTyped(e: KeyEvent?) {
            // not used
        }

        override fun keyReleased(e: KeyEvent?) {
            // not used
        }

        override fun keyPressed(e: KeyEvent) {
            if (e.keyCode == KeyEvent.VK_ENTER) {
                val newName = myNameField.text

                if (newName.isNotBlank()) {
                    applyAction.invoke(newName)
                    myPopup.closeOk(e)
                }
            }
        }
    }
}
