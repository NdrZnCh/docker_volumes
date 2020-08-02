package docker.volumes.ui.components

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.AddEditRemovePanel
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.ui.layout.panel
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JTextField
import utils.filterIf
import utils.firstOr
import utils.minusIfNotNull

class DockerVolumePairPanel(
    title: String,
    val possibleValues: Map<String, Array<String>> = mapOf()
) : AddEditRemovePanel<Pair<String, String>>(MyPairTableModel(), mutableListOf(), title) {

    init {
        this.table.setShowColumns(true)
    }

    override fun removeItem(pair: Pair<String, String>): Boolean = true

    override fun editItem(pair: Pair<String, String>): Pair<String, String>? = doAddOrEdit(pair)

    override fun addItem(): Pair<String, String>? = doAddOrEdit(null)

    private fun doAddOrEdit(pair: Pair<String, String>?): Pair<String, String>? {
        val dataKeys = this.data.toMap().keys.minusIfNotNull(pair?.first)
        val optionDialog = MyAddOrEditPairDialog(pair, dataKeys, possibleValues.filterIf(pair == null) {
            !dataKeys.contains(it.key)
        })

        return if (!optionDialog.showAndGet()) null else optionDialog.getValue().takeIf {
            it.first.isNotBlank() && it.second.isNotBlank()
        }
    }

    private class MyAddOrEditPairDialog(
        option: Pair<String, String>?,
        val alreadyDefinedKeys: Set<String>,
        val possibleValues: Map<String, Array<String>>
    ) : DialogWrapper(false) {
        private var myName: String = option?.first ?: possibleValues.keys.firstOr { "" }
        private var myValue: String = option?.second ?: possibleValues[myName].firstOr { "" }

        init {
            init()
            this.title = if (option == null) "Add new pair" else "Edit pair"
        }

        override fun createCenterPanel(): JComponent? {
            val valueComponent = ComboBox(DefaultComboBoxModel(possibleValues.values.firstOr { emptyArray() })).apply {
                isEditable = true
                if (myValue.isNotBlank()) editor.item = myValue
                addItemListener { myValue = it.item.toString() }
            }

            val nameComponent = ComboBox(DefaultComboBoxModel(possibleValues.keys.toTypedArray())).apply {
                isEditable = true
                if (myName.isNotBlank()) editor.item = myName
                addItemListener {
                    myName = it.item.toString()
                    if (it.stateChange == 1) valueComponent.setNewArray(possibleValues[myName])
                }
            }

            return panel {
                row("Name:") {
                    nameComponent(growX, growY, pushY).apply {
                        withValidationOnApply(validator("([a-zA-Z0-9])+"))
                        withErrorOnApplyIf("Key already defined. All keys must be unique.") {
                            alreadyDefinedKeys.contains(it.editor.item)
                        }
                    }.focused()
                }
                row("Value:") {
                    valueComponent(growX, growY, pushY).withValidationOnApply(validator("([a-zA-Z0-9_.,{}=:/-])+"))
                }
            }
        }

        fun getValue() = Pair(myName, myValue)

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
    }

    private class MyPairTableModel : AddEditRemovePanel.TableModel<Pair<String, String>>() {

        override fun getColumnCount(): Int = 2

        override fun getColumnName(column: Int): String = if (column == 0) "Name" else "Value"

        override fun getField(pair: Pair<String, String>, c: Int): String = if (c == 0) pair.first else pair.second
    }
}

private fun <T> JComboBox<T>.setNewArray(array: Array<T>?) {
    if (array == null) return

    with((this.model as DefaultComboBoxModel)) {
        removeAllElements()
        addAll(array.toList())
    }

    this.editor.item = array.firstOrNull()
}
