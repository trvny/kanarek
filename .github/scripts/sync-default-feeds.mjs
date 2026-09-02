#!/usr/bin/env node

import { readFile, writeFile } from "node:fs/promises";
import { resolve } from "node:path";

const root = process.cwd();
const wranglerPath = resolve(root, "worker/wrangler.jsonc");
const repositoryPath = resolve(
  root,
  "app/src/main/java/com/kanarek/data/NewsRepository.kt",
);
const checkOnly = process.argv.includes("--check");

const wrangler = await readFile(wranglerPath, "utf8");
const match = wrangler.match(/"DEFAULT_FEEDS"\s*:\s*("(?:\\.|[^"\\])*")/);
if (!match) {
  throw new Error("worker/wrangler.jsonc does not define vars.DEFAULT_FEEDS");
}

const feeds = JSON.parse(match[1])
  .split(",")
  .map((feed) => feed.trim())
  .filter(Boolean);

if (feeds.length === 0) {
  throw new Error("vars.DEFAULT_FEEDS must contain at least one feed");
}
if (new Set(feeds).size !== feeds.length) {
  throw new Error("vars.DEFAULT_FEEDS contains duplicate URLs");
}
for (const feed of feeds) {
  const url = new URL(feed);
  if (url.protocol !== "http:" && url.protocol !== "https:") {
    throw new Error(`Default feed must use HTTP(S): ${feed}`);
  }
}

const kotlinString = (value) =>
  `"${value.replaceAll("\\", "\\\\").replaceAll('"', '\\"').replaceAll("$", "\\$")}"`;

const repository = await readFile(repositoryPath, "utf8");
const blockPattern =
  /        val DEFAULT_FEEDS =\n            listOf\(\n(?:                "(?:\\.|[^"\\])*",\n)+            \)/;
const currentBlock = repository.match(blockPattern)?.[0];
if (!currentBlock) {
  throw new Error("NewsRepository.kt does not contain the expected DEFAULT_FEEDS block");
}

const generatedBlock = `        val DEFAULT_FEEDS =
            listOf(
${feeds.map((feed) => `                ${kotlinString(feed)},`).join("\n")}
            )`;
const updatedRepository = repository.replace(blockPattern, generatedBlock);

if (checkOnly) {
  if (updatedRepository !== repository) {
    console.error(
      "NewsRepository.DEFAULT_FEEDS is out of sync with worker/wrangler.jsonc. " +
        "Run: node .github/scripts/sync-default-feeds.mjs",
    );
    process.exitCode = 1;
  } else {
    console.log(`Default feeds are in sync (${feeds.length}).`);
  }
} else if (updatedRepository !== repository) {
  await writeFile(repositoryPath, updatedRepository);
  console.log(`Synced ${feeds.length} default feeds into NewsRepository.kt.`);
} else {
  console.log(`Default feeds already in sync (${feeds.length}).`);
}
