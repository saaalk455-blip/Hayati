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

## Licence (copyright page, p. 2)

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

## Review table (to be filled once the PDF and licence are available)

| id | text | repeat | source | takhrij | matches source? | licence | needs human review |
|---|---|---|---|---|---|---|---|
| (none) | — | — | — | — | — | unknown | — |

Chapter 27 count on the comparison site: 24 entries (not verified against the book).
Chapter 25 count: not checked.

## Future expansion list (documentation only, not for the UI yet)

Waking up, sleep, entering/leaving home, entering/leaving the mosque, adhan, wudu, ruku/sujud,
food and drink, travel and riding, worry/grief/distress. Each needs its own schema review
(a "section" notion beyond three categories, per-section progress, ordering) before any UI work.
