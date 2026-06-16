# Privacy Policy — Finatra

**Last updated: June 2026**

> **Note:** This is a template policy provided for transparency about how the app is built. It is **not legal advice**. If you publish Finatra, have it reviewed by a qualified professional.

Finatra ("the app") is a personal finance manager published by **Jinatra**. Privacy is a core design principle: the app is built to work **entirely offline**, and your financial data **stays on your device**.

## 1. Data we collect

**We do not collect, transmit, or store your data on any server.** Finatra has **no analytics, no crash reporting, and no telemetry.**

All information you enter — accounts, transactions, budgets, categories, balances, notes, tags, and receipt photos — is stored **locally on your device** in the app's private storage.

## 2. Sensitive data

- **API keys and PIN** are stored in Android's `EncryptedSharedPreferences` (encrypted at rest).
- **Receipt photos** are stored in the app's private files directory and are not shared.
- The app can enable `FLAG_SECURE` to block screenshots and screen recording of your financial data.

## 3. Optional AI features

AI features are **off by default** and entirely optional.

- **On-device AI (Gemma):** runs fully locally. No data leaves your device.
- **Cloud AI (Gemini / Claude / OpenRouter):** if **you** configure an API key, the specific text you ask the app to process (e.g. a transaction description or a spending summary) is sent **directly to the provider you chose**, using **your own key**, to generate a response. This data is handled under **that provider's** privacy policy. Finatra does not proxy, log, or retain these requests. Your API key is never sent anywhere except to your chosen provider.

If you do not configure AI, the app makes **no network requests** for AI.

## 4. Permissions

The app requests only what features need:

- **Notifications** — budget, recurring, low-balance, and summary alerts (optional).
- **Camera** — to attach receipt photos (optional).
- **Biometric** — for app unlock (optional).
- **Internet** — used **only** for optional cloud AI requests you initiate, and to download fonts / an on-device model if you choose to.

## 5. Data sharing

We do **not** sell, rent, or share your data with anyone. There is no third party with access to your financial data through this app.

## 6. Data export & deletion

- You can **export** your data at any time (CSV / JSON) from Settings → Data.
- Uninstalling the app, or clearing its data in Android settings, **permanently deletes** all locally stored data.

## 7. Children's privacy

Finatra is not directed at children under 13 and does not knowingly collect data from them (it does not collect data at all).

## 8. Changes

This policy may be updated as the app evolves. Material changes will be reflected in this document.

## 9. Contact

For questions about this policy, contact the developer (Jinatra) through the distribution channel where you obtained the app.
