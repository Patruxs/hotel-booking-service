# Agent Instructions

## Project Skills

Use `.codex/skills/harness-intake-griller/SKILL.md` when a request needs
discussion, feature intake, docs, or story shaping before Symphony execution.
The skill is project-scoped; do not use a global copy as the source of truth.

## Spawned Sub-Agent Configuration

Every spawned sub-agent must use `model = "gpt-5.6-sol"`,
`reasoning_effort = "medium"`, `service_tier = "priority"`, and
`fork_turns = "none"`. If the Harness cannot set all four values, do not
spawn the sub-agent; report the capability blocker instead.

<!-- HARNESS:BEGIN -->
## Harness

This repo uses Harness. Before work, read:

- `README.md`
- `docs/HARNESS.md`
- `docs/FEATURE_INTAKE.md`
- `docs/ARCHITECTURE.md`
- `docs/CONTEXT_RULES.md`
- `docs/TOOL_REGISTRY.md`
- `scripts/bin/harness-cli query matrix` on macOS/Linux, or `.\scripts\bin\harness-cli.exe query matrix` on Windows

Use the Rust Harness CLI at `scripts/bin/harness-cli` on macOS/Linux or
`scripts/bin/harness-cli.exe` on Windows as the main operational tool. Before a
step that could use an external tool, run `scripts/bin/harness-cli query tools
--capability <name> --status present` to see what is equipped; an absent
capability is a clean skip.
<!-- HARNESS:END -->

## Codex CLI Code Navigation

When operating from Codex CLI, use `srcwalk` for codebase navigation and exact
evidence before making code claims or edits. Prefer it over ad-hoc `grep`,
`find`, or blind file reads when locating symbols, callers, dependencies, or
changed-code risk.

Common commands:

```bash
srcwalk guide                         # Full embedded, version-matched guide
srcwalk overview                      # Repo orientation and dependency groups
srcwalk discover "<query>"            # Find candidate symbols/usages/text/files
srcwalk discover "*.java" --as file   # Find files by glob
srcwalk context <symbol-or-path:line> # Understand a known target
srcwalk trace callers <symbol>        # Who calls a symbol
srcwalk trace callees <symbol>        # What a symbol calls
srcwalk deps <file>                   # Imports and dependents for a file
srcwalk review staged                 # Review staged changes
srcwalk review working-tree           # Review unstaged/current changes
srcwalk show <path>:<line-range> -C 10 # Read exact source evidence
```

Suggested workflow:

1. Start with `srcwalk overview` for unfamiliar areas.
2. Use `srcwalk discover "natural language query"` when the target is unknown.
3. Use `srcwalk context <symbol-or-path:line>` once a target is known.
4. Use `srcwalk trace callers|callees <symbol>` for impact analysis.
5. Use `srcwalk show <path>:<line-range>` to quote or verify exact code before
   editing or reporting.
6. Use `srcwalk review staged` or `srcwalk review working-tree` before final
   validation when code changed.

If `srcwalk` is not on `PATH`, check whether it is installed under the active
Node prefix, for example:

```bash
command -v srcwalk || find "$HOME/.nvm" -path '*/bin/srcwalk' -type f 2>/dev/null | head
```

<!-- gitnexus:start -->
# GitNexus — Code Intelligence

This project is indexed by GitNexus as **hotel-booking-service** (9487 symbols, 22087 relationships, 300 execution flows). Use the GitNexus MCP tools to understand code, assess impact, and navigate safely.

> Index stale? Run `node .gitnexus/run.cjs analyze` from the project root — it auto-selects an available runner. No `.gitnexus/run.cjs` yet? `npx gitnexus analyze` (npm 11 crash → `npm i -g gitnexus`; #1939).

## Always Do

- **MUST run impact analysis before editing any symbol.** Before modifying a function, class, or method, run `impact({target: "symbolName", direction: "upstream"})` and report the blast radius (direct callers, affected processes, risk level) to the user.
- **MUST run `detect_changes()` before committing** to verify your changes only affect expected symbols and execution flows. For regression review, compare against the default branch: `detect_changes({scope: "compare", base_ref: "master"})`.
- **MUST warn the user** if impact analysis returns HIGH or CRITICAL risk before proceeding with edits.
- When exploring unfamiliar code, use `query({search_query: "concept"})` to find execution flows instead of grepping. It returns process-grouped results ranked by relevance.
- When you need full context on a specific symbol — callers, callees, which execution flows it participates in — use `context({name: "symbolName"})`.
- For security review, `explain({target: "fileOrSymbol"})` lists taint findings (source→sink flows; needs `analyze --pdg`).

## Never Do

- NEVER edit a function, class, or method without first running `impact` on it.
- NEVER ignore HIGH or CRITICAL risk warnings from impact analysis.
- NEVER rename symbols with find-and-replace — use `rename` which understands the call graph.
- NEVER commit changes without running `detect_changes()` to check affected scope.

## Resources

| Resource | Use for |
|----------|---------|
| `gitnexus://repo/hotel-booking-service/context` | Codebase overview, check index freshness |
| `gitnexus://repo/hotel-booking-service/clusters` | All functional areas |
| `gitnexus://repo/hotel-booking-service/processes` | All execution flows |
| `gitnexus://repo/hotel-booking-service/process/{name}` | Step-by-step execution trace |

## CLI

| Task | Read this skill file |
|------|---------------------|
| Understand architecture / "How does X work?" | `.claude/skills/gitnexus/gitnexus-exploring/SKILL.md` |
| Blast radius / "What breaks if I change X?" | `.claude/skills/gitnexus/gitnexus-impact-analysis/SKILL.md` |
| Trace bugs / "Why is X failing?" | `.claude/skills/gitnexus/gitnexus-debugging/SKILL.md` |
| Rename / extract / split / refactor | `.claude/skills/gitnexus/gitnexus-refactoring/SKILL.md` |
| Tools, resources, schema reference | `.claude/skills/gitnexus/gitnexus-guide/SKILL.md` |
| Index, status, clean, wiki CLI commands | `.claude/skills/gitnexus/gitnexus-cli/SKILL.md` |

<!-- gitnexus:end -->
