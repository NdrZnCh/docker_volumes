package docker.volumes.ui.toolWindow

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import docker.communicator.Failure
import docker.communicator.Success
import docker.communicator.docker
import docker.volumes.notifyAboutError

class DockerVolumesToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val dockerVolumes = DockerVolumesToolWindow()
        val contentManager = toolWindow.contentManager
        val content: Content = contentManager.factory.createContent(dockerVolumes, null, false)

        toolWindow.contentManager.addContent(content)
    }

    override fun isApplicable(project: Project): Boolean = when (val result = docker("-v")) {
        is Success -> true
        is Failure -> {
            notifyAboutError(result.reason, project)
            false
        }
    }
}
