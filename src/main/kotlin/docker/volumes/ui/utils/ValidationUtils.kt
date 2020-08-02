package docker.volumes.ui.utils

import com.intellij.ui.layout.ValidationInfoBuilder
import docker.communicator.Failure
import docker.communicator.Result
import docker.communicator.Success
import docker.volumes.DockerVolumesBundle

fun isNotEmpty(value: String): Result<String> = when {
    value.isBlank() -> {
        Failure(DockerVolumesBundle.messagePointer("docker.volume.pair.panel.errors.emptyValue"))
    }
    else -> Success(value)
}

fun checkRegex(value: String, regex: String): Result<String> = when {
    !regex.toRegex().matches(value) -> {
        Failure(DockerVolumesBundle.messagePointer("docker.volume.pair.panel.errors.regex", regex))
    }
    else -> Success(value)
}

fun ValidationInfoBuilder.toError(reason: String) = this.error(reason)
