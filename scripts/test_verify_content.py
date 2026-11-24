import json
import unittest
from pathlib import Path

from verify_content import validate_catalog


class CatalogValidationTest(unittest.TestCase):
    def setUp(self):
        root = Path(__file__).resolve().parents[1]
        source = root / "app/src/main/assets/canais.json"
        self.catalog = json.loads(source.read_text(encoding="utf-8"))

    def test_accepts_packaged_catalog(self):
        self.assertEqual([], validate_catalog(self.catalog))

    def test_rejects_duplicate_and_invalid_metadata(self):
        invalid = self.catalog[0] | {"checkedOn": "2026-02-30", "source": ""}
        errors = validate_catalog(self.catalog + [invalid])
        for field in ("id", "checkedOn", "source"):
            self.assertTrue(any(f").{field}:" in error for error in errors), errors)

    def test_rejects_wrong_emergency_number_and_messaging_action(self):
        self.catalog[0].update(phone="192", whatsapp="5511999999999")
        errors = validate_catalog(self.catalog)
        self.assertTrue(any("telefone incompatível" in error for error in errors))
        self.assertTrue(any("ação incompatível" in error for error in errors))

    def test_rejects_required_channel_pending_review(self):
        self.catalog[0]["status"] = "pendente"
        self.assertIn("policia: canal obrigatório precisa de fonte conferida.", validate_catalog(self.catalog))

    def test_rejects_incorrect_types_without_crashing(self):
        self.catalog[0].update(id=[], status={}, phone=190)
        errors = validate_catalog(self.catalog)
        for field in ("id", "status", "phone"):
            self.assertTrue(any(f").{field}:" in error for error in errors), errors)
