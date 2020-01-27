# 外旅シフト作成プログラム

## Input File
- csv file
- 含時間, email, 編號＋姓名, request...
- 例：2020/01/01  15:30:47, abc@gmail.com, 01 外旅太郎, ABD, O, CD,...
- 註：不希望時 "-"; 全天都可 "O"
- 以姓名分類，若有多個request則使用較晚的

## 規則
1. 從希望者較少的日期開始排
1. 早上希望者不足時，優先安排B &rarr; H &rarr; A
