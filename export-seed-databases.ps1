# BaoVe_Microservice - export current PostgreSQL data as seed SQL
$Container = "microservice-postgres"
$OutputDir = ".\database\seed"

$Databases = @(
    "x_auth_db",
    "x_product_db",
    "x_inventory_db",
    "x_order_db",
    "x_payment_db"
)

$running = docker ps --format "{{.Names}}" | Select-String -SimpleMatch $Container
if (-not $running) {
    Write-Error "Container '$Container' is not running."
    exit 1
}

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

foreach ($Db in $Databases) {
    $File = Join-Path $OutputDir "$Db.sql"
    Write-Host "Exporting $Db -> $File"

    docker exec $Container pg_dump `
        -U postgres `
        -d $Db `
        --data-only `
        --column-inserts `
        --no-owner `
        --no-privileges `
        > $File

    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to export $Db"
        exit 1
    }
}

Write-Host "`nExport completed:"
Get-ChildItem $OutputDir | Select-Object Name, Length
