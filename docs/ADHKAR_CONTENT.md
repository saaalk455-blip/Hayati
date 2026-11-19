# Adhkar content (assets/adhkar.json)

Status: **the shipped file is empty** (`"adhkar": []`). Phase 5 is not complete until verified
content is added (KI-025).

## Why it is empty
- No adhkar text is written from memory and none is copied from sites whose licence is unclear
  (islamweb.net states all rights reserved; public JSON datasets found had no stated licence).
- Verbatim verification was attempted and was **not possible** with the tools available: the page
  reader summarises pages and limits quotes to about 125 characters, so a full text cannot be
  compared character by character. An unverified text is worse than no text.

## File format
```json
{
  "version": 1,
  "source": "name and edition of the source collection",
  "adhkar": [
    {
      "id": "unique-id",
      "category": "morning | evening | after_prayer",
      "text": "the Arabic text exactly as in the source",
      "repeat": 3,
      "source": "e.g. القرآن الكريم، البقرة: 255 / رواه … برقم …",
      "takhrij": "the authentication line exactly as in the source"
    }
  ]
}
```

## Rules enforced by code and tests
- Parser (strict, one bad entry rejects the file): unique non-blank `id`, known `category`, non-blank
  `text`, `repeat` present and >= 1.
- Release rules: `source` and `takhrij` non-blank, no placeholder markers in the text, no duplicate
  text inside a category. Tested against the real shipped file (`AdhkarShippedContentTest`).
- Release gate test (skipped while the file is empty): every category must contain adhkar.
- CI prints how many adhkar are inside the built APK and which lack source/takhrij.
- These checks cannot tell whether a text is *correct*. A person must compare each entry with the
  original source before merging.

## What is needed to finish
1. A source the owner trusts and may legally use: a file already in the format above, or written
   permission from the publisher, or the text of a public-domain printed edition provided by the owner.
2. Each entry must carry the repeat count and takhrij as printed in that source. Nothing is added or
   graded by the app.
3. Owner or reviewer compares the entries with the source, then the file is committed.

## Reminders (not implemented)
Adhkar reminders are a separate part, proposed after the content is approved: reuse the prayer
notification infrastructure (AlarmManager, channels, reschedule triggers), with per-category enable
and time settings. Not started; no placeholder reminder system exists (KI-028).
