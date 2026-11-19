# Adhkar content review (Hisn al-Muslim, chapters 25 and 27)

Status (2026-11-19): **blocked on licence and on schema decisions. No adhkar text was copied into
the repository and `assets/adhkar.json` is unchanged (empty).**

## Source file inspected (owner-attached PDF)

- Title: حصن المسلم من أذكار الكتاب والسنة, Said bin Ali bin Wahf Al-Qahtani, **34th edition, Rajab
  1427 AH**, 160 pages. The PDF is **scanned images with no text layer** (producer "Image
  Conversion Plug-in"; `pdftotext` returns nothing). Machine OCR of diacritised Arabic is not
  reliable, so any text would have to be transcribed by eye and then checked by a person.
- Printed page numbers equal PDF page numbers.
- Table of contents (pp. 153-156): chapter 25 «الأذكار بعد السلام من الصلاة» starts on p. 46;
  chapter 26 on p. 52; chapter 27 «أذكار الصباح والمساء» starts on p. 54; chapter 28 on p. 68.

## Second source file (owner-attached PDF, 2026-11-19)

- Same book, **5th edition, 1421 AH**, "طبع ونشر وزارة الشؤون الإسلامية والأوقاف والدعوة والإرشاد"
  (Saudi Ministry of Islamic Affairs, with the Cooperative Office for Call and Guidance), 96 PDF
  pages (pocket format). Also **image-only, no text layer**.
- Table of contents (p. 93): chapter 25 starts on p. 28, chapter 26 on p. 31, chapter 27 on p. 32,
  chapter 28 on p. 41. Printed page numbers equal PDF page numbers.
- Copyright page (p. 2): «© سعيد بن علي بن وهف القحطاني، 1421هـ»; **no free-distribution clause
  appears on it**, and the last pages (94-96) are the table of contents only. This edition therefore
  gives no permission that settles the question.
- Use: a second, independent edition for cross-checking wording and takhrij against the 34th
  edition. Differences between the editions must be recorded for review, not resolved by guessing.

## Licence (34th edition, copyright page, p. 2)

The page states «حقوق الطبع محفوظة إلا لمن أراد طبعه، وتوزيعه مجانًا، بدون حذف، أو إضافة أو تغيير..
بشرط أن يكتب على الغلاف الخارجي «وقف لله تعالى»» (rights reserved, except for whoever wants to print
and distribute it **free of charge, without deletion, addition or change**, on condition that the
outer cover carries «وقف لله تعالى»).

Assessment: this is a real permission, but for the **whole book, unchanged, free**. Putting only
chapters 25 and 27 into a JSON file, splitting morning and evening, and reshaping entries is
deletion and change, and an app is a new medium. It is **not clearly covered**, so nothing was
copied. Written permission from the rights holder is needed (or a decision to ship the complete,
unaltered book with the «وقف لله تعالى» notice, which is a different and much larger feature).

## Structure of chapters 25 and 27 (counted by eye from the scan; no text reproduced)

| Chapter | Pages | Numbered entries | Notes |
|---|---|---|---|
| 25 الأذكار بعد السلام من الصلاة | 46-51 | 8 (numbers 66-73) | Entry numbers run through the whole book, not per chapter. Some entries are conditional (only after Fajr; after Maghrib and Fajr; after every prayer) |
| 27 أذكار الصباح والمساء | 54-67 | 22 (numbers 75-96), plus one unnumbered opening line with a footnote | Evening wording is given in footnotes («وإذا أمسى قال…»), not as separate entries |

The comparison site lists 24 entries for chapter 27; the printed edition numbers 22 plus the
opening line. This difference is recorded, not resolved.

Findings that affect the JSON design (need a decision before any content is entered):
1. Repeat counts are written inside the text in parentheses or in footnotes, not as a field. Several
   entries state no count at all; entering `repeat: 1` for them would be an editorial inference and
   must be flagged for review, not assumed.
2. Some counts are conditional («عشر مرات» or «مرة واحدة عند الكسل»; «مائة مرة إذا أصبح»).
3. Some entries are morning-only, and many have a different evening wording in a footnote. The
   current model has one `text` per entry and one category, so morning/evening variants need a
   schema change (for example a `times` list and per-time `text`), which also edits the book's
   layout, which the licence forbids without permission.
4. Takhrij is the book's footnotes: book/volume/page references plus the author's quoted gradings
   («وصححه الألباني»، «وحسنه ابن باز»). They sometimes continue over a page break («=»). They are
   the author's statements and may only be copied under the licence; no grade may be added by us.



## Source access check

| Source | Result |
|---|---|
| PDF `risala-ar-hisnulmuslim.pdf` (islamhouse) | Shell download refused (HTTP 403 from the sandbox proxy); the page reader refused the URL (not on the session allowlist). The owner then attached an islamhouse PDF of the same book (see above) |
| `risala.prh.gov.sa/ar/content/51` | Refused (not on the session allowlist) |
| `l.islamhouse.com/ar/books/2522/` | Readable. Book page only (title, author Said bin Ali bin Wahf Al-Qahtani, links to PDF/DOCX editions). Not the book text |
| `islamhouse.com/ar/books/269032` (Wird al-Sabah wal-Masa) | Readable. Book page only |
| `allahservants.com/public/ar/duas/27` | Readable. Lists 24 entries for chapter 27. Footer: "© 2026 AllahServants.com"; credits hisnmuslim.com. Comparison reference only |
| Other PDF/DOCX editions linked from the IslamHouse page | Refused (not on the allowlist) |

The page reader summarises and limits quotes to about 125 characters, so even readable pages cannot
give a verbatim, diacritics-exact text. Extraction therefore needs the PDF file itself.

## Licence check

- IslamHouse book pages (both books): no permission statement for copying or redistribution was
  found. The only language is the site-wide notice "يلتزم الموقع بحفظ حقوق الملكية الفكرية للجهات
  والأفراد" and "© موقع IslamHouse.com". Free availability is not treated as permission.
- The attached PDF's copyright page was read; see "Licence (copyright page)" above.
- AllahServants: copyrighted site; used only as a comparison reference, never copied.
- HadeethEnc: not accessed.
- Decision: nothing is copied. Needed: written permission from the rights holder to use selected
  chapters, restructured, inside the app.

## Review table (filled 2026-11-19)

Text: digital vocalised copy supplied by the owner (no licence notice inside the file; the owner states the source permits publishing). Takhrij, counts and evening wording: read visually from pages 46–51 and 54–68 of the 34th printed edition. Gradings appear only where the book quotes them. The 5th edition (1421 AH) was not re-collated entry by entry (scan only).

Known discrepancies / decisions needing a human:
- ap-02: the digital copy adds «[ثلاثًا]» that the printed 34th edition does not have; it was NOT copied.
- Evening wording (e-03, e-04, e-06, e-07, e-15, e-16) is composed from the book's footnote variants; the footnote words are unvocalised in the book and were not vocalised here.
- The unnumbered opening line «الحمد لله وحده والصلاة والسلام على من لا نبي بعده» is not shipped (not a numbered dhikr).
- Entries with a count inside the text (ap-01, ap-04, m-18) keep repeat 1/10 as listed; whether the counter should model 3/33 internally is a product decision.
- Numbering differs between editions (printed 34th: 66–73 / 75–98; digital copy: 86–93 / 96–129); the file records the printed numbers.
- Licence: not independently verified; relies on the owner statement.

| id | الفئة | ص | رقم | العدد | المصدر/التخريج (مختصر) | مطابقة النص | الترخيص | مراجعة بشرية |
|---|---|---|---|---|---|---|---|---|
| ap-01 | after_prayer | 46 | 66 | 1 | رواه أهل السنن وانظر صحيح ابن ماجه 2/329 (ص46 هامش 1؛ وفي ص47: مسلم 1/… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | نعم: الاستغفار ثلاثًا مذكور داخل النص؛ العدّاد يحسب الذكر مرة واحدة |
| ap-02 | after_prayer | 47 | 67 | 1 | البخاري 1/255 ومسلم 1/414 | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | نعم: النسخة الرقمية فيها قيد [ثلاثًا] بعد «قدير» وهو غير موجود في الطبعة الورقية 34؛ لم يُنقل القيد؛ يحتاج حسم |
| ap-03 | after_prayer | 48 | 68 | 1 | مسلم 1/415 | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| ap-04 | after_prayer | 48 | 69 | 1 | مسلم 1/418 وفيه: من قال ذلك دبر كل صلاة غفرت خطاياه وإن كانت مثل زبد ا… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | نعم: التسبيح والتحميد والتكبير ثلاثًا وثلاثين مذكور داخل النص؛ يحتاج حسم طريقة العدّ |
| ap-05 | after_prayer | 49 | 70 | 1 | أبو داود 2/86 والنسائي 3/68 وانظر صحيح الترمذي 2/8. والسور الثلاث يقال… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| ap-06 | after_prayer | 50 | 71 | 1 | من قرأها دبر كل صلاة لم يمنعه من دخول الجنة إلا أن يموت. النسائي في عم… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| ap-07 | after_prayer | 51 | 72 | 10 | الترمذي 5/515 وأحمد 4/227، وانظر تخريجه في زاد المعاد 1/300 | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| ap-08 | after_prayer | 51 | 73 | 1 | ابن ماجه وغيره. وانظر صحيح ابن ماجه 1/152 ومجمع الزوائد 10/111، وسيأتي… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| m-01 | morning | 54 | 75 | 1 | من قالها حين يصبح أجير من الجن حتى يمسي، ومن قالها حين يمسي أجير منهم … | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| e-01 | evening | 54 | 75 | 1 | من قالها حين يصبح أجير من الجن حتى يمسي، ومن قالها حين يمسي أجير منهم … | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| m-02 | morning | 55 | 76 | 3 | من قالها ثلاث مرات حين يصبح وحين يمسي كفته من كل شيء. أخرجه أبو داود 4… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| e-02 | evening | 55 | 76 | 3 | من قالها ثلاث مرات حين يصبح وحين يمسي كفته من كل شيء. أخرجه أبو داود 4… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| m-03 | morning | 57 | 77 | 1 | مسلم 4/2088 | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | نعم: صيغة المساء مركّبة من هامش الكتاب (غير مشكولة في الأصل) |
| e-03 | evening | 57 | 77 | 1 | مسلم 4/2088 | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | نعم: صيغة المساء مركّبة من هامش الكتاب (غير مشكولة في الأصل) |
| m-04 | morning | 58 | 78 | 1 | الترمذي 5/466 وانظر صحيح الترمذي 3/142 | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | نعم: صيغة المساء مركّبة من هامش الكتاب (غير مشكولة في الأصل) |
| e-04 | evening | 58 | 78 | 1 | الترمذي 5/466 وانظر صحيح الترمذي 3/142 | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | نعم: صيغة المساء مركّبة من هامش الكتاب (غير مشكولة في الأصل) |
| m-05 | morning | 58 | 79 | 1 | من قالها موقنًا بها حين يمسي فمات من ليلته دخل الجنة، وكذلك إذا أصبح. … | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| e-05 | evening | 58 | 79 | 1 | من قالها موقنًا بها حين يمسي فمات من ليلته دخل الجنة، وكذلك إذا أصبح. … | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| m-06 | morning | 59 | 80 | 4 | من قالها حين يصبح أو يمسي أربع مرات أعتقه الله من النار. أخرجه أبو داو… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | نعم: صيغة المساء مركّبة من هامش الكتاب (غير مشكولة في الأصل) |
| e-06 | evening | 59 | 80 | 4 | من قالها حين يصبح أو يمسي أربع مرات أعتقه الله من النار. أخرجه أبو داو… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | نعم: صيغة المساء مركّبة من هامش الكتاب (غير مشكولة في الأصل) |
| m-07 | morning | 60 | 81 | 1 | من قالها حين يصبح فقد أدّى شكر يومه، ومن قالها حين يمسي فقد أدّى شكر ل… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | نعم: صيغة المساء مركّبة من هامش الكتاب (غير مشكولة في الأصل) |
| e-07 | evening | 60 | 81 | 1 | من قالها حين يصبح فقد أدّى شكر يومه، ومن قالها حين يمسي فقد أدّى شكر ل… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | نعم: صيغة المساء مركّبة من هامش الكتاب (غير مشكولة في الأصل) |
| m-08 | morning | 60 | 82 | 3 | أبو داود 4/324، وأحمد 5/42، والنسائي في عمل اليوم والليلة برقم 22 وابن… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| e-08 | evening | 60 | 82 | 3 | أبو داود 4/324، وأحمد 5/42، والنسائي في عمل اليوم والليلة برقم 22 وابن… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| m-09 | morning | 61 | 83 | 7 | من قالها حين يصبح وحين يمسي سبع مرات كفاه الله ما أهمه من أمر الدنيا و… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| e-09 | evening | 61 | 83 | 7 | من قالها حين يصبح وحين يمسي سبع مرات كفاه الله ما أهمه من أمر الدنيا و… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| m-10 | morning | 61 | 84 | 1 | أبو داود وابن ماجه وانظر صحيح ابن ماجه 2/332 | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| e-10 | evening | 61 | 84 | 1 | أبو داود وابن ماجه وانظر صحيح ابن ماجه 2/332 | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| m-11 | morning | 62 | 85 | 1 | الترمذي وأبو داود. وانظر: صحيح الترمذي 3/142 | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| e-11 | evening | 62 | 85 | 1 | الترمذي وأبو داود. وانظر: صحيح الترمذي 3/142 | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| m-12 | morning | 63 | 86 | 3 | من قالها ثلاثًا إذا أصبح وثلاثًا إذا أمسى لم يضره شيء. أخرجه أبو داود … | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| e-12 | evening | 63 | 86 | 3 | من قالها ثلاثًا إذا أصبح وثلاثًا إذا أمسى لم يضره شيء. أخرجه أبو داود … | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| m-13 | morning | 63 | 87 | 3 | من قالها ثلاثًا حين يصبح وثلاثًا حين يمسي كان حقًا على الله أن يرضيه ي… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| e-13 | evening | 63 | 87 | 3 | من قالها ثلاثًا حين يصبح وثلاثًا حين يمسي كان حقًا على الله أن يرضيه ي… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| m-14 | morning | 64 | 88 | 1 | الحاكم وصححه ووافقه الذهبي 1/545 وانظر صحيح الترغيب والترهيب 1/273 | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| e-14 | evening | 64 | 88 | 1 | الحاكم وصححه ووافقه الذهبي 1/545 وانظر صحيح الترغيب والترهيب 1/273 | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| m-15 | morning | 64 | 89 | 1 | أبو داود 4/322 وحسن إسناده شعيب وعبدالقادر الأرناؤوط في تحقيق زاد المع… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | نعم: صيغة المساء مركّبة من هامش الكتاب (غير مشكولة في الأصل) |
| e-15 | evening | 64 | 89 | 1 | أبو داود 4/322 وحسن إسناده شعيب وعبدالقادر الأرناؤوط في تحقيق زاد المع… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | نعم: صيغة المساء مركّبة من هامش الكتاب (غير مشكولة في الأصل) |
| m-16 | morning | 65 | 90 | 1 | أحمد 3/406 و407 وابن السني في عمل اليوم والليلة برقم 34 وانظر: صحيح ال… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | نعم: صيغة المساء مركّبة من هامش الكتاب (غير مشكولة في الأصل) |
| e-16 | evening | 65 | 90 | 1 | أحمد 3/406 و407 وابن السني في عمل اليوم والليلة برقم 34 وانظر: صحيح ال… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | نعم: صيغة المساء مركّبة من هامش الكتاب (غير مشكولة في الأصل) |
| m-17 | morning | 65 | 91 | 100 | من قالها مائة مرة حين يصبح وحين يمسي لم يأت أحد يوم القيامة بأفضل مما … | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| e-17 | evening | 65 | 91 | 100 | من قالها مائة مرة حين يصبح وحين يمسي لم يأت أحد يوم القيامة بأفضل مما … | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| m-18 | morning | 66 | 92 | 10 | النسائي في عمل اليوم والليلة برقم 24 وانظر: صحيح الترغيب والترهيب 1/27… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | نعم: العدد عشر مرات، أو مرة واحدة عند الكسل (مذكور في النص) |
| e-18 | evening | 66 | 92 | 10 | النسائي في عمل اليوم والليلة برقم 24 وانظر: صحيح الترغيب والترهيب 1/27… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | نعم: العدد عشر مرات، أو مرة واحدة عند الكسل (مذكور في النص) |
| m-19 | morning | 66 | 93 | 100 | من قالها مائة مرة في يوم كانت له عدل عشر رقاب، وكتبت له مائة حسنة، ومح… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | نعم: ذكر خاص بالصباح (إذا أصبح) |
| m-20 | morning | 67 | 94 | 3 | مسلم 4/2090 | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | نعم: ذكر خاص بالصباح (إذا أصبح) |
| m-21 | morning | 67 | 95 | 1 | أخرجه ابن السني في عمل اليوم والليلة برقم 54 وابن ماجه برقم 925 وحسن إ… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | نعم: ذكر خاص بالصباح (إذا أصبح) |
| m-22 | morning | 67 | 96 | 100 | البخاري مع الفتح 11/101، ومسلم 4/2075 | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| e-22 | evening | 67 | 96 | 100 | البخاري مع الفتح 11/101، ومسلم 4/2075 | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| e-23 | evening | 68 | 97 | 3 | من قالها حين يمسي ثلاث مرات لم تضره حمة تلك الليلة. أخرجه أحمد 2/290، … | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | نعم: ذكر خاص بالمساء (إذا أمسى) |
| m-24 | morning | 68 | 98 | 10 | من صلى علي حين يصبح عشرًا وحين يمسي عشرًا أدركته شفاعتي يوم القيامة. أ… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |
| e-24 | evening | 68 | 98 | 10 | من صلى علي حين يصبح عشرًا وحين يمسي عشرًا أدركته شفاعتي يوم القيامة. أ… | تحقق من المتن الرقمي مقابل الطبعتين الممسوحتين (قراءة بصرية للتخريج فقط) | تأكيد المالك: المصدر يسمح بالنشر (لم يُتحقق منه من طرفنا) | لا |

## Future expansion list (documentation only, not for the UI yet)

Waking up, sleep, entering/leaving home, entering/leaving the mosque, adhan, wudu, ruku/sujud,
food and drink, travel and riding, worry/grief/distress. Each needs its own schema review
(a "section" notion beyond three categories, per-section progress, ordering) before any UI work.
