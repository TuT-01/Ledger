$ErrorActionPreference = 'Stop'

$target = 'E:\Python\Bking_Harmony'

function Write-Utf8NoBom {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$Content
    )
    $encoding = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($Path, $Content, $encoding)
}

if (-not (Test-Path $target)) {
    throw "Project directory does not exist: $target"
}

Write-Utf8NoBom -Path (Join-Path $target 'entry\src\main\resources\base\profile\main_pages.json') -Content @'
{
  "src": [
    "pages/Index"
  ]
}
'@

Write-Utf8NoBom -Path (Join-Path $target 'oh-package.json5') -Content @'
{
  "modelVersion": "5.0.0",
  "name": "bking_harmony",
  "version": "0.1.0",
  "description": "Bking personal bookkeeping app for HarmonyOS ArkTS.",
  "dependencies": {
  },
  "devDependencies": {
  }
}
'@

Write-Utf8NoBom -Path (Join-Path $target 'entry\oh-package.json5') -Content @'
{
  "modelVersion": "5.0.0",
  "name": "entry",
  "version": "0.1.0",
  "description": "Entry module for Bking HarmonyOS app.",
  "dependencies": {
  },
  "devDependencies": {
  }
}
'@

Write-Utf8NoBom -Path (Join-Path $target 'hvigor\hvigor-config.json5') -Content @'
{
  "modelVersion": "5.0.0",
  "dependencies": {
  },
  "execution": {
  },
  "logging": {
  },
  "debugging": {
  },
  "nodeOptions": {
  }
}
'@

Write-Utf8NoBom -Path (Join-Path $target 'build-profile.json5') -Content @'
{
  "app": {
    "signingConfigs": [],
    "products": [
      {
        "name": "default",
        "signingConfig": "default",
        "compatibleSdkVersion": "5.0.0(12)",
        "targetSdkVersion": "5.0.0(12)",
        "runtimeOS": "HarmonyOS"
      }
    ],
    "buildModeSet": [
      {
        "name": "debug"
      },
      {
        "name": "release"
      }
    ]
  },
  "modules": [
    {
      "name": "entry",
      "srcPath": "./entry",
      "targets": [
        {
          "name": "default",
          "applyToProducts": [
            "default"
          ]
        }
      ]
    }
  ]
}
'@

Write-Utf8NoBom -Path (Join-Path $target 'entry\build-profile.json5') -Content @'
{
  "apiType": "stageMode",
  "buildOption": {
  },
  "buildOptionSet": [
    {
      "name": "release",
      "arkOptions": {
        "obfuscation": {
          "ruleOptions": {
            "enable": false,
            "files": [
              "./obfuscation-rules.txt"
            ]
          }
        }
      }
    }
  ],
  "targets": [
    {
      "name": "default"
    }
  ]
}
'@

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

Write-Host 'Fixed Harmony project JSON encoding and cleared build caches.'
Write-Host 'Now build again in DevEco Studio: Build > Compile Hap(s)/APP(s) > Compile Hap(s).'
