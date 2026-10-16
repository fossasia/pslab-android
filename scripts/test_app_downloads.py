"""Exercise artifact publication without contacting GitHub or building Flutter."""

import json
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import textwrap
import unittest

ROOT = Path(__file__).resolve().parents[1]


class AppDownloadsTest(unittest.TestCase):
    def test_existing_raw_download_links(self):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)
            (output / "app-release.apk").touch()
            subprocess.run(
                ["bash", str(ROOT / "scripts/generate-app-download-page.sh"), "fossasia/pslab-app", directory],
                check=True,
                capture_output=True,
            )
            self.assertIn(
                'href="https://github.com/fossasia/pslab-app/raw/app/app-release.apk"',
                (output / "index.html").read_text(),
            )

    def publish(self, fail_release=False):
        temporary = tempfile.TemporaryDirectory()
        self.addCleanup(temporary.cleanup)
        root = Path(temporary.name)
        checkout = root / "checkout with spaces"
        checkout.mkdir()
        scripts = checkout / "scripts"
        scripts.mkdir()
        shutil.copy(ROOT / "scripts/generate-app-download-page.sh", scripts)
        artifacts = checkout / "artifacts" / "APK Generated"
        artifacts.mkdir(parents=True)
        with (artifacts / "app-release.apk").open("wb") as artifact:
            artifact.truncate(106 * 1024 * 1024)
        (artifacts / "app-debug.apk").touch()
        (artifacts / "app-profile.apk").touch()
        (artifacts / "ignored.txt").touch()
        for suffix in ("aab", "ipa", "exe", "rpm", "dmg", "zip"):
            (artifacts / f"release.{suffix}").touch()
        (artifacts / "PSLab 4.58.2+738 & Linux.deb").touch()

        home = root / "home"
        home.mkdir()
        environment = dict(os.environ, HOME=str(home), GIT_CONFIG_NOSYSTEM="1")
        remote = root / "remote.git"
        subprocess.run(["git", "init", "--bare", str(remote)], check=True, capture_output=True)
        seed = root / "seed"
        subprocess.run(["git", "init", "-b", "app", str(seed)], check=True, capture_output=True)
        self.git(seed, "config", "user.name", "Test")
        self.git(seed, "config", "user.email", "test@example.invalid")
        (seed / "README.md").write_text("Downloads\n")
        (seed / "old.apk").touch()
        self.git(seed, "add", ".")
        self.git(seed, "commit", "-m", "Seed")
        self.git(seed, "push", str(remote), "app")
        before = self.git(remote, "rev-parse", "app").strip()
        hook = remote / "hooks" / "pre-receive"
        hook.write_text(textwrap.dedent("""\
            #!/usr/bin/env bash
            while read -r old new ref; do
              if git ls-tree -r -l "$new" | awk '$4 > 104857600 {bad=1} END {exit !bad}'; then
                echo 'Blob exceeds GitHub limit' >&2
                exit 1
              fi
            done
            """))
        hook.chmod(0o755)
        binaries = root / "bin"
        binaries.mkdir()
        gh = binaries / "gh"
        gh.write_text(textwrap.dedent("""\
            #!/usr/bin/env python3
            import json, os, pathlib, sys
            pathlib.Path(os.environ['GH_RECORD']).write_text(json.dumps(sys.argv[1:]))
            if os.environ.get('FAIL_RELEASE') == '1':
                sys.exit(1)
            if not any(pathlib.Path(arg).is_file() for arg in sys.argv[4:]):
                sys.exit(1)
            """))
        gh.chmod(0o755)
        workflow = (ROOT / ".github/workflows/push-event.yml").read_text()
        step = workflow.split("      - name: Publish artifacts to app branch\n", 1)[1]
        command = textwrap.dedent(step.split("        run: |\n", 1)[1])
        command = command.replace(
            "https://${{ github.repository_owner }}:${{ github.token }}@github.com/${{ github.repository }}",
            str(remote),
        )
        command = command.replace("${{ github.repository }}", "fossasia/pslab-app")
        environment.update(
            PATH=f"{binaries}:{os.environ['PATH']}",
            GH_RECORD=str(root / "gh.json"),
            FAIL_RELEASE="1" if fail_release else "0",
            GITHUB_REPOSITORY="fossasia/pslab-app",
            GITHUB_SHA="0123456789012345678901234567890123456789",
            GITHUB_RUN_ID="42",
            GITHUB_RUN_ATTEMPT="2",
            GITHUB_RUN_NUMBER="73",
            GITHUB_HEAD_REF="",
            GITHUB_REF="refs/heads/main",
        )
        result = subprocess.run(["bash", "-c", command], cwd=checkout, env=environment, capture_output=True, text=True)
        return root, checkout, remote, before, result

    @staticmethod
    def git(directory, *arguments):
        return subprocess.check_output(["git", "-C", str(directory), *arguments], stderr=subprocess.PIPE, text=True)

    def test_large_binaries_are_released_and_only_index_is_committed(self):
        root, checkout, remote, _, result = self.publish()
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        files = self.git(remote, "ls-tree", "--name-only", "app").splitlines()
        self.assertEqual(files, ["README.md", "index.html"])
        page = self.git(remote, "show", "app:index.html")
        self.assertIn("releases/download/development-42-2/pslab-main-release.apk", page)
        self.assertIn("PSLab%204.58.2%2B738%20%26%20Linux.deb", page)
        self.assertIn("PSLab 4.58.2+738 &amp; Linux.deb", page)
        self.assertNotIn("app-debug", page)
        self.assertNotIn("app-profile", page)
        self.assertNotIn("old.apk", page)
        for suffix in ("aab", "ipa", "exe", "rpm", "dmg", "zip"):
            self.assertIn(f"/release.{suffix}", page)
        arguments = json.loads((root / "gh.json").read_text())
        self.assertEqual(arguments[:3], ["release", "create", "development-42-2"])
        self.assertIn("--prerelease", arguments)
        self.assertIn("--latest=false", arguments)
        self.assertEqual(arguments[arguments.index("--target") + 1], "0123456789012345678901234567890123456789")
        self.assertEqual((checkout / "release-assets/pslab-main-release.apk").stat().st_size, 106 * 1024 * 1024)

    def test_failed_upload_does_not_publish_broken_download_page(self):
        _, _, remote, before, result = self.publish(fail_release=True)
        self.assertNotEqual(result.returncode, 0)
        self.assertEqual(self.git(remote, "rev-parse", "app").strip(), before)

    def test_development_releases_cannot_promote_store_releases(self):
        workflow = (ROOT / ".github/workflows/release.yml").read_text()
        condition = workflow.split("    if:", 1)[1].splitlines()[0]
        self.assertIn("!github.event.release.prerelease", condition)
        self.assertIn("!github.event.release.draft", condition)


if __name__ == "__main__":
    unittest.main()
