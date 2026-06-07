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

Write-Utf8NoBom -Path (Join-Path $target 'entry\src\main\ets\entryability\EntryAbility.ets') -Content @'
import AbilityConstant from '@ohos.app.ability.AbilityConstant';
import UIAbility from '@ohos.app.ability.UIAbility';
import Want from '@ohos.app.ability.Want';
import window from '@ohos.window';

export default class EntryAbility extends UIAbility {
  onCreate(want: Want, launchParam: AbilityConstant.LaunchParam): void {
  }

  onWindowStageCreate(windowStage: window.WindowStage): void {
    windowStage.loadContent('pages/Index', (err) => {
      if (err.code) {
        console.error(`Failed to load page. code=${err.code}, message=${err.message}`);
      }
    });
  }

  onDestroy(): void {
  }
}
'@

Write-Utf8NoBom -Path (Join-Path $target 'entry\src\main\ets\pages\Index.ets') -Content @'
class LedgerRecord {
  id: string;
  type: string;
  category: string;
  amount: number;
  note: string;
  occurredAt: number;

  constructor(id: string, type: string, category: string, amount: number, note: string, occurredAt: number) {
    this.id = id;
    this.type = type;
    this.category = category;
    this.amount = amount;
    this.note = note;
    this.occurredAt = occurredAt;
  }
}

class CategoryTotal {
  label: string;
  amount: number;
  color: string;

  constructor(label: string, amount: number, color: string) {
    this.label = label;
    this.amount = amount;
    this.color = color;
  }
}

const TEXT_TITLE: string = '\u8bb0\u8d26';
const TEXT_RECORDS: string = '\u8bb0\u5f55';
const TEXT_STATS: string = '\u7edf\u8ba1';
const TEXT_PROFILE: string = '\u6211\u7684';
const TEXT_REGISTER: string = '\u6ce8\u518c Bking';
const TEXT_REGISTER_DESC: string = '\u4f7f\u7528\u90ae\u7bb1\u521b\u5efa\u672c\u673a\u8bb0\u8d26\u6863\u6848\u3002';
const TEXT_EMAIL: string = '\u90ae\u7bb1';
const TEXT_NICKNAME: string = '\u6635\u79f0\uff08\u53ef\u9009\uff09';
const TEXT_REGISTER_BY_EMAIL: string = '\u4f7f\u7528\u90ae\u7bb1\u6ce8\u518c';
const TEXT_BALANCE: string = '\u5f53\u524d\u4f59\u989d';
const TEXT_INCOME: string = '\u6536\u5165';
const TEXT_EXPENSE: string = '\u652f\u51fa';
const TEXT_AMOUNT: string = '\u91d1\u989d';
const TEXT_NOTE: string = '\u5907\u6ce8';
const TEXT_SAVE: string = '\u4fdd\u5b58\u8bb0\u5f55';
const TEXT_EMPTY_RECORDS: string = '\u8fd8\u6ca1\u6709\u8bb0\u5f55';
const TEXT_EMPTY_RECORDS_BODY: string = '\u56de\u5230\u9996\u9875\u6dfb\u52a0\u6536\u5165\u6216\u652f\u51fa\u3002';
const TEXT_SUMMARY: string = '\u6c47\u603b';
const TEXT_SURPLUS: string = '\u7ed3\u4f59';
const TEXT_EXPENSE_STRUCTURE: string = '\u652f\u51fa\u7ed3\u6784';
const TEXT_INCOME_STRUCTURE: string = '\u6536\u5165\u7ed3\u6784';
const TEXT_NO_DATA: string = '\u6682\u65e0\u6570\u636e';
const TEXT_PERSONAL_INFO: string = '\u4e2a\u4eba\u4fe1\u606f';
const TEXT_CATEGORY: string = '\u5206\u7c7b';
const TEXT_DESC: string = '\u8bf4\u660e';
const TEXT_UNSET: string = '\u672a\u586b\u5199';
const TEXT_NO_NOTE: string = '\u65e0\u5907\u6ce8';
const TEXT_WEEK: string = '\u5468';
const TEXT_MONTH: string = '\u6708';
const TEXT_YEAR: string = '\u5e74';

const CATEGORY_SALARY: string = '\u5de5\u8d44\u6536\u5165';
const CATEGORY_EXTERNAL: string = '\u5916\u90e8\u6536\u5165';
const CATEGORY_MEAL: string = '\u9910\u98df';
const CATEGORY_SHOPPING: string = '\u8d2d\u7269';
const CATEGORY_SPORT: string = '\u8fd0\u52a8';

