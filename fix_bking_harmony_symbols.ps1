$ErrorActionPreference = 'Stop'

$target = 'E:\Python\Bking_Harmony'
$indexPath = Join-Path $target 'entry\src\main\ets\pages\Index.ets'

if (-not (Test-Path $indexPath)) {
    throw "Index.ets does not exist: $indexPath"
}

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$text = [System.IO.File]::ReadAllText($indexPath, [System.Text.Encoding]::UTF8)
$lines = $text -split "`r?`n"

for ($i = 0; $i -lt $lines.Length; $i++) {
    if ($lines[$i] -like "*Button(this.statMode === 'week'*") {
        $lines[$i] = "        Button(this.statMode === 'week' ? TEXT_WEEK + ' \u2713' : TEXT_WEEK)"
    } elseif ($lines[$i] -like "*Button(this.statMode === 'month'*") {
        $lines[$i] = "        Button(this.statMode === 'month' ? TEXT_MONTH + ' \u2713' : TEXT_MONTH)"
    } elseif ($lines[$i] -like "*Button(this.statMode === 'year'*") {
        $lines[$i] = "        Button(this.statMode === 'year' ? TEXT_YEAR + ' \u2713' : TEXT_YEAR)"
    } elseif ($lines[$i] -like "*this.money(item.amount)*Math.round*") {
        $lines[$i] = "              Text(this.money(item.amount) + ' \u00B7 ' + Math.round(item.amount / totalAmount * 100).toString() + '%')"
    } elseif ($lines[$i].TrimStart().StartsWith("Text('") -and $i + 1 -lt $lines.Length -and $lines[$i + 1] -like "*fontColor(item.color)*") {
        $lines[$i] = "            Text('\u25A0')"
    }
}

[System.IO.File]::WriteAllText($indexPath, ($lines -join "`n"), $utf8NoBom)

$cachePaths = @(
    (Join-Path $target 'entry\build'),
    (Join-Path $target 'build'),
    (Join-Path $target '.hvigor\outputs')
)

foreach ($path in $cachePaths) {
    if (Test-Path $path) {
        Remove-Item -Recurse -Force $path
    }
}

Write-Host 'Fixed ArkTS symbol string escapes and cleared build caches.'
