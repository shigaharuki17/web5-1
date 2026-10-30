$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $quizJava = if ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
        Join-Path $env:JAVA_HOME 'bin\java.exe'
    } elseif (Test-Path -LiteralPath 'C:\Program Files\Java\jdk-17\bin\java.exe') {
        'C:\Program Files\Java\jdk-17\bin\java.exe'
    } else {
        (Get-Command java -ErrorAction Stop).Source
    }
    if (-not (Test-Path -LiteralPath 'target\quiz-backend-1.0.0.jar')) {
        throw 'Build backend/pom.xml with Maven package first.'
    }
    & $quizJava -jar 'target\quiz-backend-1.0.0.jar'
} finally {
    Pop-Location
}
