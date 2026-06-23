#!/usr/bin/env python3
"""
Testar URLs do ANTT para concessionárias sem mapeamento.
GET-only (HEAD é bloqueado pelo gov.br/antt).
"""

import time
import re
import requests
from bs4 import BeautifulSoup

BASE = "https://www.gov.br/antt/pt-br/assuntos/rodovias/concessionarias/lista-de-concessoes"

SLUGS = [
    ("cro", "CRO"),
    ("elovias", "ELOVIAS"),
    ("epr-iguacu", "EPR IGUAÇU"),
    ("litoral-pioneiro", "LITORAL PIONEIRO"),
    ("pantanal", "PANTANAL"),
    ("prvias", "PRVIAS"),
    ("riossp", "RIOSP"),
    ("rodovia-do-aco", "RODOVIA DO AÇO"),
    ("transbrasiliana", "TRANSBRASILIANA"),
    ("via-costeira", "VIA COSTEIRA"),
    ("via-cristais", "VIA CRISTAIS"),
    ("via-mineira", "VIA MINEIRA"),
    ("via-sul", "VIA SUL"),
    ("way-262", "WAY 262"),
]

def test_url(url, name, variant):
    """GET request to test URL."""
    headers = {
        'User-Agent': 'NewRoutes/1.0 (url-tester; anderson@newroutes.app)'
    }

    try:
        resp = requests.get(url, headers=headers, timeout=15, allow_redirects=True)
        status = resp.status_code
        final = str(resp.url)
        soup = BeautifulSoup(resp.text, 'lxml')
        tables = soup.find_all('table')
        first_headers = [h.get_text(strip=True) for h in tables[0].find_all(['th', 'td'])[:5]] if tables else []
        return status, final, tables, first_headers
    except Exception as e:
        return 0, url, [], [str(e)]


def main():
    results = []
    valid_urls = []

    for slug, nome in SLUGS:
        found = False
        for variant_name in ["tarifas-de-pedagio", "tarifas-de-pedagios"]:
            url = f"{BASE}/{slug}/{variant_name}"
            status, final, tables, headers = test_url(url, nome, variant_name)

            if status == 200 and tables:
                print(f"✅ {nome} ({variant_name})")
                print(f"   URL: {final}")
                print(f"   tables={len(tables)} headers={headers[:3]}")
                results.append((nome, final))
                valid_urls.append((nome, final))
                found = True
                break
            else:
                print(f"❌ {nome} ({variant_name}): status={status}, tables={len(tables)}")

        if not found:
            print(f"   (nenhuma variante válida para {nome})")

        time.sleep(1.5)  # delay entre requests

    # Resumo
    print(f"\n{'=' * 70}")
    print("RESUMO — URLs válidas encontradas:")
    print("=" * 70)
    if valid_urls:
        for nome, url in valid_urls:
            print(f'    "{nome}": "{url}",')
    else:
        print("  Nenhuma URL válida encontrada.")

    # Gerar código Python para atualizar CONCESSIONARIA_URLS
    print(f"\n{'=' * 70}")
    print("CÓDIGO PARA COLAR EM CONCESSIONARIA_URLS:")
    print("=" * 70)
    if valid_urls:
        for nome, url in valid_urls:
            print(f'    "{nome}":                  "{url}",')
    else:
        print("  # Nenhuma adição necessária")


if __name__ == "__main__":
    main()
