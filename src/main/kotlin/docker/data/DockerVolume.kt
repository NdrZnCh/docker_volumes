package docker.data

@Suppress("ConstructorParameterNaming")
data class DockerVolume(
    val CreatedAt: String,
    val Driver: String,
    val Labels: Any?,
    val Mountpoint: String,
    val Name: String,
    val Options: Any?,
    val Scope: String
)
