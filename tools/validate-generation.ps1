param(
    [string]$GeneratedSrcDir = "src/generated/java"
)

Write-Host "Checking spec files..."
if (!(Test-Path -Path "spec/domain.yaml")) { Write-Error "spec/domain.yaml missing"; exit 2 }
if (!(Test-Path -Path "spec/api.yaml")) { Write-Error "spec/api.yaml missing"; exit 2 }

Write-Host "(Optional) Run code generation here using your preferred tool."

Write-Host "Running Gradle build and tests..."
./gradlew.bat clean test
if ($LASTEXITCODE -ne 0) { Write-Error "Gradle build/tests failed"; exit $LASTEXITCODE }

Write-Host "Validation completed successfully."
