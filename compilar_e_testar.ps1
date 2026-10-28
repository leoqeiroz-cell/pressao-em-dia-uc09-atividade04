$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
New-Item -ItemType Directory -Force build/classes | Out-Null
$fontes = Get-ChildItem src -Recurse -Filter '*.java' | ForEach-Object FullName
& javac --release 17 -Xlint:all -Werror -encoding UTF-8 -d build/classes $fontes testes/TestesSistema.java testes/TestesInterface.java
if ($LASTEXITCODE -ne 0) { throw 'Falha na compilação.' }
& java '-Djava.awt.headless=true' -cp build/classes TestesSistema
if ($LASTEXITCODE -ne 0) { throw 'Falha nos testes de sistema.' }
& java -cp build/classes TestesInterface
if ($LASTEXITCODE -ne 0) { throw 'Falha nos testes de interface.' }
& jar --create --file PressaoEmDia.jar --main-class br.com.senac.pressao.Aplicacao -C build/classes br
if ($LASTEXITCODE -ne 0) { throw 'Falha ao criar o executável.' }
