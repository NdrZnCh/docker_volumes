package docker.volumes.toolWindow

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.ui.TableUtil
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.layout.LCFlags
import com.intellij.ui.layout.panel
import com.intellij.ui.table.JBTable
import docker.volumes.DockerVolumesBundle
import org.jetbrains.concurrency.runAsync
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer


class CreateVolumeDialogWrapper : DialogWrapper(true) {
    private var myVolumeName = ""
    private var myVolumeDriver = "local"
    private var myVolumeIsReadonly: Boolean = false

    private val myVolumeOptions: MutableList<Pair<String, String>> = mutableListOf()

    var applyAction: (String) -> Unit = {}

    init {
        this.init()
        this.title = DockerVolumesBundle.messagePointer("new.volume.dialog.title")
    }

    override fun createCenterPanel(): JComponent? {
        val tableModel = OptionsTableModel(myVolumeOptions)

        val optionsTable = JBTable(tableModel).apply {
            setDefaultRenderer(String::class.java, DefaultTableCellRenderer())
        }

        val optionsPanel = ToolbarDecorator.createDecorator(optionsTable).apply {
            setAddAction {
                myVolumeOptions.add(Pair("", ""))
                tableModel.fireTableRowsInserted(myVolumeOptions.size - 1, myVolumeOptions.size - 1)
            }
            setRemoveAction { TableUtil.removeSelectedItems(optionsTable) }
        }.createPanel().toLabeledComponent("Options:")

        return panel(LCFlags.fillX) {
            row("Name:") {
                textField({ myVolumeName }, { myVolumeName = it }).withValidationOnInput {
                    val value = it.text

                    when {
                        value.isBlank() -> null
                        value.length < 2 -> error("Name length can't be lower than 2.")
                        !"([a-zA-Z0-9])([a-zA-Z0-9_.-])+".toRegex().matches(value) -> {
                            error("Only \"[a-zA-Z0-9][a-zA-Z0-9_.-]\" are allowed.")
                        }
                        else -> null
                    }
                }.focused()
            }
            row("Driver:") {
                textField({ myVolumeDriver }, { myVolumeDriver = it })
            }
            row {
                checkBox("Readonly:", { myVolumeIsReadonly }, { myVolumeIsReadonly = it })
            }
            row { optionsPanel(growX, growY, pushY) }
        }
    }

    override fun doOKAction() {
        super.doOKAction()

        runAsync { applyAction(myVolumeName) }
    }

    private class OptionsTableModel(var options: MutableList<Pair<String, String>>) : AbstractTableModel() {
        override fun getRowCount(): Int = options.size

        override fun getColumnCount(): Int = 2

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val option = options[rowIndex]

            return if (columnIndex == 0) option.first else option.second
        }

        override fun getColumnName(column: Int): String? {
            return if (column == 0) "Name" else "Value"
        }

        override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
            return true
        }
    }
}

private fun JPanel.toLabeledComponent(title: String): JComponent {
    return LabeledComponent.create(this, title)
}
