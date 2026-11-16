param(
    [ValidateSet('Start', 'Stop')]
    [string]$Acao = 'Start',
    [string]$PgBin = 'C:/Program Files/PostgreSQL/17/bin',
    [string]$Diretorio = (Join-Path $env:LOCALAPPDATA 'EstudioBDD/postgresql'),
    [ValidateRange(1024, 65535)]
    [int]$Porta = 55432
)

$ErrorActionPreference = 'Stop'
$pgCtl = Join-Path $PgBin 'pg_ctl.exe'
if (-not (Test-Path -LiteralPath $pgCtl -PathType Leaf)) {
    throw 'Informe o caminho da pasta bin do PostgreSQL com -PgBin.'
}

$dados = Join-Path $Diretorio 'data'
$arquivoSenha = Join-Path $Diretorio 'senha-local.txt'
$arquivoLog = Join-Path $Diretorio 'postgresql.log'
$inicializado = Test-Path -LiteralPath (Join-Path $dados 'PG_VERSION')

if ($Acao -eq 'Stop') {
    if ($inicializado) {
        & $pgCtl status -D $dados *> $null
        if ($LASTEXITCODE -eq 0) {
            & $pgCtl stop -D $dados -m fast -w
            if ($LASTEXITCODE -ne 0) { throw 'Falha ao parar o PostgreSQL local.' }
        }
    }
    return
}

$usuario = 'estudio_bdd'
$banco = 'estudio_bdd'
if (-not $inicializado) {
    New-Item -ItemType Directory -Path $Diretorio -Force | Out-Null
    if (-not (Test-Path -LiteralPath $arquivoSenha)) {
        $senhaGerada = [Guid]::NewGuid().ToString('N') + [Guid]::NewGuid().ToString('N')
        Set-Content -LiteralPath $arquivoSenha -Value $senhaGerada -Encoding ascii
    }

    & (Join-Path $PgBin 'initdb.exe') -D $dados -U $usuario --encoding=UTF8 --locale=C --auth=scram-sha-256 "--pwfile=$arquivoSenha"
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao inicializar o PostgreSQL local.' }
}

$senha = (Get-Content -LiteralPath $arquivoSenha -Raw).Trim()
$pidFile = Join-Path $dados 'postmaster.pid'
& $pgCtl status -D $dados *> $null
if ($LASTEXITCODE -ne 0) {
    & $pgCtl start -D $dados -l $arquivoLog -o "-p $Porta -h 127.0.0.1" -w
    if ($LASTEXITCODE -ne 0) { throw "Falha ao iniciar o PostgreSQL. Consulte $arquivoLog." }
} elseif ((Get-Content -LiteralPath $pidFile -TotalCount 4)[3] -ne [string]$Porta) {
    throw 'O PostgreSQL local já está rodando em outra porta. Use a mesma porta ou pare essa instância.'
}

$senhaAnterior = $env:PGPASSWORD
try {
    $env:PGPASSWORD = $senha
    $conexao = @('-h', '127.0.0.1', '-p', $Porta, '-U', $usuario, '-w')
    $existe = & (Join-Path $PgBin 'psql.exe') @conexao -d postgres -X -At -c "SELECT 1 FROM pg_database WHERE datname = 'estudio_bdd'"
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao conectar ao PostgreSQL local.' }
    if ($existe -ne '1') {
        & (Join-Path $PgBin 'createdb.exe') @conexao $banco
        if ($LASTEXITCODE -ne 0) { throw 'Falha ao criar o banco estudio_bdd.' }
    }
} finally {
    $env:PGPASSWORD = $senhaAnterior
}

$env:DB_URL = "jdbc:postgresql://127.0.0.1:${Porta}/$banco"
$env:DB_USER = $usuario
$env:DB_PASSWORD = $senha
Write-Host "Banco estudio_bdd disponível na porta $Porta. Inicie a API neste mesmo terminal."
