package docker.volumes.toolWindow

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.AddEditRemovePanel
import com.intellij.ui.layout.panel
import javax.swing.JComponent

class DockerVolumeLabelsPanel : AddEditRemovePanel<String>(MyLabelsTableModel(), mutableListOf(), "Labels:") {
    override fun removeItem(o: String?): Boolean = true

    override fun editItem(o: String?): String? = doAddOfEdit(o)

    override fun addItem(): String? = doAddOfEdit(null)

    private fun doAddOfEdit(label: String?): String? {
        val labelDialog = MyAddOrEditLabelDialog(label)
        return if (!labelDialog.showAndGet()) null else labelDialog.labelValue
    }

    private class MyAddOrEditLabelDialog(label: String?) : DialogWrapper(false) {
        var labelValue = label.orEmpty()

        init {
            init()
            this.title = if (label == null) "Add new volume label" else "Edit volume label"
        }

        override fun createCenterPanel(): JComponent? = panel {
            row("Value:") {
                textField({ labelValue }, { labelValue = it }).withValidationOnApply {
                    val value = it.text

                    when {
                        value.isBlank() -> error("Can't be empty")
                        !"([a-zA-Z0-9_.,{}=-])+".toRegex().matches(value) -> {
                            error("Only '[a-zA-Z0-9_.,{}=-]' are allowed.")
                        }
                        else -> null
                    }
                }.focused()
            }
        }
    }

    private class MyLabelsTableModel : TableModel<String>() {
        override fun getField(o: String, columnIndex: Int): String = o

        override fun getColumnName(columnIndex: Int): String? = "Label"

        override fun getColumnCount(): Int = 1
    }
}