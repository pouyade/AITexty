import { chromium } from "playwright-core";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const html = path.join(__dirname, "aitexty-minimal.html");
const outDir = path.join(__dirname, "screenshots");

const ids = [
  ["s01", "01-onboarding-light"],
  ["s02", "02-conversations-light"],
  ["s03", "03-conversations-dark"],
  ["s04", "04-chat-light"],
  ["s05", "05-chat-dark"],
  ["s06", "06-new-message"],
  ["s07", "07-settings-light"],
  ["s08", "08-settings-dark"],
  ["s09", "09-empty-search-dark"],
  ["s10", "10-onboarding-dark"],
];

const browser = await chromium.launch({
  executablePath: "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
  headless: true,
});
const page = await browser.newPage({
  viewport: { width: 1400, height: 900 },
  deviceScaleFactor: 2,
});
await page.goto("file://" + html, { waitUntil: "networkidle" });
await page.waitForTimeout(800);

const fs = await import("fs");
fs.mkdirSync(outDir, { recursive: true });

for (const [id, name] of ids) {
  const phone = page.locator(`#${id} .phone`);
  await phone.scrollIntoViewIfNeeded();
  await phone.screenshot({ path: path.join(outDir, `${name}.png`) });
  console.log("saved", name);
}

await browser.close();
console.log("done");
