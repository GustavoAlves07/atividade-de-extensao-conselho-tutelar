"""Validação local do catálogo Android, usando apenas a biblioteca padrão."""
import argparse
import json
import re
from datetime import date
from pathlib import Path
from urllib.parse import urlsplit

REQUIRED_CHANNELS = {
    "policia": ("policial", "190"),
    "samu": ("medica", "192"),
    "disque100": ("suspeita", "100"),
    "conselho": ("suspeita", None),
}
STATUSES = {"pendente", "verificado_em_fonte_publica", "validado_com_orgao"}
FIELDS = {"id", "name", "category", "phone", "source", "checkedOn", "status", "whatsapp", "website"}


def is_https(value):
    if not isinstance(value, str) or any(char.isspace() for char in value):
        return False
    try:
        url = urlsplit(value)
        return url.scheme == "https" and bool(url.hostname) and not url.username and not url.password
    except ValueError:
        return False


def validate_catalog(catalog):
    if not isinstance(catalog, list):
        return ["O catálogo deve ser uma lista."]

    errors = []
    seen = set()
    available = set()
    for index, channel in enumerate(catalog, start=1):
        if not isinstance(channel, dict):
            errors.append(f"Registro {index}: esperado objeto JSON.")
            continue

        identifier = channel.get("id")
        prefix = f"Registro {index} ({identifier!r})"
        if not isinstance(identifier, str) or not re.fullmatch(r"[a-z][a-z0-9_]*", identifier):
            errors.append(f"{prefix}.id: identificador inválido.")
        elif identifier in seen:
            errors.append(f"{prefix}.id: identificador duplicado.")
        else:
            seen.add(identifier)

        for unknown in sorted(channel.keys() - FIELDS):
            errors.append(f"{prefix}.{unknown}: campo desconhecido.")

        name = channel.get("name")
        if not isinstance(name, str) or not name.strip():
            errors.append(f"{prefix}.name: informe o nome do serviço.")
        if channel.get("category") not in ("policial", "medica", "suspeita"):
            errors.append(f"{prefix}.category: categoria inválida.")
        status = channel.get("status")
        if not isinstance(status, str) or status not in STATUSES:
            errors.append(f"{prefix}.status: estado de validação inválido.")
        phone = channel.get("phone")
        if not isinstance(phone, str) or not re.fullmatch(r"(?:[0-9]{3}|[0-9]{10,11})", phone):
            errors.append(f"{prefix}.phone: informe 3, 10 ou 11 dígitos.")
        if not is_https(channel.get("source")):
            errors.append(f"{prefix}.source: informe o endereço HTTPS da fonte.")
        checked_on = channel.get("checkedOn")
        try:
            if not isinstance(checked_on, str) or date.fromisoformat(checked_on).isoformat() != checked_on:
                raise ValueError
        except ValueError:
            errors.append(f"{prefix}.checkedOn: informe uma data válida no formato AAAA-MM-DD.")

        if "whatsapp" in channel:
            whatsapp = channel["whatsapp"]
            if not isinstance(whatsapp, str) or not re.fullmatch(r"55[0-9]{10,11}", whatsapp):
                errors.append(f"{prefix}.whatsapp: informe DDI 55 e o número com DDD.")
            if channel.get("category") != "suspeita":
                errors.append(f"{prefix}.whatsapp: ação incompatível com a categoria.")
        if "website" in channel and not is_https(channel["website"]):
            errors.append(f"{prefix}.website: informe endereço HTTPS.")

        required = REQUIRED_CHANNELS.get(identifier) if isinstance(identifier, str) else None
        if required:
            category, official_phone = required
            if channel.get("category") != category or (official_phone and phone != official_phone):
                errors.append(f"{prefix}: categoria ou telefone incompatível com o serviço.")
            if status in ("verificado_em_fonte_publica", "validado_com_orgao"):
                available.add(identifier)

    for identifier in sorted(REQUIRED_CHANNELS.keys() - available):
        errors.append(f"{identifier}: canal obrigatório precisa de fonte conferida.")
    return errors


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("catalog", type=Path)
    arguments = parser.parse_args()
    try:
        catalog = json.loads(arguments.catalog.read_text(encoding="utf-8"))
    except (OSError, ValueError) as error:
        parser.exit(1, f"{arguments.catalog}: {error}\n")

    errors = validate_catalog(catalog)
    if errors:
        parser.exit(1, "\n".join(errors) + "\n")
    print(f"Catálogo válido: {len(catalog)} canais.")


if __name__ == "__main__":
    main()
