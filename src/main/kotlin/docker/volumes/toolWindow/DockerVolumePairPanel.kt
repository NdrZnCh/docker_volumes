package docker.volumes.toolWindow

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.AddEditRemovePanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.ui.layout.panel
import javax.swing.JComponent

class DockerVolumePairPanel(title: String) : AddEditRemovePanel<Pair<String, String>>(
        MyPairTableModel(), mutableListOf(), title
) {
    override fun removeItem(option: Pair<String, String>): Boolean = true

    override fun editItem(option: Pair<String, String>): Pair<String, String>? = doAddOrEdit(option)

    override fun addItem(): Pair<String, String>? = doAddOrEdit(null)

    private fun doAddOrEdit(value: Pair<String, String>?): Pair<String, String>? {
        val optionDialog = MyAddOrEditPairDialog(value)
        return if (!optionDialog.showAndGet()) null else optionDialog.getValue().takeIf {
            it.first.isNotBlank() && it.second.isNotBlank()
        }
    }

    private class MyPairTableModel : TableModel<Pair<String, String>>() {
        override fun getColumnCount(): Int = 2

        override fun getColumnName(column: Int): String = if (column == 0) "Name" else "Value"

        override fun getField(o: Pair<String, String>, c: Int): String = if (c == 0) o.first else o.second
    }

    private class MyAddOrEditPairDialog(option: Pair<String, String>?) : DialogWrapper(false) {
        private var myOptionName: String = option?.first.orEmpty()
        private var myOptionValue: String = option?.second.orEmpty()

        private fun validator(regex: String): ValidationInfoBuilder.(JBTextField) -> ValidationInfo? {
            return {
                val value = it.text

                when {
                    value.isBlank() -> error("Can't be empty")
                    !regex.toRegex().matches(value) -> error("Only '$regex' are allowed.")
                    else -> null
                }
            }
        }

        init {
            init()
            this.title = if (option == null) "Add new pair" else "Edit pair"
        }

        override fun createCenterPanel(): JComponent? = panel {
            row("Name:") {
                textField({ myOptionName }, { myOptionName = it })
                        .withValidationOnApply(validator("([a-zA-Z0-9])+"))
                        .focused()
            }
            row("Value:") {
                textField({ myOptionValue }, { myOptionValue = it })
                        .withValidationOnApply(validator("([a-zA-Z0-9_.,{}=:/-])+"))
            }
        }

        fun getValue() = Pair(myOptionName, myOptionValue)
    }
}