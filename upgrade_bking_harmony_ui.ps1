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

$entryStringPath = Join-Path $target 'entry\src\main\resources\base\element\string.json'
Write-Utf8NoBom -Path $entryStringPath -Content @'
{
  "string": [
    {
      "name": "entry_desc",
      "value": "Personal bookkeeping"
    }
  ]
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

const TEXT_HOME: string = '\u9996\u9875';
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
const TEXT_START: string = '\u8d77\u59cb';
const TEXT_END_DEFAULT: string = '\u7ed3\u675f\u9ed8\u8ba4\u5230';
const TEXT_NEXT_WEEK: string = '\u4e0b\u4e00\u5468';
const TEXT_NEXT_MONTH: string = '\u4e0b\u4e00\u6708';
const TEXT_NEXT_YEAR: string = '\u4e0b\u4e00\u5e74';

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
  @State private statStartInput: string = '2026-05-13';
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
    const record = new LedgerRecord(`${Date.now()}`, this.recordType, this.category, amount, this.noteInput, Date.now());
    this.records = [record, ...this.records];
    this.amountInput = '';
    this.noteInput = '';
  }

  private money(value: number): string {
    return '\u00A5' + value.toFixed(2);
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

  private allTotal(type: string): number {
    return this.records
      .filter((item: LedgerRecord) => item.type === type)
      .reduce((sum: number, item: LedgerRecord) => sum + item.amount, 0);
  }

  private periodRecords(): LedgerRecord[] {
    const start = this.periodStart();
    const end = this.periodEnd();
    if (start <= 0 || end <= start) {
      return this.records;
    }
    return this.records.filter((item: LedgerRecord) => item.occurredAt >= start && item.occurredAt < end);
  }

  private periodTotal(type: string): number {
    return this.periodRecords()
      .filter((item: LedgerRecord) => item.type === type)
      .reduce((sum: number, item: LedgerRecord) => sum + item.amount, 0);
  }

  private periodBreakdown(type: string): CategoryTotal[] {
    const source = type === 'income' ? INCOME_CATEGORIES : EXPENSE_CATEGORIES;
    const periodItems = this.periodRecords();
    return source.map((label: string, index: number) => {
      const amount = periodItems
        .filter((item: LedgerRecord) => item.type === type && item.category === label)
        .reduce((sum: number, item: LedgerRecord) => sum + item.amount, 0);
      return new CategoryTotal(label, amount, CATEGORY_COLORS[index % CATEGORY_COLORS.length]);
    }).filter((item: CategoryTotal) => item.amount > 0);
  }

  private periodStart(): number {
    let raw = this.statStartInput.trim();
    if (this.statMode === 'month') {
      raw = raw + '-01';
    } else if (this.statMode === 'year') {
      raw = raw + '-01-01';
    }
    const date = new Date(raw);
    const time = date.getTime();
    if (Number.isNaN(time)) {
      return 0;
    }
    return time;
  }

  private periodEnd(): number {
    const start = this.periodStart();
    if (start <= 0) {
      return 0;
    }
    const date = new Date(start);
    if (this.statMode === 'week') {
      date.setDate(date.getDate() + 7);
    } else if (this.statMode === 'month') {
      date.setMonth(date.getMonth() + 1);
    } else {
      date.setFullYear(date.getFullYear() + 1);
    }
    return date.getTime();
  }

  private periodHint(): string {
    if (this.statMode === 'week') {
      return TEXT_END_DEFAULT + ' ' + TEXT_NEXT_WEEK;
    }
    if (this.statMode === 'month') {
      return TEXT_END_DEFAULT + ' ' + TEXT_NEXT_MONTH;
    }
    return TEXT_END_DEFAULT + ' ' + TEXT_NEXT_YEAR;
  }

  private startPlaceholder(): string {
    if (this.statMode === 'week') {
      return 'YYYY-MM-DD';
    }
    if (this.statMode === 'month') {
      return 'YYYY-MM';
    }
    return 'YYYY';
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
        .margin({ top: 14, bottom: 6 })
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
        .padding({ left: 18, right: 18, bottom: 12 })
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
        Text(this.money(this.allTotal('income') - this.allTotal('expense')))
          .fontSize(32)
          .fontColor(Color.White)
          .fontWeight(FontWeight.Bold)
          .margin({ top: 8, bottom: 12 })
        Row() {
          Text(TEXT_INCOME + ' ' + this.money(this.allTotal('income')))
            .fontColor(Color.White)
            .layoutWeight(1)
          Text(TEXT_EXPENSE + ' ' + this.money(this.allTotal('expense')))
            .fontColor(Color.White)
        }
      }
      .width('100%')
      .padding(20)
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
            item.category + ' \u00B7 ' + this.money(item.amount) + ' \u00B7 ' + (item.note.length > 0 ? item.note : TEXT_NO_NOTE)
          )
        }, (item: LedgerRecord) => item.id)
      }
    }
  }

  @Builder
  StatsView() {
    Column() {
      Row() {
        Button(this.statMode === 'week' ? TEXT_WEEK + ' \u2713' : TEXT_WEEK)
          .margin({ right: 8 })
          .onClick(() => {
            this.statMode = 'week';
            this.statStartInput = '2026-05-13';
          })
        Button(this.statMode === 'month' ? TEXT_MONTH + ' \u2713' : TEXT_MONTH)
          .margin({ right: 8 })
          .onClick(() => {
            this.statMode = 'month';
            this.statStartInput = '2026-05';
          })
        Button(this.statMode === 'year' ? TEXT_YEAR + ' \u2713' : TEXT_YEAR)
          .onClick(() => {
            this.statMode = 'year';
            this.statStartInput = '2026';
          })
      }
      TextInput({ placeholder: TEXT_START + ' ' + this.startPlaceholder(), text: this.statStartInput })
        .margin({ top: 10 })
        .onChange((value: string) => {
          this.statStartInput = value;
        })
      Text(this.periodHint())
        .fontSize(13)
        .fontColor('#6F7478')
        .margin({ top: 6, bottom: 2 })
      this.InfoCard(
        TEXT_SUMMARY,
        TEXT_INCOME + ' ' + this.money(this.periodTotal('income')) + '\n' +
        TEXT_EXPENSE + ' ' + this.money(this.periodTotal('expense')) + '\n' +
        TEXT_SURPLUS + ' ' + this.money(this.periodTotal('income') - this.periodTotal('expense'))
      )
      this.StructureView(TEXT_EXPENSE_STRUCTURE, this.periodBreakdown('expense'), this.periodTotal('expense'))
      this.StructureView(TEXT_INCOME_STRUCTURE, this.periodBreakdown('income'), this.periodTotal('income'))
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
              Text(this.money(item.amount) + ' \u00B7 ' + Math.round(item.amount / totalAmount * 100).toString() + '%')
            }
            Progress({ value: item.amount, total: totalAmount, type: ProgressType.Linear })
              .color(item.color)
              .height(8)
              .margin({ top: 4 })
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
      this.NavItem(0, TEXT_HOME)
      this.NavItem(1, TEXT_RECORDS)
      this.NavItem(2, TEXT_STATS)
      this.NavItem(3, TEXT_PROFILE)
    }
    .width('100%')
    .padding({ top: 8, bottom: 8, left: 10, right: 10 })
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

Write-Host 'Upgraded Harmony UI and cleared build caches.'
