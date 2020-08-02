package docker.volumes.ui.toolWindow.actions

import com.google.gson.GsonBuilder
import com.intellij.icons.AllIcons
import docker.volumes.ui.toolWindow.dialogs.GenerateMountOptionDialog
import docker.volumes.ui.utils.ActionDefinition
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
        GenerateMountOptionDialog(list.selectedValue).show()
    }
}
