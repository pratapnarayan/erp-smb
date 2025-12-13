# Migrate all service schemas using Flyway Maven Plugin
# Usage:
#   pwsh ./migrate_all.ps1 -DbUrl "jdbc:postgresql://localhost:5432/erp" -DbUser "postgres" -DbPassword "postgres"
param(
  [string]$DbUrl = "jdbc:postgresql://localhost:5432/erp",
  [string]$DbUser = "postgres",
  [string]$DbPassword = "postgres",
  [string]$FlywayVersion = "10.22.0"
)

$ErrorActionPreference = "Stop"

function Invoke-Migration($modulePath, $schema) {
  Write-Host "==> Migrating $modulePath (schema: $schema)" -ForegroundColor Cyan
  mvn --% -q -f $modulePath `
    org.flywaydb:flyway-maven-plugin:$FlywayVersion:migrate `
    -Dflyway.url=$DbUrl `
    -Dflyway.user=$DbUser `
    -Dflyway.password=$DbPassword `
    -Dflyway.defaultSchema=$schema `
    -Dflyway.schemas=$schema `
    -Dflyway.locations=filesystem:${project.basedir}/src/main/resources/db/migration
}

# Map of modules to their schemas
$services = @(
  @{ module = "auth-service";     schema = "auth" },
  @{ module = "user-service";     schema = "users" },
  @{ module = "product-service";  schema = "products" },
  @{ module = "order-service";    schema = "orders" },
  @{ module = "sales-service";    schema = "sales" },
  @{ module = "finance-service";  schema = "finance" },
  @{ module = "hrms-service";     schema = "hrms" },
  @{ module = "enquiry-service";  schema = "enquiry" }
)

foreach ($svc in $services) {
  $modulePom = Join-Path $PSScriptRoot ("{0}/pom.xml" -f $svc.module)
  if (-Not (Test-Path $modulePom)) {
    Write-Warning "Skipping $($svc.module) - pom.xml not found"
    continue
  }
  try {
    Invoke-Migration -modulePath $modulePom -schema $svc.schema
  } catch {
    Write-Error "Migration failed for $($svc.module): $_"
    exit 1
  }
}

Write-Host "All migrations completed successfully." -ForegroundColor Green
