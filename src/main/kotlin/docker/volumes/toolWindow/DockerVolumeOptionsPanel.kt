package docker.volumes.toolWindow

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.AddEditRemovePanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.ui.layout.panel
import javax.swing.JComponent

class DockerVolumeOptionsPanel : AddEditRemovePanel<Pair<String, String>>(
        MyOptionsTableModel(), mutableListOf(), "Options:"
) {
    override fun removeItem(option: Pair<String, String>): Boolean = true

    override fun editItem(option: Pair<String, String>): Pair<String, String>? = doAddOrEdit(option)

    override fun addItem(): Pair<String, String>? = doAddOrEdit(null)

    private fun doAddOrEdit(option: Pair<String, String>?): Pair<String, String>? {
        val optionDialog = MyAddOrEditOptionDialog(option)
        return if (!optionDialog.showAndGet()) null else optionDialog.getValue().takeIf {
            it.first.isNotBlank() && it.second.isNotBlank()
        }
    }

    private class MyOptionsTableModel : TableModel<Pair<String, String>>() {
        override fun getColumnCount(): Int = 2

        override fun getColumnName(column: Int): String = if (column == 0) "Name" else "Value"

        override fun getField(o: Pair<String, String>, c: Int): String = if (c == 0) o.first else o.second
    }

    private class MyAddOrEditOptionDialog(option: Pair<String, String>?) : DialogWrapper(false) {
        private var myOptionName: String = option?.first.orEmpty()
        private var myOptionValue: String = option?.second.orEmpty()

        private val validator: (ValidationInfoBuilder.(JBTextField) -> ValidationInfo?) = {
            val value = it.text

            when {
                value.isBlank() -> error("Can't be empty")
                !"([a-zA-Z0-9_.,{}=-])+".toRegex().matches(value) -> {
                    error("Only '[a-zA-Z0-9_.,{}=-]' are allowed.")
                }
                else -> null
            }
        }

        init {
            init()
            this.title = if (option == null) "Add new volume option" else "Edit volume option"
        }

        override fun createCenterPanel(): JComponent? = panel {
            row("Name:") {
                textField({ myOptionName }, { myOptionName = it }).withValidationOnApply(validator).focused()
            }
            row("Value:") {
                textField({ myOptionValue }, { myOptionValue = it }).withValidationOnApply(validator)
            }
        }

        fun getValue() = Pair(myOptionName, myOptionValue)
    }
}