$ErrorActionPreference = 'Stop'

$target = 'E:\Python\Bking_Harmony'

if (-not (Test-Path $target)) {
    throw "Project directory does not exist: $target"
}

New-Item -ItemType Directory -Force -Path (Join-Path $target 'hvigor') | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $target 'AppScope\resources\base\element') | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $target 'AppScope\resources\base\media') | Out-Null

Set-Content -Encoding utf8 -Path (Join-Path $target 'hvigor\hvigor-config.json5') -Value @'
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

Set-Content -Encoding utf8 -Path (Join-Path $target 'oh-package.json5') -Value @'
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

Set-Content -Encoding utf8 -Path (Join-Path $target 'build-profile.json5') -Value @'
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

Set-Content -Encoding utf8 -Path (Join-Path $target 'entry\build-profile.json5') -Value @'
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

Set-Content -Encoding utf8 -Path (Join-Path $target 'hvigorfile.ts') -Value @'
import { appTasks } from '@ohos/hvigor-ohos-plugin';

export default {
  system: appTasks,
  plugins: []
};
'@

Set-Content -Encoding utf8 -Path (Join-Path $target 'entry\hvigorfile.ts') -Value @'
import { hapTasks } from '@ohos/hvigor-ohos-plugin';

export default {
  system: hapTasks,
  plugins: []
};
'@

Set-Content -Encoding utf8 -Path (Join-Path $target 'AppScope\resources\base\element\string.json') -Value @'
{
  "string": [
    {
      "name": "app_name",
      "value": "Bking"
    }
  ]
}
'@

Set-Content -Encoding utf8 -Path (Join-Path $target 'AppScope\resources\base\media\app_icon.svg') -Value @'
<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 64 64"><rect width="64" height="64" rx="14" fill="#007D7E"/><text x="32" y="41" text-anchor="middle" font-size="28" font-family="Arial" fill="#FFFFFF">B</text></svg>
'@

if (-not (Test-Path (Join-Path $target 'entry\obfuscation-rules.txt'))) {
    Set-Content -Encoding utf8 -Path (Join-Path $target 'entry\obfuscation-rules.txt') -Value ''
}

Write-Host "Bking Harmony project config repaired."
Write-Host "Now return to DevEco Studio and run: Build > Compile Hap(s)/APP(s) > Compile Hap(s)."
