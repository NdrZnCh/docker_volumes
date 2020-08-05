package docker.volumes.ui.components

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.AddEditRemovePanel
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.ui.layout.panel
import docker.volumes.DockerVolumesBundle.messagePointer
import docker.volumes.ui.utils.checkRegex
import docker.volumes.ui.utils.isNotEmpty
import docker.volumes.ui.utils.toError
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JTextField
import utils.Failure
import utils.Result
import utils.Success
import utils.filterIf
import utils.firstOr
import utils.minusIfNotNull
import utils.otherwise
import utils.then

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
            this.title = if (option == null) {
                messagePointer("docker.volume.pair.panel.dialog.addNew.title")
            } else messagePointer("docker.volume.pair.panel.dialog.edit.title")
        }

        override fun createCenterPanel(): JComponent? {
            val valueComponent = createEditableCombobox(possibleValues.values.firstOr { emptyArray() }).apply {
                if (myValue.isNotBlank()) editor.item = myValue
                addItemListener { myValue = it.item.toString() }
            }

            val nameComponent = createEditableCombobox(possibleValues.keys.toTypedArray()).apply {
                if (myName.isNotBlank()) editor.item = myName

                addItemListener {
                    myName = it.item.toString()
                    if (it.stateChange == 1) valueComponent.setNewArray(possibleValues[myName])
                }
            }

            return panel {
                row(messagePointer("docker.volume.pair.panel.name.title")) {
                    nameComponent(growX, growY, pushY).apply {
                        withValidationOnApply {
                            isNotEmpty(textValue()) then {
                                checkRegex(it, "([a-zA-Z0-9])+")
                            } then ::assetThatIsUniqueName otherwise this::toError
                        }
                    }.focused()
                }
                row(messagePointer("docker.volume.pair.panel.value.title")) {
                    valueComponent(growX, growY, pushY).apply {
                        withValidationOnApply {
                            isNotEmpty(textValue()) then {
                                checkRegex(it, "([a-zA-Z0-9_.,{}=:/-])+")
                            } otherwise this::toError
                        }
                    }
                }
            }
        }

        fun getValue() = Pair(myName, myValue)

        private fun createEditableCombobox(array: Array<String>): ComboBox<String> {
            return ComboBox(DefaultComboBoxModel(array)).apply { isEditable = true }
        }

        private fun assetThatIsUniqueName(value: String): Result<String> = when {
            alreadyDefinedKeys.contains(value) -> {
                Failure(messagePointer("docker.volume.pair.panel.errors.keyAlreadyDefined"))
            }
            else -> Success(value)
        }

        private fun ValidationInfoBuilder.textValue(): String = when (val component: JComponent = this.component) {
            is JTextField -> component.text
            is JComboBox<*> -> component.editor.item.toString()
            else -> throw UnsupportedOperationException("Unknown JComponent")
        }
    }

    private class MyPairTableModel : AddEditRemovePanel.TableModel<Pair<String, String>>() {

        override fun getColumnCount(): Int = 2

        override fun getColumnName(column: Int): String = if (column == 0) {
            messagePointer("docker.volume.pair.panel.table.name.title")
        } else messagePointer("docker.volume.pair.panel.table.value.title")

        override fun getField(pair: Pair<String, String>, c: Int): String = if (c == 0) pair.first else pair.second
    }
}

private fun <T> JComboBox<T>.setNewArray(array: Array<T>?) {
    if (array == null) return

    with((this.model as DefaultComboBoxModel)) {
        removeAllElements()
        array.forEach { addElement(it) }
    }

    this.editor.item = array.firstOrNull()
}
