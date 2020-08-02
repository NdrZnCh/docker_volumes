# Docker volumes integration

<!-- Plugin description -->
A plug-in for working with [Docker volumes](https://docs.docker.com/storage/volumes/).

Features:
* Tool window (`Docker volumes`)
    * Actions
        * Create volume
            * Dialog for configure and create a new volume
        * Remove one or more volumes
        * Refresh list
        * Remove all unused volumes (`docker volume prune`)
    * Right click actions
        * Inspect volume 
        * Dialog for generate mount command
    * Notification about docker errors
<!-- Plugin description end -->

## Installation
  
- Manually:

  Download the [latest release](https://github.com/NdrZnCh/docker_volumes/releases/latest) and install it manually using
  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>
  
  
## Important

If you use Mac OS X you should run IntelliJ IDEA from shell with command e.g. `open -a 'IntelliJ IDEA CE'`.
On Mac OS X, IDE doesn't pick up shell environment when started from Dock/Spotlight.

[IDEA-99154](https://youtrack.jetbrains.com/issue/IDEA-99154)

[Same problem](https://discuss.gradle.org/t/exec-execute-in-gradle-doesnt-use-path/25598)

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template