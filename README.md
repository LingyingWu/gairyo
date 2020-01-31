# 外旅シフト作成プログラム

## Input Format
- csv file
- format: time, email, number+name, \[request\]
- example：2020/01/01 15:30:47, abc@gmail.com, 01 外旅太郎, \[ABD, O, CD,...\]
- notation：不希望時 "-"; 全天都可 "O"
- basically sorted by **name**, when multiple requested with the same name received, will overwrite with that with latest timestamp

## Rules
1. 日期安排順序：希望者數較少的先
1. 希望者充足時，優先安排chinese staff兩位(random)
1. 選擇順序：**希望日數<5** &rarr; **\(已分配的日數與希望日數\)之比例較低** &rarr; **已分配日數>15** 
1. 午前番安排順序：B &rarr; H &rarr; A
1. 午後番排順序：D &rarr; C
1. 不允許整天班
1. 最多三連勤，不允許四連勤
