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
SERVICE_CATEGORIES = ("protecao_de_direitos", "assistencia_social", "saude", "seguranca")


def is_https(value):
    if not isinstance(value, str) or any(char.isspace() for char in value):
        return False
    try:
        url = urlsplit(value)
        return url.scheme == "https" and bool(url.hostname) and not url.username and not url.password
    except ValueError:
        return False


def is_date(value):
    try:
        return isinstance(value, str) and date.fromisoformat(value).isoformat() == value
    except ValueError:
        return False


def is_text(value):
    return isinstance(value, str) and bool(value.strip())


def is_id(value):
    return isinstance(value, str) and bool(re.fullmatch(r"[a-z][a-z0-9_]*", value))


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
        if not is_id(identifier):
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
        if not is_date(channel.get("checkedOn")):
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


def validate_offline_content(channels, guide, network, editorial):
    errors = []

    def records(document, key, required_text):
        if not isinstance(document, dict) or not isinstance(document.get(key), list):
            errors.append(f"{key}: esperada lista de registros.")
            return []
        entries = []
        seen = set()
        for index, entry in enumerate(document[key], start=1):
            path = f"{key}[{index}]"
            if not isinstance(entry, dict):
                errors.append(f"{path}: esperado objeto JSON.")
                continue
            identifier = entry.get("id")
            if key != "campaigns":
                if not is_id(identifier) or identifier in seen:
                    errors.append(f"{path}.id: identificador inválido ou duplicado.")
                else:
                    seen.add(identifier)
            if not all(is_text(entry.get(field)) for field in required_text):
                errors.append(f"{path}: preencha {', '.join(required_text)}.")
            metadata(entry, path)
            entries.append((path, entry))
        return entries

    def metadata(entry, path):
        status = entry.get("status")
        if not isinstance(status, str) or status not in STATUSES:
            errors.append(f"{path}.status: estado de validação inválido.")
        if not is_date(entry.get("checkedOn")):
            errors.append(f"{path}.checkedOn: informe uma data válida no formato AAAA-MM-DD.")
        sources = entry.get("sources")
        if not isinstance(sources, list) or not sources or any(
            not isinstance(source, dict)
            or not is_text(source.get("name"))
            or not is_https(source.get("url"))
            for source in sources
        ):
            errors.append(f"{path}.sources: inclua nome e URL HTTPS para cada fonte.")

    def links(path, entry, key, destinations):
        ids = entry.get(key, [])
        if not isinstance(ids, list) or any(not is_id(item) or item not in destinations for item in ids):
            errors.append(f"{path}.{key}: referência a registro inexistente.")
        elif entry.get("status") != "pendente" and any(
            destinations[item].get("status") == "pendente" for item in ids
        ):
            errors.append(f"{path}.{key}: referência publicada aponta para registro pendente.")

    for name, document in (("guia", guide), ("rede_protecao", network), ("editorial", editorial)):
        if not isinstance(document, dict) or type(document.get("schemaVersion")) is not int or document["schemaVersion"] != 1:
            errors.append(f"{name}.schemaVersion: versão 1 esperada.")

    articles = records(guide, "articles", ("title", "summary"))
    questions = records(guide, "faq", ("question", "answer"))
    services = records(network, "services", ("name", "summary"))
    topics = records(network, "supportTopics", ("title", "summary"))
    campaigns = records(editorial, "campaigns", ("title", "summary"))
    channel_by_id = {entry["id"]: entry for entry in channels if isinstance(entry, dict) and is_id(entry.get("id"))}
    service_by_id = {entry["id"]: entry for _, entry in services if is_id(entry.get("id"))}
    article_by_id = {entry["id"]: entry for _, entry in articles if is_id(entry.get("id"))}

    for path, article in articles:
        sections = article.get("sections")
        if not isinstance(sections, list) or not sections or any(
            not isinstance(section, dict) or not all(is_text(section.get(key)) for key in ("heading", "body"))
            for section in sections
        ):
            errors.append(f"{path}.sections: inclua título e texto para cada seção.")
        links(path, article, "channelIds", channel_by_id)
        links(path, article, "serviceIds", service_by_id)
    for path, question in questions:
        if "articleId" in question:
            links(path, question | {"articleId": [question["articleId"]]}, "articleId", article_by_id)
    for path, service in services:
        if service.get("category") not in SERVICE_CATEGORIES:
            errors.append(f"{path}.category: categoria inválida.")
        links(path, service, "channelIds", channel_by_id)
        for field, pattern in (("phone", r"(?:[0-9]{3}|[0-9]{10,11})"), ("whatsapp", r"55[0-9]{10,11}")):
            if field in service and (not is_text(service[field]) or not re.fullmatch(pattern, service[field])):
                errors.append(f"{path}.{field}: número inválido.")
        if "website" in service and not is_https(service["website"]):
            errors.append(f"{path}.website: informe uma URL HTTPS.")
        if "address" in service:
            address = service["address"]
            if not isinstance(address, dict) or not all(is_text(address.get(k)) for k in ("street", "city", "state")):
                errors.append(f"{path}.address: inclua logradouro, município e estado.")
            elif any(k in address and not is_text(address[k]) for k in ("number", "district")):
                errors.append(f"{path}.address: número e bairro devem ser textos preenchidos, quando informados.")
        for field in ("openingHours", "coverage", "accessConditions", "accessibility"):
            if field in service and not is_text(service[field]):
                errors.append(f"{path}.{field}: informe um texto preenchido.")
    for path, topic in topics:
        links(path, topic, "serviceIds", service_by_id)
    for path, campaign in campaigns:
        if not is_https(campaign.get("materialUrl")):
            errors.append(f"{path}.materialUrl: informe uma URL HTTPS.")
        for field in ("startsOn", "endsOn"):
            if field in campaign and not is_date(campaign[field]):
                errors.append(f"{path}.{field}: data de vigência inválida.")
        start, end = campaign.get("startsOn"), campaign.get("endsOn")
        if is_date(start) and is_date(end) and start > end:
            errors.append(f"{path}: o início da vigência deve anteceder o fim ou cair na mesma data.")
    corrections = editorial.get("corrections") if isinstance(editorial, dict) else None
    if corrections is not None:
        if not isinstance(corrections, dict) or not is_https(corrections.get("url")):
            errors.append("corrections.url: informe uma URL HTTPS.")
        if isinstance(corrections, dict):
            metadata(corrections, "corrections")

    return errors


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("catalog", type=Path)
    parser.add_argument("--offline", type=Path, nargs=3, metavar=("GUIA", "REDE", "EDITORIAL"))
    arguments = parser.parse_args()
    try:
        paths = [arguments.catalog, *(arguments.offline or [])]
        documents = []
        for path in paths:
            documents.append(json.loads(path.read_text(encoding="utf-8")))
    except (OSError, ValueError) as error:
        parser.exit(1, f"{path}: {error}\n")

    catalog = documents[0]
    errors = validate_catalog(catalog)
    if arguments.offline and not errors:
        errors.extend(validate_offline_content(*documents))
    if errors:
        parser.exit(1, "\n".join(errors) + "\n")
    print(f"Catálogo válido: {len(catalog)} canais.")
    if arguments.offline:
        print("Guia, rede de proteção e conteúdo editorial válidos.")


if __name__ == "__main__":
    main()
