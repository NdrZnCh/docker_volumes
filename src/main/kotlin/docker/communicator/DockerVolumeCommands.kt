package docker.communicator

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import docker.data.DockerVolume

private fun jsonToDockerVolume(json: String): DockerVolume? {
    return try {
        Gson().fromJson(json, DockerVolume::class.java)
    } catch (e: JsonSyntaxException) {
        e.printStackTrace()
        null
    }
}

private fun String.removeQuotes() = this.removeSurrounding("\"")

fun volumesList(): List<DockerVolume> {
    return when (val result = docker("volume", "ls", format = "json .Name") { volumeInspect(it.removeQuotes()) }) {
        is Success -> result.value.filterNotNull()
        is Failure -> emptyList()
    }
}

fun volumeInspect(name: String): DockerVolume? {
    return when (val result = docker("volume", "inspect", name) { jsonToDockerVolume(it) }) {
        is Success -> result.value.firstOrNull()
        is Failure -> null
    }
}

fun volumePrune(): Result<Unit> = docker("volume", "prune", "--force")

fun createVolume(name: String): Result<DockerVolume> {
    return when (val result = docker("volume", "create", name)) {
        is Success -> {
            val newVolume = volumeInspect(name)

            if (newVolume != null) {
                Success(newVolume)
            } else Failure("Error while getting information about docker volume")
        }
        is Failure -> Failure(result.reason)
    }
}

fun removeVolume(element: String): Result<Unit> = docker("volume", "rm", element, "--force")
