$ErrorActionPreference = 'Stop'

$source = 'E:\Python\Bking'
$target = 'E:\Python\Bking_Harmony'
$androidBackup = Join-Path $target 'android-source'

New-Item -ItemType Directory -Force -Path $target | Out-Null
New-Item -ItemType Directory -Force -Path $androidBackup | Out-Null

robocopy $source $androidBackup /E /XD .git .gradle build app\build /XF local.properties create_bking_harmony.ps1 | Out-Host
if ($LASTEXITCODE -gt 7) {
    throw "robocopy failed with exit code $LASTEXITCODE"
}

$dirs = @(
    'hvigor',
    'AppScope',
    'entry',
    'entry\src\main\ets\entryability',
    'entry\src\main\ets\pages',
    'entry\src\main\resources\base\element',
    'entry\src\main\resources\base\media',
    'entry\src\main\resources\base\profile'
)

foreach ($dir in $dirs) {
    New-Item -ItemType Directory -Force -Path (Join-Path $target $dir) | Out-Null
}

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
  "name": "bking_harmony",
  "version": "0.1.0",
  "description": "Bking personal bookkeeping app for HarmonyOS ArkTS.",
  "dependencies": {},
  "devDependencies": {
    "@ohos/hypium": "1.0.21",
    "@ohos/hamock": "1.0.0"
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
        "runtimeOS": "HarmonyOS"
      }
    ],
    "buildModeSet": [
      { "name": "debug" },
      { "name": "release" }
    ]
  },
  "modules": [
    {
      "name": "entry",
      "srcPath": "./entry",
      "targets": [
        { "name": "default", "applyToProducts": ["default"] }
      ]
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

Set-Content -Encoding utf8 -Path (Join-Path $target 'AppScope\app.json5') -Value @'
{
  "app": {
    "bundleName": "com.bking.harmony",
    "vendor": "bking",
    "versionCode": 1,
    "versionName": "0.1.0",
    "icon": "$media:app_icon",
    "label": "$string:app_name"
  }
}
'@

Set-Content -Encoding utf8 -Path (Join-Path $target 'entry\oh-package.json5') -Value @'
{
  "name": "entry",
  "version": "0.1.0",
  "description": "Entry module for Bking HarmonyOS app.",
  "dependencies": {}
}
'@

Set-Content -Encoding utf8 -Path (Join-Path $target 'entry\build-profile.json5') -Value @'
{
  "apiType": "stageMode",
  "buildOption": {},
  "targets": [
    { "name": "default" }
  ]
}
'@

Set-Content -Encoding utf8 -Path (Join-Path $target 'entry\hvigorfile.ts') -Value @'
import { hapTasks } from '@ohos/hvigor-ohos-plugin';

export default {
  system: hapTasks,
  plugins: []
};
'@

Set-Content -Encoding utf8 -Path (Join-Path $target 'entry\src\main\module.json5') -Value @'
{
  "module": {
    "name": "entry",
    "type": "entry",
    "description": "$string:entry_desc",
    "mainElement": "EntryAbility",
    "deviceTypes": ["phone"],
    "deliveryWithInstall": true,
    "installationFree": false,
    "pages": "$profile:main_pages",
    "abilities": [
      {
        "name": "EntryAbility",
        "srcEntry": "./ets/entryability/EntryAbility.ets",
        "description": "$string:entry_desc",
        "icon": "$media:app_icon",
        "label": "$string:app_name",
        "startWindowIcon": "$media:app_icon",
        "startWindowBackground": "$color:start_window_background",
        "exported": true,
        "skills": [
          { "entities": ["entity.system.home"], "actions": ["action.system.home"] }
        ]
      }
    ]
  }
}
'@

Set-Content -Encoding utf8 -Path (Join-Path $target 'entry\src\main\resources\base\profile\main_pages.json') -Value @'
{
  "src": [
    "pages/Index"
  ]
}
'@

Set-Content -Encoding utf8 -Path (Join-Path $target 'entry\src\main\resources\base\element\string.json') -Value @'
{
  "string": [
    { "name": "app_name", "value": "Bking" },
    { "name": "entry_desc", "value": "Personal bookkeeping" }
  ]
}
'@

Set-Content -Encoding utf8 -Path (Join-Path $target 'entry\src\main\resources\base\element\color.json') -Value @'
{
  "color": [
    { "name": "start_window_background", "value": "#F8FAF8" }
  ]
}
'@

Set-Content -Encoding utf8 -Path (Join-Path $target 'entry\src\main\resources\base\media\app_icon.svg') -Value @'
<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 64 64"><rect width="64" height="64" rx="14" fill="#007D7E"/><text x="32" y="41" text-anchor="middle" font-size="28" font-family="Arial" fill="#FFFFFF">B</text></svg>
'@

Set-Content -Encoding utf8 -Path (Join-Path $target 'entry\src\main\ets\entryability\EntryAbility.ets') -Value @'
import AbilityConstant from '@ohos.app.ability.AbilityConstant';
import UIAbility from '@ohos.app.ability.UIAbility';
import Want from '@ohos.app.ability.Want';
import window from '@ohos.window';

export default class EntryAbility extends UIAbility {
  onCreate(want: Want, launchParam: AbilityConstant.LaunchParam): void {}

  onWindowStageCreate(windowStage: window.WindowStage): void {
    windowStage.loadContent('pages/Index');
  }

  onDestroy(): void {}
}
'@

Set-Content -Encoding utf8 -Path (Join-Path $target 'entry\src\main\ets\pages\Index.ets') -Value @'
interface LedgerRecord {
  id: string;
  type: string;
  category: string;
  amount: number;
  note: string;
  occurredAt: number;
}

interface CategoryTotal {
  label: string;
  amount: number;
  color: string;
}

const INCOME_CATEGORIES: string[] = ['工资收入', '外部收入'];
const EXPENSE_CATEGORIES: string[] = ['餐食', '购物', '运动'];
const CATEGORY_COLORS: string[] = ['#007D7E', '#6A7D5A', '#C0392B', '#8E6AD8', '#D89A2B'];

@Entry
@Component
struct Index {
  @State private email: string = '';
  @State private name: string = '';
  @State private emailInput: string = '';
  @State private nameInput: string = '';
  @State private selectedTab: number = 0;
  @State private recordType: string = 'expense';
  @State private category: string = '餐食';
  @State private amountInput: string = '';
  @State private noteInput: string = '';
  @State private statMode: string = 'week';
  @State private records: LedgerRecord[] = [];

  private isRegistered(): boolean {
    return this.email.trim().length > 0;
  }

  private register(): void {
    if (!this.emailInput.includes('@')) {
      return;
    }
    this.email = this.emailInput.trim();
    this.name = this.nameInput.trim();
  }

  private saveRecord(): void {
    const amount = Number(this.amountInput);
    if (Number.isNaN(amount) || amount <= 0) {
      return;
    }
    const record: LedgerRecord = {
      id: `${Date.now()}`,
      type: this.recordType,
      category: this.category,
      amount: amount,
      note: this.noteInput,
      occurredAt: Date.now()
    };
    this.records = [record, ...this.records];
    this.amountInput = '';
    this.noteInput = '';
  }

  private total(type: string): number {
    return this.records.filter((item: LedgerRecord) => item.type === type)
      .reduce((sum: number, item: LedgerRecord) => sum + item.amount, 0);
  }

  private breakdown(type: string): CategoryTotal[] {
    const source = type === 'income' ? INCOME_CATEGORIES : EXPENSE_CATEGORIES;
    return source.map((label: string, index: number) => {
      const amount = this.records
        .filter((item: LedgerRecord) => item.type === type && item.category === label)
        .reduce((sum: number, item: LedgerRecord) => sum + item.amount, 0);
      return { label, amount, color: CATEGORY_COLORS[index % CATEGORY_COLORS.length] } as CategoryTotal;
    }).filter((item: CategoryTotal) => item.amount > 0);
  }

  private money(value: number): string {
    return `¥${value.toFixed(2)}`;
  }

  build() {
    Column() {
      if (!this.isRegistered()) {
        this.RegisterView();
      } else {
        this.AppView();
      }
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F8FAF8')
  }

  @Builder
  private RegisterView() {
    Column({ space: 16 }) {
      Text('注册 Bking')
        .fontSize(30)
        .fontWeight(FontWeight.Bold)
      Text('使用邮箱创建本机记账档案。')
        .fontSize(15)
        .fontColor('#5F666A')
      TextInput({ placeholder: '邮箱', text: this.emailInput })
        .onChange((value: string) => this.emailInput = value)
      TextInput({ placeholder: '昵称（可选）', text: this.nameInput })
        .onChange((value: string) => this.nameInput = value)
      Button('使用邮箱注册')
        .width('100%')
        .onClick(() => this.register())
    }
    .width('100%')
    .height('100%')
    .justifyContent(FlexAlign.Center)
    .padding(24)
  }

  @Builder
  private AppView() {
    Column() {
      Text(this.title())
        .fontSize(24)
        .fontWeight(FontWeight.Bold)
        .margin({ top: 20, bottom: 12 })
      Scroll() {
        Column({ space: 14 }) {
          if (this.selectedTab === 0) {
            this.HomeView();
          } else if (this.selectedTab === 1) {
            this.RecordsView();
          } else if (this.selectedTab === 2) {
            this.StatsView();
          } else {
            this.ProfileView();
          }
        }.padding({ left: 18, right: 18, bottom: 18 })
      }
      .layoutWeight(1)
      this.NavBar();
    }
    .height('100%')
  }

  private title(): string {
    if (this.selectedTab === 0) return '记账';
    if (this.selectedTab === 1) return '记录';
    if (this.selectedTab === 2) return '统计';
    return '我的';
  }

  @Builder
  private HomeView() {
    Column({ space: 14 }) {
      Column({ space: 10 }) {
        Text('当前余额').fontSize(15).fontColor(Color.White)
        Text(this.money(this.total('income') - this.total('expense')))
          .fontSize(34)
          .fontColor(Color.White)
          .fontWeight(FontWeight.Bold)
        Row() {
          Text(`收入 ${this.money(this.total('income'))}`).fontColor(Color.White).layoutWeight(1)
          Text(`支出 ${this.money(this.total('expense'))}`).fontColor(Color.White)
        }
      }
      .width('100%')
      .padding(22)
      .borderRadius(10)
      .backgroundColor('#007D7E')

      Row({ space: 10 }) {
        Button('支出').onClick(() => { this.recordType = 'expense'; this.category = '餐食'; })
        Button('收入').onClick(() => { this.recordType = 'income'; this.category = '工资收入'; })
      }

      this.CategoryPicker();
      TextInput({ placeholder: '金额', text: this.amountInput })
        .type(InputType.Number)
        .onChange((value: string) => this.amountInput = value)
      TextInput({ placeholder: '备注', text: this.noteInput })
        .onChange((value: string) => this.noteInput = value)
      Button('保存记录')
        .width('100%')
        .onClick(() => this.saveRecord())
    }
  }

  @Builder
  private CategoryPicker() {
    Flex({ wrap: FlexWrap.Wrap }) {
      ForEach(this.recordType === 'income' ? INCOME_CATEGORIES : EXPENSE_CATEGORIES, (item: string) => {
        Button(item)
          .margin({ right: 8, bottom: 8 })
          .backgroundColor(this.category === item ? '#D7F0EF' : '#ECE8F2')
          .fontColor('#202124')
          .onClick(() => this.category = item)
      })
    }
  }

  @Builder
  private RecordsView() {
    Column({ space: 10 }) {
      if (this.records.length === 0) {
        this.InfoCard('还没有记录', '回到首页添加收入或支出。');
      } else {
        ForEach(this.records, (item: LedgerRecord) => {
          this.InfoCard(item.type === 'income' ? '收入' : '支出', `${item.category} · ${this.money(item.amount)} · ${item.note || '无备注'}`)
        })
      }
    }
  }

  @Builder
  private StatsView() {
    Column({ space: 12 }) {
      Row({ space: 8 }) {
        Button(this.statMode === 'week' ? '周 ✓' : '周').onClick(() => this.statMode = 'week')
        Button(this.statMode === 'month' ? '月 ✓' : '月').onClick(() => this.statMode = 'month')
        Button(this.statMode === 'year' ? '年 ✓' : '年').onClick(() => this.statMode = 'year')
      }
      this.InfoCard('汇总', `收入 ${this.money(this.total('income'))}\n支出 ${this.money(this.total('expense'))}\n结余 ${this.money(this.total('income') - this.total('expense'))}`)
      this.StructureView('支出结构', this.breakdown('expense'), this.total('expense'));
      this.StructureView('收入结构', this.breakdown('income'), this.total('income'));
    }
  }

  @Builder
  private StructureView(title: string, items: CategoryTotal[], total: number) {
    Column({ space: 8 }) {
      Text(title).fontSize(18).fontWeight(FontWeight.Bold)
      if (items.length === 0 || total <= 0) {
        Text('暂无数据').fontColor('#6F7478')
      } else {
        ForEach(items, (item: CategoryTotal) => {
          Column({ space: 4 }) {
            Row() {
              Text(item.label).layoutWeight(1)
              Text(`${this.money(item.amount)} · ${Math.round(item.amount / total * 100)}%`)
            }
            Progress({ value: item.amount, total: total, type: ProgressType.Linear })
              .color(item.color)
              .height(8)
          }
        })
      }
    }
    .width('100%')
    .padding(16)
    .borderRadius(8)
    .backgroundColor(Color.White)
  }

  @Builder
  private ProfileView() {
    Column({ space: 12 }) {
      this.InfoCard('个人信息', `邮箱：${this.email}\n昵称：${this.name || '未填写'}`);
      this.InfoCard('分类', '收入：工资收入、外部收入\n支出：餐食、购物、运动');
      this.InfoCard('说明', '这是 HarmonyOS ArkTS 首版迁移。数据目前保存在页面状态中，下一步可接入 preferences 持久化。');
    }
  }

  @Builder
  private InfoCard(title: string, body: string) {
    Column({ space: 8 }) {
      Text(title).fontSize(18).fontWeight(FontWeight.Bold)
      Text(body).fontSize(14).fontColor('#4D5358')
    }
    .width('100%')
    .padding(16)
    .borderRadius(8)
    .backgroundColor(Color.White)
  }

  @Builder
  private NavBar() {
    Row() {
      this.NavItem(0, '首页');
      this.NavItem(1, '记录');
      this.NavItem(2, '统计');
      this.NavItem(3, '我的');
    }
    .width('100%')
    .padding({ top: 8, bottom: 8 })
    .backgroundColor(Color.White)
  }

  @Builder
  private NavItem(index: number, label: string) {
    Button(label)
      .layoutWeight(1)
      .backgroundColor(this.selectedTab === index ? '#D7F0EF' : '#FFFFFF')
      .fontColor('#202124')
      .onClick(() => this.selectedTab = index)
  }
}
'@

Set-Content -Encoding utf8 -Path (Join-Path $target 'README_HARMONY.md') -Value @'
# Bking HarmonyOS

This directory is a HarmonyOS ArkTS/ArkUI migration scaffold for the Android Bking bookkeeping app.

## Structure

- `AppScope/app.json5`: app-level configuration.
- `entry/src/main/module.json5`: entry HAP module configuration.
- `entry/src/main/ets/entryability/EntryAbility.ets`: Stage model entry ability.
- `entry/src/main/ets/pages/Index.ets`: first-pass ArkUI implementation.
- `android-source/`: copied Android project for reference.

## Implemented in the first pass

- Email registration screen.
- Bottom navigation: home, records, stats, profile.
- Manual income and expense records.
- Income categories: salary income and external income.
- Expense categories: meal, shopping, sport.
- Stats summary and income/expense structure display.

## Next steps in DevEco Studio

1. Open `E:\Python\Bking_Harmony` in DevEco Studio.
2. Let DevEco Studio sync/generate missing local SDK metadata if prompted.
3. Configure signing for a real device.
4. Run the `entry` module on a HarmonyOS device or emulator.

The first pass keeps records in page state. After DevEco confirms the project template version, wire `@ohos.data.preferences` or relational store for persistence.
'@

Write-Host "Bking Harmony project generated at $target"
