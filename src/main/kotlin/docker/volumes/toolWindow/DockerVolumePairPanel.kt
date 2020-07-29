package docker.volumes.toolWindow

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.AddEditRemovePanel
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.ui.layout.panel
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JTextField


class DockerVolumePairPanel(title: String, private val possibleNames: List<String>) : AddEditRemovePanel<Pair<String, String>>(
        MyPairTableModel(), mutableListOf(), title
) {
    override fun removeItem(option: Pair<String, String>): Boolean = true

    override fun editItem(option: Pair<String, String>): Pair<String, String>? = doAddOrEdit(option)

    override fun addItem(): Pair<String, String>? = doAddOrEdit(null)

    private fun doAddOrEdit(value: Pair<String, String>?): Pair<String, String>? {
        val optionDialog = MyAddOrEditPairDialog(value, possibleNames)
        return if (!optionDialog.showAndGet()) null else optionDialog.getValue().takeIf {
            it.first.isNotBlank() && it.second.isNotBlank()
        }
    }

    private class MyPairTableModel : TableModel<Pair<String, String>>() {
        override fun getColumnCount(): Int = 2

        override fun getColumnName(column: Int): String = if (column == 0) "Name" else "Value"

        override fun getField(o: Pair<String, String>, c: Int): String = if (c == 0) o.first else o.second
    }

    private class MyAddOrEditPairDialog(option: Pair<String, String>?, val possibleNames: List<String>) : DialogWrapper(false) {
        private var myName: String = option?.first.orEmpty()
        private var myValue: String = option?.second.orEmpty()

        init {
            init()
            this.title = if (option == null) "Add new pair" else "Edit pair"
        }

        private fun <T : JComponent> validator(regex: String): ValidationInfoBuilder.(T) -> ValidationInfo? {
            return {
                val value = when (it) {
                    is JTextField -> it.text
                    is JComboBox<*> -> it.editor.item.toString()
                    else -> throw UnsupportedOperationException("Unknown JComponent")
                }

                when {
                    value.isBlank() -> error("Can't be empty")
                    !regex.toRegex().matches(value) -> error("Only '$regex' are allowed.")
                    else -> null
                }
            }
        }

        override fun createCenterPanel(): JComponent? = panel {
            val comboBox = JComboBox(DefaultComboBoxModel(possibleNames.toTypedArray())).apply {
                isEditable = true
                addItemListener { myName = it.item.toString() }
            }

            row("Name:") {
                comboBox(growX, growY, pushY).withValidationOnApply(validator("([a-zA-Z0-9])+")).focused()
            }
            row("Value:") {
                textField({ myValue }, {
                    myValue = it
                }).withValidationOnApply(validator("([a-zA-Z0-9_.,{}=:/-])+"))
            }
        }

        fun getValue() = Pair(myName, myValue)
    }
}