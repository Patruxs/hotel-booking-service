import { mkdir, writeFile } from "node:fs/promises";
import path from "node:path";
import process from "node:process";

const openApiUrl = process.env.OPENAPI_URL ?? "http://localhost:8080/v3/api-docs";
const outputPath = path.resolve(
  process.env.OPENAPI_OUTPUT ?? "openapi/generated/openapi.json"
);

const controller = new AbortController();
const timeout = setTimeout(() => controller.abort(), 10000);

try {
  const response = await fetch(openApiUrl, {
    headers: { Accept: "application/json" },
    signal: controller.signal,
  });

  if (!response.ok) {
    throw new Error(`Received ${response.status} ${response.statusText} from ${openApiUrl}`);
  }

  const body = await response.text();
  JSON.parse(body);

  await mkdir(path.dirname(outputPath), { recursive: true });
  await writeFile(outputPath, `${body}\n`, "utf8");

  console.log(`Exported OpenAPI spec from ${openApiUrl} to ${outputPath}`);
} catch (error) {
  const detail = error instanceof Error ? error.message : String(error);
  console.error(`OpenAPI export failed: ${detail}`);
  console.error("Start the Spring Boot app first, or set OPENAPI_URL to a reachable /v3/api-docs endpoint.");
  process.exitCode = 1;
} finally {
  clearTimeout(timeout);
}