const INCOME_CATEGORIES: string[] = [CATEGORY_SALARY, CATEGORY_EXTERNAL];
const EXPENSE_CATEGORIES: string[] = [CATEGORY_MEAL, CATEGORY_SHOPPING, CATEGORY_SPORT];
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
  @State private category: string = CATEGORY_MEAL;
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
    const record = new LedgerRecord(
      `${Date.now()}`,
      this.recordType,
      this.category,
      amount,
      this.noteInput,
      Date.now()
    );
    this.records = [record, ...this.records];
    this.amountInput = '';
    this.noteInput = '';
  }

  private total(type: string): number {
    return this.records
      .filter((item: LedgerRecord) => item.type === type)
      .reduce((sum: number, item: LedgerRecord) => sum + item.amount, 0);
  }

  private breakdown(type: string): CategoryTotal[] {
    const source = type === 'income' ? INCOME_CATEGORIES : EXPENSE_CATEGORIES;
    return source.map((label: string, index: number) => {
      const amount = this.records
        .filter((item: LedgerRecord) => item.type === type && item.category === label)
        .reduce((sum: number, item: LedgerRecord) => sum + item.amount, 0);
      return new CategoryTotal(label, amount, CATEGORY_COLORS[index % CATEGORY_COLORS.length]);
    }).filter((item: CategoryTotal) => item.amount > 0);
  }

  private money(value: number): string {
    return '¥' + value.toFixed(2);
  }

  private title(): string {
    if (this.selectedTab === 0) {
      return TEXT_TITLE;
    }
    if (this.selectedTab === 1) {
      return TEXT_RECORDS;
    }
    if (this.selectedTab === 2) {
      return TEXT_STATS;
    }
    return TEXT_PROFILE;
  }

  build() {
    Column() {
      if (!this.isRegistered()) {
        this.RegisterView()
      } else {
        this.AppView()
      }
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F8FAF8')
  }

  @Builder
  RegisterView() {
    Column() {
      Text(TEXT_REGISTER)
        .fontSize(30)
        .fontWeight(FontWeight.Bold)
      Text(TEXT_REGISTER_DESC)
        .fontSize(15)
        .fontColor('#5F666A')
        .margin({ top: 12, bottom: 20 })
      TextInput({ placeholder: TEXT_EMAIL, text: this.emailInput })
        .onChange((value: string) => {
          this.emailInput = value;
        })
      TextInput({ placeholder: TEXT_NICKNAME, text: this.nameInput })
        .margin({ top: 10 })
        .onChange((value: string) => {
          this.nameInput = value;
        })
      Button(TEXT_REGISTER_BY_EMAIL)
        .width('100%')
        .margin({ top: 16 })
        .onClick(() => {
          this.register();
        })
    }
    .width('100%')
    .height('100%')
    .justifyContent(FlexAlign.Center)
    .padding(24)
  }

  @Builder
  AppView() {
    Column() {
      Text(this.title())
        .fontSize(24)
        .fontWeight(FontWeight.Bold)
        .margin({ top: 20, bottom: 12 })
      Scroll() {
        Column() {
          if (this.selectedTab === 0) {
            this.HomeView()
          } else if (this.selectedTab === 1) {
            this.RecordsView()
          } else if (this.selectedTab === 2) {
            this.StatsView()
          } else {
            this.ProfileView()
          }
        }
        .padding({ left: 18, right: 18, bottom: 18 })
      }
      .layoutWeight(1)
      this.NavBar()
    }
    .height('100%')
  }

  @Builder
  HomeView() {
    Column() {
      Column() {
        Text(TEXT_BALANCE)
          .fontSize(15)
          .fontColor(Color.White)
        Text(this.money(this.total('income') - this.total('expense')))
          .fontSize(34)
          .fontColor(Color.White)
          .fontWeight(FontWeight.Bold)
          .margin({ top: 8, bottom: 12 })
        Row() {
          Text(TEXT_INCOME + ' ' + this.money(this.total('income')))
            .fontColor(Color.White)
            .layoutWeight(1)
          Text(TEXT_EXPENSE + ' ' + this.money(this.total('expense')))
            .fontColor(Color.White)
        }
      }
      .width('100%')
      .padding(22)
      .borderRadius(10)
      .backgroundColor('#007D7E')

      Row() {
        Button(TEXT_EXPENSE)
          .margin({ right: 10 })
          .onClick(() => {
            this.recordType = 'expense';
            this.category = CATEGORY_MEAL;
          })
        Button(TEXT_INCOME)
          .onClick(() => {
            this.recordType = 'income';
            this.category = CATEGORY_SALARY;
          })
      }
      .margin({ top: 14, bottom: 8 })

      this.CategoryPicker()
      TextInput({ placeholder: TEXT_AMOUNT, text: this.amountInput })
        .type(InputType.Number)
        .margin({ top: 8 })
        .onChange((value: string) => {
          this.amountInput = value;
        })
      TextInput({ placeholder: TEXT_NOTE, text: this.noteInput })
        .margin({ top: 8 })
        .onChange((value: string) => {
          this.noteInput = value;
        })
      Button(TEXT_SAVE)
        .width('100%')
        .margin({ top: 10 })
        .onClick(() => {
          this.saveRecord();
        })
    }
  }

  @Builder
  CategoryPicker() {
    Flex({ wrap: FlexWrap.Wrap }) {
      ForEach(this.recordType === 'income' ? INCOME_CATEGORIES : EXPENSE_CATEGORIES, (item: string) => {
        Button(item)
          .margin({ right: 8, bottom: 8 })
          .backgroundColor(this.category === item ? '#D7F0EF' : '#ECE8F2')
          .fontColor('#202124')
          .onClick(() => {
            this.category = item;
          })
      })
    }
  }

  @Builder
  RecordsView() {
    Column() {
      if (this.records.length === 0) {
        this.InfoCard(TEXT_EMPTY_RECORDS, TEXT_EMPTY_RECORDS_BODY)
      } else {
        ForEach(this.records, (item: LedgerRecord) => {
          this.InfoCard(
            item.type === 'income' ? TEXT_INCOME : TEXT_EXPENSE,
            item.category + ' · ' + this.money(item.amount) + ' · ' + (item.note.length > 0 ? item.note : TEXT_NO_NOTE)
          )
        }, (item: LedgerRecord) => item.id)
      }
    }
  }

  @Builder
  StatsView() {
    Column() {
      Row() {
        Button(this.statMode === 'week' ? TEXT_WEEK + ' ✓' : TEXT_WEEK)
          .margin({ right: 8 })
          .onClick(() => {
            this.statMode = 'week';
          })
        Button(this.statMode === 'month' ? TEXT_MONTH + ' ✓' : TEXT_MONTH)
          .margin({ right: 8 })
          .onClick(() => {
            this.statMode = 'month';
          })
        Button(this.statMode === 'year' ? TEXT_YEAR + ' ✓' : TEXT_YEAR)
          .onClick(() => {
            this.statMode = 'year';
          })
      }
      this.InfoCard(
        TEXT_SUMMARY,
        TEXT_INCOME + ' ' + this.money(this.total('income')) + '\n' +
        TEXT_EXPENSE + ' ' + this.money(this.total('expense')) + '\n' +
        TEXT_SURPLUS + ' ' + this.money(this.total('income') - this.total('expense'))
      )
      this.StructureView(TEXT_EXPENSE_STRUCTURE, this.breakdown('expense'), this.total('expense'))
      this.StructureView(TEXT_INCOME_STRUCTURE, this.breakdown('income'), this.total('income'))
    }
  }

  @Builder
  StructureView(titleText: string, items: CategoryTotal[], totalAmount: number) {
    Column() {
      Text(titleText)
        .fontSize(18)
        .fontWeight(FontWeight.Bold)
      if (items.length === 0 || totalAmount <= 0) {
        Text(TEXT_NO_DATA)
          .fontColor('#6F7478')
          .margin({ top: 8 })
      } else {
        ForEach(items, (item: CategoryTotal) => {
          Column() {
            Row() {
              Text(item.label)
                .layoutWeight(1)
              Text(this.money(item.amount) + ' · ' + Math.round(item.amount / totalAmount * 100).toString() + '%')
            }
            Text('■')
              .fontColor(item.color)
              .fontSize(18)
          }
          .margin({ top: 8 })
        }, (item: CategoryTotal) => item.label)
      }
    }
    .width('100%')
    .padding(16)
    .margin({ top: 12 })
    .borderRadius(8)
    .backgroundColor(Color.White)
  }

  @Builder
  ProfileView() {
    Column() {
      this.InfoCard(TEXT_PERSONAL_INFO, TEXT_EMAIL + ': ' + this.email + '\n' + TEXT_NICKNAME + ': ' + (this.name.length > 0 ? this.name : TEXT_UNSET))
      this.InfoCard(TEXT_CATEGORY, TEXT_INCOME + ': ' + CATEGORY_SALARY + ', ' + CATEGORY_EXTERNAL + '\n' + TEXT_EXPENSE + ': ' + CATEGORY_MEAL + ', ' + CATEGORY_SHOPPING + ', ' + CATEGORY_SPORT)
      this.InfoCard(TEXT_DESC, 'HarmonyOS ArkTS first pass. Data is currently stored in page state.')
    }
  }

  @Builder
  InfoCard(titleText: string, bodyText: string) {
    Column() {
      Text(titleText)
        .fontSize(18)
        .fontWeight(FontWeight.Bold)
      Text(bodyText)
        .fontSize(14)
        .fontColor('#4D5358')
        .margin({ top: 8 })
    }
    .width('100%')
    .padding(16)
    .margin({ top: 12 })
    .borderRadius(8)
    .backgroundColor(Color.White)
  }

  @Builder
  NavBar() {
    Row() {
      this.NavItem(0, '\u9996\u9875')
      this.NavItem(1, TEXT_RECORDS)
      this.NavItem(2, TEXT_STATS)
      this.NavItem(3, TEXT_PROFILE)
    }
    .width('100%')
    .padding({ top: 8, bottom: 8 })
    .backgroundColor(Color.White)
  }

  @Builder
  NavItem(index: number, label: string) {
    Button(label)
      .layoutWeight(1)
      .backgroundColor(this.selectedTab === index ? '#D7F0EF' : '#FFFFFF')
      .fontColor('#202124')
      .onClick(() => {
        this.selectedTab = index;
      })
  }
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

Write-Host 'Rewrote Index.ets using ASCII-safe Unicode escapes and cleared build caches.'
