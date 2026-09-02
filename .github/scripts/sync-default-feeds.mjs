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

function stripJsoncComments(input) {
  let output = "";
  let inString = false;
  let escaped = false;

  for (let i = 0; i < input.length; i += 1) {
    const char = input[i];
    const next = input[i + 1];

    if (inString) {
      output += char;
      if (escaped) {
        escaped = false;
      } else if (char === "\\") {
        escaped = true;
      } else if (char === '"') {
        inString = false;
      }
      continue;
    }

    if (char === '"') {
      inString = true;
      output += char;
      continue;
    }

    if (char === "/" && next === "/") {
      while (i < input.length && input[i] !== "\n") i += 1;
      output += "\n";
      continue;
    }

    if (char === "/" && next === "*") {
      i += 2;
      while (i < input.length && !(input[i] === "*" && input[i + 1] === "/")) {
        if (input[i] === "\n") output += "\n";
        i += 1;
      }
      i += 1;
      continue;
    }

    output += char;
  }

  return output;
}

function stripTrailingCommas(input) {
  let output = "";
  let inString = false;
  let escaped = false;

  for (let i = 0; i < input.length; i += 1) {
    const char = input[i];

    if (inString) {
      output += char;
      if (escaped) {
        escaped = false;
      } else if (char === "\\") {
        escaped = true;
      } else if (char === '"') {
        inString = false;
      }
      continue;
    }

    if (char === '"') {
      inString = true;
      output += char;
      continue;
    }

    if (char === ",") {
      let nextIndex = i + 1;
      while (/\s/.test(input[nextIndex] ?? "")) nextIndex += 1;
      if (input[nextIndex] === "}" || input[nextIndex] === "]") continue;
    }

    output += char;
  }

  return output;
}

const wrangler = await readFile(wranglerPath, "utf8");
const config = JSON.parse(stripTrailingCommas(stripJsoncComments(wrangler)));
const defaultFeeds = config?.vars?.DEFAULT_FEEDS;
if (typeof defaultFeeds !== "string") {
  throw new Error("worker/wrangler.jsonc does not define string vars.DEFAULT_FEEDS");
}

const feeds = defaultFeeds
  .split(",")
  .map((feed) => feed.trim())
  .filter(Boolean);

if (feeds.length === 0) {
  throw new Error("vars.DEFAULT_FEEDS must contain at least one feed");
}
if (feeds.length > 12) {
  throw new Error("vars.DEFAULT_FEEDS must contain at most 12 feeds");
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
