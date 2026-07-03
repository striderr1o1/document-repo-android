# WhatsApp Document Organizer (Android)

An AI-powered file organizer that automatically classifies and sorts documents (PDFs, images, receipts) shared via WhatsApp — built for messy, mixed-language, real-world document flows (invoices, medical records, tuition/college docs, tenant/property paperwork) that generic file organizers don't handle well.

## Why

Existing AI file organizers (Sortio, Filex AI, AI File Sorter) are either desktop-only or generic mobile tools optimized for English-first, globally generic use cases. This project targets a specific, underserved gap: WhatsApp-native document chaos common in South Asian freelancer/small-business/student workflows — where WhatsApp, not email, is the primary document-transfer channel.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    INGESTION LAYER                            │
├─────────────────────────────────────────────────────────────┤
│  Share Sheet Receiver (ACTION_SEND / ACTION_SEND_MULTIPLE)    │
│  → Primary intake. Zero permissions needed.                   │
│                                                                 │
│  SAF Folder Watcher (ACTION_OPEN_DOCUMENT_TREE)                │
│  → Phase 2. One-time picked folder, persisted URI permission. │
│  → Polled via WorkManager periodic job (15-30 min interval)   │
└────────────────────┬────────────────────────────────────────┘
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                  PRE-PROCESSING LAYER                         │
├─────────────────────────────────────────────────────────────┤
│  File Type Router → PDF / Image / Doc                         │
│       │                                                        │
│  Text Extraction:                                              │
│    - PDF with text layer → PDFBox-Android / PdfRenderer        │
│    - Image or scanned PDF → ML Kit Text Recognition (OCR)      │
│    - Fallback: ML Kit doesn't support Urdu well —               │
│      test early, may need Google Cloud Vision as backup        │
└────────────────────┬────────────────────────────────────────┘
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                CLASSIFICATION LAYER                           │
├─────────────────────────────────────────────────────────────┤
│  Path A (fast, deterministic):                                │
│    Keyword/regex match against category dictionaries          │
│    (e.g. "invoice", "due date", finance-related local terms)   │
│    → High-confidence exact match? File immediately.            │
│                                                                 │
│  Path B (fallback, semantic):                                 │
│    MediaPipe Text Embedder (on-device)                         │
│    → Compare extracted text embedding to category               │
│      prototype embeddings (precomputed, stored locally)        │
│    → Cosine similarity per category                             │
│                                                                 │
│  Decision Rule:                                                │
│    - Keyword match confidence ≥ threshold → use Path A          │
│    - Else use Path B's top category IF similarity ≥ threshold  │
│    - Else → "Needs Review" bucket, never auto-file blind        │
└────────────────────┬────────────────────────────────────────┘
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                    ACTION LAYER                                │
├─────────────────────────────────────────────────────────────┤
│  File Mover → app-scoped storage or SAF-granted destination    │
│  Rename Engine → optional, based on extracted entities          │
│  Collision Handler → append date/index if name exists           │
└────────────────────┬────────────────────────────────────────┘
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                  STATE / STORAGE LAYER                        │
├─────────────────────────────────────────────────────────────┤
│  Room DB (SQLite) — local:                                     │
│    - file_log (source, destination, category, confidence,      │
│      timestamp, extracted_text_hash)                           │
│    - categories (name, prototype_embedding, keyword_list)      │
│    - undo_stack (last N operations, reversible)                │
└────────────────────┬────────────────────────────────────────┘
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                    UI LAYER                                    │
├─────────────────────────────────────────────────────────────┤
│  Minimal Jetpack Compose UI:                                   │
│    - "Needs Review" queue (swipe to confirm/correct category)  │
│    - Category folder browser                                    │
│    - Undo/history screen                                        │
│    - Settings: category management, add keywords                │
└─────────────────────────────────────────────────────────────┘
```

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Min SDK:** 26+
- **OCR:** ML Kit Text Recognition (Google Cloud Vision as fallback for non-Latin scripts)
- **Text Embeddings:** MediaPipe Text Embedder (on-device)
- **PDF text extraction:** PDFBox-Android / Android `PdfRenderer`
- **Local storage:** Room (SQLite)
- **Background scheduling:** WorkManager

## Feature List

### Phase 0 — Proof of Concept (build first)
- [ ] Share-sheet receiver for PDFs/images (`ACTION_SEND`)
- [ ] OCR via ML Kit for images/scanned PDFs
- [ ] Keyword-match classification against 4–5 hardcoded categories (finance, medical, college/uni, tenant/property, misc)
- [ ] Move file into local category folder
- [ ] Basic operation log (Room table, no UI yet)

### Phase 1 — Trust & Accuracy
- [ ] Embedding fallback (MediaPipe Text Embedder) for keyword misses
- [ ] "Needs Review" queue UI (manual confirm/correct low-confidence files)
- [ ] Single-level undo (last action)
- [ ] Category management screen (user-editable keyword lists)

### Phase 2 — Near-Automatic Capture
- [ ] SAF folder watcher for automatic capture (no manual share needed)
- [ ] Entity extraction (dates, amounts, vendor names) for smart renaming
- [ ] Multi-level undo / full history timeline
- [ ] Urdu-aware OCR/keyword handling (if testing shows it's needed)
- [ ] Cross-device cloud sync (only if there's real demand)

### Explicitly Out of Scope (for now)
- Lease/deadline notification reminders
- WhatsApp Business Cloud API integration
- iOS port
- Cloud AI fallback for low-confidence classification

## Getting Started

1. New Android Studio project — Kotlin, min SDK 26+, Jetpack Compose.
2. Implement the share-sheet intent filter — receive a shared PDF/image and log its URI. Test with real WhatsApp shares first.
3. Wire in ML Kit Text Recognition (OCR). Test against real, blurry, WhatsApp-forwarded documents — not clean sample PDFs.
4. Hardcode 5 categories with keyword lists (plain string matching to start). Route extracted text against them, move file to a local folder per category.
5. Test with real files from real users (yourself + a couple of friends/family). Evaluate: OCR failure rate, keyword miss rate, and whether the categories actually match real document types people receive.

**Do not build the embedding layer, review UI, or SAF automation until step 5 validates whether keyword-matching alone is sufficient or a semantic fallback is genuinely needed.**

## Status

🚧 Early prototype / exploration phase.
