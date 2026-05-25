import os
import re
import sys
import requests
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()

JIRA_URL = os.getenv('JIRA_URL')  # e.g., https://your-domain.atlassian.net
JIRA_EMAIL = os.getenv('JIRA_EMAIL')
JIRA_API_TOKEN = os.getenv('JIRA_API_TOKEN')
JIRA_PROJECT_KEY = os.getenv('JIRA_PROJECT_KEY')

HEADERS = {
    "Accept": "application/json",
    "Content-Type": "application/json"
}
AUTH = (JIRA_EMAIL, JIRA_API_TOKEN)

# Regex patterns
STORY_PATTERN = re.compile(r"^## (Phase \d+: US\d+ — .+)", re.MULTILINE)
TASK_PATTERN = re.compile(r"- \[.\] (T\d{3}.*)")

def parse_stories_and_tasks(md_path):
    if not os.path.isfile(md_path):
        print(f"Error: File '{md_path}' not found.")
        sys.exit(1)
    with open(md_path, encoding="utf-8") as f:
        content = f.read()

    stories = []
    for story_match in STORY_PATTERN.finditer(content):
        story_title = story_match.group(1).strip()
        story_start = story_match.end()
        next_story = STORY_PATTERN.search(content, story_start)
        story_end = next_story.start() if next_story else len(content)
        story_block = content[story_start:story_end]
        tasks = [m.group(1).strip() for m in TASK_PATTERN.finditer(story_block)]
        stories.append({"title": story_title, "tasks": tasks})
    return stories

def create_jira_story(summary, description=""):  # Returns issue key
    url = f"{JIRA_URL}/rest/api/3/issue"
    payload = {
        "fields": {
            "project": {"key": JIRA_PROJECT_KEY},
            "summary": summary,
            "description": {
                "type": "doc",
                "version": 1,
                "content": [
                    {
                        "type": "paragraph",
                        "content": [
                            {"type": "text", "text": description or ""}
                        ]
                    }
                ]
            },
            "issuetype": {"name": "Story"}
        }
    }
    print(f"[DEBUG] Sending to Jira (Story): {payload}")
    resp = requests.post(url, json=payload, headers=HEADERS, auth=AUTH)
    print(f"[DEBUG] Jira response (Story): {resp.status_code} {resp.text}")
    resp.raise_for_status()
    return resp.json()["key"]

def create_jira_subtask(summary, parent_key, description=""):
    url = f"{JIRA_URL}/rest/api/3/issue"
    payload = {
        "fields": {
            "project": {"key": JIRA_PROJECT_KEY},
            "parent": {"key": parent_key},
            "summary": summary,
            "description": {
                "type": "doc",
                "version": 1,
                "content": [
                    {
                        "type": "paragraph",
                        "content": [
                            {"type": "text", "text": description or ""}
                        ]
                    }
                ]
            },
            "issuetype": {"name": "Subtask"}
        }
    }
    print(f"[DEBUG] Sending to Jira (Sub-task): {payload}")
    resp = requests.post(url, json=payload, headers=HEADERS, auth=AUTH)
    print(f"[DEBUG] Jira response (Sub-task): {resp.status_code} {resp.text}")
    resp.raise_for_status()
    return resp.json()["key"]

def print_usage():
    print("""
Usage: python jira_story_importer.py [tasks_markdown_file]

Arguments:
  tasks_markdown_file   Path to the markdown file containing stories and tasks (default: rrsp_tasks.md)

Example:
  python jira_story_importer.py rrsp_tasks.md
  python jira_story_importer.py my_next_tasks.md
""")

def main():
    if len(sys.argv) > 2:
        print_usage()
        sys.exit(1)
    md_path = sys.argv[1] if len(sys.argv) == 2 else "rrsp_tasks.md"
    if not os.path.isfile(md_path):
        print(f"Error: File '{md_path}' not found.")
        print_usage()
        sys.exit(1)
    stories = parse_stories_and_tasks(md_path)
    if not stories:
        print(f"No stories found in '{md_path}'. Make sure the file follows the expected format.")
        sys.exit(1)
    print("\n--- Stories and Sub-tasks to be created ---")
    for story in stories:
        print(f"Story: {story['title']}")
        for task in story['tasks']:
            print(f"  Sub-task: {task}")
    print("--- End of List ---\n")

    for story in stories:
        print(f"[JIRA] Creating story: {story['title']}")
        try:
            story_key = create_jira_story(story['title'])
            print(f"[JIRA] Story created: {story_key}")
        except requests.HTTPError as e:
            print(f"[JIRA] Error creating story: {e.response.text}")
            continue
        for task in story['tasks']:
            print(f"  [JIRA] Creating sub-task: {task}")
            try:
                subtask_key = create_jira_subtask(task, story_key)
                print(f"  [JIRA] Sub-task created: {subtask_key}")
            except requests.HTTPError as e:
                print(f"  [JIRA] Error creating sub-task: {e.response.text}")
    print("Done!")

if __name__ == "__main__":
    main()
