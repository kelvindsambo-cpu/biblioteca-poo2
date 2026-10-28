# Prepara o MySQL local para a aplicação (Windows):
#   1. cria a base biblioteca_poo2 (biblioteca.sql) e, se for nova, insere os dados de exemplo (dados_teste.sql);
#   2. cria o utilizador biblioteca_app só com permissões sobre biblioteca_poo2;
#   3. guarda BIBLIOTECA_DB_URL, BIBLIOTECA_DB_USER e BIBLIOTECA_DB_PASSWORD no utilizador do Windows.
# As passwords são pedidas no terminal e não ficam escritas em nenhum ficheiro.
#
# Uso, na raiz do projecto:
#   powershell -ExecutionPolicy Bypass -File sql\preparar-mysql.ps1
$ErrorActionPreference = 'Stop'
$pastaSql = $PSScriptRoot -replace '\\', '/'

function Encontrar-Mysql {
    $noPath = Get-Command mysql.exe -ErrorAction SilentlyContinue
    if ($noPath) { return $noPath.Source }
    $instalado = Get-ChildItem "$env:ProgramFiles\MySQL\MySQL Server *\bin\mysql.exe" -ErrorAction SilentlyContinue |
        Sort-Object FullName -Descending | Select-Object -First 1
    if ($instalado) { return $instalado.FullName }
    throw 'Não encontrei o mysql.exe. Instala o MySQL Server 8 ou acrescenta a pasta bin dele ao PATH.'
}

function Ler-Password([string]$pergunta) {
    $sec = Read-Host $pergunta -AsSecureString
    $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($sec)
    try { [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr) }
}

function Executar-Sql([string]$utilizador, [string]$comandos) {
    & $mysql -u $utilizador --default-character-set=utf8mb4 -N -e $comandos
    if ($LASTEXITCODE -ne 0) { throw "O MySQL devolveu um erro (utilizador $utilizador)." }
}

try {
    $mysql = Encontrar-Mysql
    Write-Host "A usar $mysql"
    $env:MYSQL_PWD = Ler-Password 'Password do root do MySQL'

    $existia = (Executar-Sql 'root' "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = 'biblioteca_poo2';") -eq '1'
    Executar-Sql 'root' "source $pastaSql/biblioteca.sql"
    if ($existia) {
        Write-Host 'A base biblioteca_poo2 já existia: tabelas verificadas, dados de exemplo NÃO inseridos de novo.'
    } else {
        Executar-Sql 'root' "source $pastaSql/dados_teste.sql"
        Write-Host 'Base biblioteca_poo2 criada com os dados de exemplo.'
    }

    do {
        $app = Ler-Password 'Nova password para o utilizador biblioteca_app (sem aspas nem \)'
        $invalida = [string]::IsNullOrEmpty($app) -or $app.Contains("'") -or $app.Contains('"') -or $app.Contains('\')
        if ($invalida) { Write-Host 'Password vazia ou com aspas/\. Escolhe outra.' }
    } while ($invalida)

    # Só as permissões de que a aplicação precisa, e só sobre biblioteca_poo2 (doc 05)
    Executar-Sql 'root' ("CREATE USER IF NOT EXISTS 'biblioteca_app'@'localhost' IDENTIFIED BY '$app'; " +
        "ALTER USER 'biblioteca_app'@'localhost' IDENTIFIED BY '$app'; " +
        "GRANT SELECT, INSERT, UPDATE, DELETE ON biblioteca_poo2.* TO 'biblioteca_app'@'localhost';")

    $env:MYSQL_PWD = $app
    $livros = Executar-Sql 'biblioteca_app' 'SELECT COUNT(*) FROM biblioteca_poo2.livro;'
    Write-Host "Ligação como biblioteca_app OK ($livros livros na base)."

    [Environment]::SetEnvironmentVariable('BIBLIOTECA_DB_URL', 'jdbc:mysql://localhost:3306/biblioteca_poo2', 'User')
    [Environment]::SetEnvironmentVariable('BIBLIOTECA_DB_USER', 'biblioteca_app', 'User')
    [Environment]::SetEnvironmentVariable('BIBLIOTECA_DB_PASSWORD', $app, 'User')
    Write-Host 'Variáveis BIBLIOTECA_DB_* guardadas. Fecha e volta a abrir o terminal e o IDE para as usar.'
}
finally {
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
}
