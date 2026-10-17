import importlib.util
import json
import tempfile
import unittest
import zipfile
from argparse import Namespace
from pathlib import Path


TOOL_PATH = Path(__file__).with_name("zpk_tool.py")
SPEC = importlib.util.spec_from_file_location("template_zpk_tool", TOOL_PATH)
zpk_tool = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(zpk_tool)


class RepositoryIndexTest(unittest.TestCase):
    def test_catalog_only_advertises_binaries_present_in_each_package(self):
        with tempfile.TemporaryDirectory() as temporary_directory:
            root = Path(temporary_directory)
            packages = root / "packages"
            packages.mkdir()
            self.write_package(packages / "desktop-only.zpk", {"jvm": "provider.jar"}, ["provider.jar"])
            self.write_package(packages / "android-only.zpk", {"android": "provider.dex"}, ["provider.dex"])
            self.write_package(
                packages / "dual-target.zpk",
                {"jvm": "provider.jar", "android": "provider.dex"},
                ["provider.jar", "provider.dex"],
            )
            self.write_package(packages / "missing-binary.zpk", {"jvm": "provider.jar"}, [])
            output = root / "index.json"

            zpk_tool.cmd_index(Namespace(repo_dir=str(packages), output=str(output), base_url="https://example.test"))

            providers = {item["id"]: item for item in json.loads(output.read_text())["providers"]}
            self.assertEqual({"desktop-only", "android-only", "dual-target"}, set(providers))
            self.assertIsNone(providers["desktop-only"]["androidArtifact"])
            self.assertIsNotNone(providers["desktop-only"]["desktopArtifact"])
            self.assertIsNotNone(providers["android-only"]["androidArtifact"])
            self.assertIsNone(providers["android-only"]["desktopArtifact"])
            self.assertIsNotNone(providers["dual-target"]["androidArtifact"])
            self.assertIsNotNone(providers["dual-target"]["desktopArtifact"])

    def test_catalog_skips_oversized_package_before_opening_archive(self):
        with tempfile.TemporaryDirectory() as temporary_directory:
            root = Path(temporary_directory)
            packages = root / "packages"
            packages.mkdir()
            oversized_package = packages / "oversized.zpk"
            with oversized_package.open("wb") as package_file:
                package_file.truncate(zpk_tool.MAX_PACKAGE_BYTES + 1)
            output = root / "index.json"

            zpk_tool.cmd_index(Namespace(repo_dir=str(packages), output=str(output), base_url="https://example.test"))

            self.assertEqual([], json.loads(output.read_text())["providers"])

    @staticmethod
    def write_package(path, declared_entries, binaries):
        manifest = {
            "id": path.stem,
            "name": path.stem,
            "entry": declared_entries,
        }
        with zipfile.ZipFile(path, "w") as archive:
            archive.writestr("manifest.json", json.dumps(manifest))
            for binary in binaries:
                archive.writestr(binary, binary.encode())


if __name__ == "__main__":
    unittest.main()
