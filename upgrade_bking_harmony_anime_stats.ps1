$ErrorActionPreference = 'Stop'

$target = 'E:\Python\Bking_Harmony'
$indexPath = Join-Path $target 'entry\src\main\ets\pages\Index.ets'

function Write-Utf8NoBom {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$Content
    )
    $encoding = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($Path, $Content, $encoding)
}

if (-not (Test-Path $indexPath)) {
    throw "Index.ets does not exist: $indexPath"
}

$text = [System.IO.File]::ReadAllText($indexPath, [System.Text.Encoding]::UTF8)

$text = $text.Replace("const CATEGORY_COLORS: string[] = ['#007D7E', '#6A7D5A', '#C0392B', '#8E6AD8', '#D89A2B'];", @'
const CATEGORY_COLORS: string[] = ['#00A6A6', '#8E6AD8', '#FF6B6B', '#FFE07A', '#1D3557'];
const COLOR_BG: string = '#FFF8F2';
const COLOR_CARD: string = '#FFFFFF';
const COLOR_INK: string = '#202124';
const COLOR_MUTED: string = '#687076';
const COLOR_PRIMARY: string = '#00A6A6';
const COLOR_ACCENT: string = '#FFE07A';
const COLOR_CHIP: string = '#F0E7FF';
const COLOR_CHIP_SELECTED: string = '#BDF3EE';
const TEXT_TODAY_PANEL: string = '\u2726 \u4eca\u65e5\u5c0f\u8d26\u672c \u2726';
'@)

$text = [regex]::Replace($text, "(?s)  private periodStart\(\): number \{.*?\n  \}\n\n  private periodEnd\(\): number \{", @'
  private periodStart(): number {
    const raw = this.statStartInput.trim();
    const date = new Date(raw);
    const time = date.getTime();
    if (Number.isNaN(time)) {
      return 0;
    }
    return time;
  }

  private periodEnd(): number {
'@)

$text = [regex]::Replace($text, "(?s)  private startPlaceholder\(\): string \{.*?\n  \}\n\n  build\(\) \{", @'
  private startPlaceholder(): string {
    return 'YYYY-MM-DD';
  }

  private panelValues(items: CategoryTotal[]): number[] {
    const values: number[] = [];
    items.forEach((item: CategoryTotal) => {
      values.push(item.amount);
    });
    return values;
  }

  build() {
'@)

$text = $text.Replace(".backgroundColor('#F8FAF8')", ".backgroundColor(COLOR_BG)")
$text = $text.Replace(".margin({ top: 14, bottom: 6 })", ".margin({ top: 8, bottom: 4 })")
$text = $text.Replace(".padding({ left: 18, right: 18, bottom: 12 })", ".padding({ left: 16, right: 16, bottom: 10 })")
$text = $text.Replace(".backgroundColor('#007D7E')", ".backgroundColor(COLOR_PRIMARY)")
$text = $text.Replace(".backgroundColor(Color.White)", ".backgroundColor(COLOR_CARD)")
$text = $text.Replace(".fontColor('#6F7478')", ".fontColor(COLOR_MUTED)")
$text = $text.Replace(".fontColor('#4D5358')", ".fontColor(COLOR_MUTED)")
$text = $text.Replace(".backgroundColor(this.category === item ? '#D7F0EF' : '#ECE8F2')", ".backgroundColor(this.category === item ? COLOR_CHIP_SELECTED : COLOR_CHIP)")
$text = $text.Replace(".backgroundColor(this.selectedTab === index ? '#D7F0EF' : '#FFFFFF')", ".backgroundColor(this.selectedTab === index ? COLOR_ACCENT : COLOR_CARD)")

$text = $text.Replace("        Button(this.statMode === 'week' ? TEXT_WEEK + ' \u2713' : TEXT_WEEK)", "        Button(this.statMode === 'week' ? TEXT_WEEK + ' \u2713' : TEXT_WEEK)")
$text = $text.Replace("            this.statStartInput = '2026-05';", "            this.statStartInput = '2026-05-13';")
$text = $text.Replace("            this.statStartInput = '2026';", "            this.statStartInput = '2026-05-13';")

$text = $text.Replace("      Column() {`n        Text(TEXT_BALANCE)", "      Text(TEXT_TODAY_PANEL)`n        .fontSize(14)`n        .fontColor(COLOR_MUTED)`n        .margin({ bottom: 8 })`n      Column() {`n        Text(TEXT_BALANCE)")

$text = $text.Replace("      .padding(20)`n      .borderRadius(10)", "      .padding(20)`n      .border({ width: 2, color: '#1D3557' })`n      .borderRadius(10)")

$text = [regex]::Replace($text, "(?s)  @Builder\s+StructureView\(titleText: string, items: CategoryTotal\[\], totalAmount: number\) \{.*?\n  @Builder\s+ProfileView\(\) \{", @'
  @Builder
  StructureView(titleText: string, items: CategoryTotal[], totalAmount: number) {
    Column() {
      Text(titleText)
        .fontSize(18)
        .fontWeight(FontWeight.Bold)
      if (items.length === 0 || totalAmount <= 0) {
        Text(TEXT_NO_DATA)
          .fontColor(COLOR_MUTED)
          .margin({ top: 8 })
      } else {
        Row() {
          DataPanel({ values: this.panelValues(items), max: totalAmount })
            .width(118)
            .height(118)
            .layoutWeight(0)
          Column() {
            ForEach(items, (item: CategoryTotal) => {
              Row() {
                Text('\u25CF')
                  .fontColor(item.color)
                  .fontSize(15)
                  .margin({ right: 6 })
                Column() {
                  Text(item.label)
                    .fontSize(14)
                    .fontWeight(FontWeight.Medium)
                  Text(this.money(item.amount) + ' \u00B7 ' + Math.round(item.amount / totalAmount * 100).toString() + '%')
                    .fontSize(12)
                    .fontColor(COLOR_MUTED)
                }
                .layoutWeight(1)
              }
              .margin({ bottom: 8 })
            }, (item: CategoryTotal) => item.label)
          }
          .layoutWeight(1)
          .margin({ left: 14 })
        }
        .margin({ top: 12 })
      }
    }
    .width('100%')
    .padding(16)
    .margin({ top: 12 })
    .border({ width: 1, color: '#E8DFF5' })
    .borderRadius(12)
    .backgroundColor(COLOR_CARD)
  }

  @Builder
  ProfileView() {
'@)

$text = $text.Replace(".borderRadius(8)`n    .backgroundColor(COLOR_CARD)", ".border({ width: 1, color: '#E8DFF5' })`n    .borderRadius(12)`n    .backgroundColor(COLOR_CARD)")
$text = $text.Replace(".padding({ top: 8, bottom: 8, left: 10, right: 10 })", ".padding({ top: 8, bottom: 8, left: 10, right: 10 })")

Write-Utf8NoBom -Path $indexPath -Content $text

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

Write-Host 'Upgraded Harmony anime-style UI and stats pie charts.'
