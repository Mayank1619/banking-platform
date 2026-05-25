# Jira Story Importer

This script automates the creation of Jira stories and sub-tasks from a markdown file containing your project tasks and stories.

## Features
- Parses stories and sub-tasks from a markdown file (e.g., `rrsp_tasks.md`).
- Creates Jira stories and sub-tasks (of type "Subtask") in your specified Jira project.
- Supports Atlassian Document Format (ADF) for descriptions (required by Jira Cloud).
- Prints all stories and sub-tasks to the console before sending to Jira, and shows Jira API responses for debugging.

## Prerequisites
- Python 3.x installed and available in your PATH.
- Jira account with API access and permissions to create issues in your target project.
- The following Python packages:
  - `requests`
  - `python-dotenv`

Install dependencies with:
```
pip install requests python-dotenv
```

## Setup
1. **Clone or copy this script and your markdown file (e.g., `rrsp_tasks.md`) into the same or accessible directory.**

2. **Create a `.env` file** in the same directory as the script with the following content:

```
JIRA_URL=https://your-domain.atlassian.net
JIRA_EMAIL=your-email@example.com
JIRA_API_TOKEN=your-api-token
JIRA_PROJECT_KEY=YOURPROJECTKEY
```
- Replace the values with your actual Jira instance details.
- The project key is usually 2-4 uppercase letters (e.g., `SCRUM`, `BANK`).
- You can generate a Jira API token from https://id.atlassian.com/manage/api-tokens

## Usage

From your project root (where the script and `.env` file are located), run:

```
python banking-platform/jira_story_importer.py banking-platform/rrsp_tasks.md
```

- If you omit the markdown file argument, it defaults to `rrsp_tasks.md` in the current directory.
- The script will print all stories and sub-tasks it finds, then attempt to create them in Jira.
- Debug output and Jira API responses will be shown in the console.

## Notes
- The script expects stories to be formatted as markdown headers (e.g., `## Phase 3: US1 — Open RRSP Account`) and sub-tasks as checklist items (e.g., `- [ ] T009 ...`).
- Sub-tasks are created with the issue type name `Subtask`. If your Jira instance uses a different name, update the script accordingly.
- Make sure your Jira user has permission to create stories and sub-tasks in the target project.

## Troubleshooting
- If you see errors about missing modules, install dependencies with `pip install requests python-dotenv`.
- If you see permission or project errors, double-check your `.env` values and Jira permissions.
- If you see ADF errors, ensure you are using the latest script version (ADF support is included).

## License
MIT License (or your preferred license)
