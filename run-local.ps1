$ErrorActionPreference = 'Stop'

# Keep Gradle's mutable cache out of the OneDrive-synchronised project directory.
# OneDrive can temporarily lock dependency JARs and cause AccessDeniedException during compilation.
$env:GRADLE_USER_HOME = Join-Path $env:LOCALAPPDATA 'Gradle'

& "$PSScriptRoot\gradlew.bat" bootRun --no-daemon --console=plain
exit $LASTEXITCODE
