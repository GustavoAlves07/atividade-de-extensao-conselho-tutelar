import json
import unittest
from pathlib import Path

from verify_content import validate_catalog, validate_offline_content


ASSETS = Path(__file__).resolve().parents[1] / "app/src/main/assets"


def read_asset(name):
    return json.loads((ASSETS / f"{name}.json").read_text(encoding="utf-8"))


class CatalogValidationTest(unittest.TestCase):
    def setUp(self):
        self.catalog = read_asset("canais")

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


class OfflineContentValidationTest(unittest.TestCase):
    def setUp(self):
        self.channels = read_asset("canais")
        self.guide = read_asset("guia")
        self.network = read_asset("rede_protecao")
        self.editorial = read_asset("editorial")

    def validate(self):
        return validate_offline_content(self.channels, self.guide, self.network, self.editorial)

    def test_accepts_packaged_content(self):
        self.assertEqual([], self.validate())

    def test_rejects_published_reference_to_pending_or_missing_service(self):
        self.network["services"][1]["status"] = "pendente"
        self.guide["faq"][0]["articleId"] = "inexistente"
        errors = self.validate()
        self.assertTrue(any("articleId: referência a registro inexistente" in error for error in errors), errors)
        self.assertTrue(any("serviceIds: referência publicada aponta para registro pendente" in error for error in errors), errors)

    def test_rejects_bad_campaign_dates_and_insecure_editorial_destination(self):
        self.editorial["campaigns"] = [{
            "title": "Campanha fictícia",
            "summary": "Material de teste.",
            "materialUrl": "https://example.org/material",
            "sources": [{"name": "Fonte fictícia", "url": "https://example.org/fonte"}],
            "status": "validado_com_orgao",
            "checkedOn": "2026-01-01",
            "startsOn": "2026-02-01",
            "endsOn": "2026-01-01",
        }]
        self.editorial["corrections"] = self.editorial["campaigns"][0] | {"url": "http://example.org/"}
        errors = self.validate()
        self.assertTrue(any("vigência" in error for error in errors), errors)
        self.assertIn("corrections.url: informe uma URL HTTPS.", errors)
