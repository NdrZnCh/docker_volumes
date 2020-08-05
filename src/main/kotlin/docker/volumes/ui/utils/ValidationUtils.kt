package docker.volumes.ui.utils

import com.intellij.ui.layout.ValidationInfoBuilder
import docker.volumes.DockerVolumesBundle
import utils.Failure
import utils.Result
import utils.Success

fun isNotEmpty(value: String): Result<String> = when {
    value.isBlank() -> {
        Failure(DockerVolumesBundle.messagePointer("docker.volume.ui.errors.emptyValue"))
    }
    else -> Success(value)
}

fun checkRegex(value: String, regex: String): Result<String> = when {
    !regex.toRegex().matches(value) -> {
        Failure(DockerVolumesBundle.messagePointer("docker.volume.ui.errors.regex", regex))
    }
    else -> Success(value)
}

fun ValidationInfoBuilder.toError(reason: String) = this.error(reason)
