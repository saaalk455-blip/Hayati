# Adhkar content review (Hisn al-Muslim, chapters 25 and 27)

Status (2026-11-19): **blocked. No adhkar were extracted and `assets/adhkar.json` is unchanged (empty).**

## Source access check

| Source | Result |
|---|---|
| PDF `risala-ar-hisnulmuslim.pdf` (islamhouse) | Shell download refused (HTTP 403 from the sandbox proxy); the page reader refused the URL (not on the session allowlist) |
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
- The PDF's own front matter (where printing/publishing permission is usually stated) could not be
  read, so the licence of the book text is **unknown**.
- AllahServants: copyrighted site; used only as a comparison reference, never copied.
- HadeethEnc: not accessed.
- Decision: nothing is copied. Needed: written permission from the rights holder (the author's
  endowment / publisher via IslamHouse), or the PDF front matter showing explicit permission.

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
