#!/usr/bin/env python3
"""
Busca URLs de Tarifas de Pedágio nos concessionaires pages do gov.br
e valida se há tabelas com dados de tarifas.
"""

import re
import time
import requests
from pathlib import Path
from bs4 import BeautifulSoup
from unidecode import unidecode

CACHE_DIR = Path("output/cache")
CACHE_DIR.mkdir(exist_ok=True)

HEADERS = {
    "User-Agent": "NewRoutes/1.0 (find-asset-ids; anderson@newroutes.app)"
}

# Concessionárias que precisamos de URLs
CONCESSIONARIAS = [
    "fernao dias",
    "fluminense",
    "litoral sul",
    "regis bittencourt",
    "concer",
]

# Mapeamento de nome (slug) -> URL da página do concessionário no gov.br
GOVBR_CONCES = {
    "fluminense": "https://www.gov.br/antt/pt-br/assuntos/rodovias/concessionarias/lista-de-concessoes/autopista-fluminense",
    "litoral sul": "https://www.gov.br/antt/pt-br/assuntos/rodovias/concessionarias/lista-de-concessoes/autopista-litoral-sul",
    "regis bittencourt": "https://www.gov.br/antt/pt-br/assuntos/rodovias/concessionarias/lista-de-concessoes/autopista-regis-bittencourt",
    "concer": "https://www.gov.br/antt/pt-br/assuntos/rodovias/concessionarias/lista-de-concessoes/concer",
}

# Fernao Dias pode estar em outra URL - vamos buscar na lista completa
LIST_URL = "https://www.gov.br/antt/pt-br/assuntos/rodovias/concessionarias/lista-de-concessoes"


def cache_get(url, prefix):
    """Baixa URL com cache em disco. Retorna (html, was_cached)."""
    safe = re.sub(r'[^a-z0-9]', '_', url[:120])
    cache_file = CACHE_DIR / f"{prefix}_{safe}.html"
    if cache_file.exists():
        return cache_file.read_text(encoding='utf-8', errors='replace'), True
    try:
        resp = requests.get(url, headers=HEADERS, timeout=15)
        resp.raise_for_status()
        html = resp.text
        cache_file.write_text(html, encoding='utf-8')
        time.sleep(1)
        return html, False
    except Exception as e:
        print(f"  [ERRO] {url[:80]}... -> {e}")
        return None, False


def find_tarifa_url(conc_name):
    """
    Encontra a URL da página de 'Tarifas de Pedagio' de uma concessionaria
    buscando na lista de concessionarios do gov.br.
    """
    # Se ja temos mapeamento, usa direto
    if conc_name in GOVBR_CONCES:
        return GOVBR_CONCES[conc_name], "govbr_concess"

    # Para fernao dias, busca na lista
    if conc_name == "fernao dias":
        html, _ = cache_get(LIST_URL, "list")
        if html:
            soup = BeautifulSoup(html, 'lxml')
            for a in soup.find_all('a', href=True):
                text = a.get_text(strip=True).lower()
                if 'fernao' in text or 'arteris' in text:
                    conc_url = a['href']
                    if not conc_url.startswith('http'):
                        conc_url = 'https://www.gov.br/antt' + conc_url
                    return conc_url, "govbr_list_fernao"

    return None, None


def find_tarifa_link(parent_url):
    """
    Dada a URL da pagina da concessionaria, encontra o link 'Tarifas de Pedagio'.
    """
    html, _ = cache_get(parent_url, "parent")
    if html is None:
        return None, None

    soup = BeautifulSoup(html, 'lxml')
    for a in soup.find_all('a', href=True):
        text = unidecode(a.get_text(strip=True)).lower()
        if 'tarifa' in text and 'pedagio' in text:
            href = a['href']
            if not href.startswith('http'):
                href = 'https://www.gov.br/antt' + href
            return href, text

    return None, None


def validate_tarifa_page(url, source):
    """
    Valida se a URL da pagina de tarifas tem tabelas e contem 'tarifa'.
    Retorna (has_tables, num_tables, num_iframes, table_headers, redirect_url, html_size).
    """
    html, _ = cache_get(url, "tarifa_" + source)
    if html is None:
        return None

    soup = BeautifulSoup(html, 'lxml')
    tables = soup.find_all('table')
    iframes = soup.find_all('iframe')

    has_tarifa = "tarifa" in html.lower()
    for table in tables:
        if "tarifa" in table.get_text().lower():
            has_tarifa = True
            break

    # Check JS redirect
    loc = re.search(r'window\.location\.href\s*=\s*["\x27]([^"\x27]+)["\x27]', html)
    redirect_url = loc.group(1) if loc else None

    # Get table headers if exists
    table_headers = []
    if tables:
        for table in tables[:3]:
            # Prefer th, fall back to first tr's cells
            ths = [th.get_text(strip=True)[:50] for th in table.find_all('th', limit=3)]
            if not ths:
                ths = [th.get_text(strip=True)[:50] for th in table.find_all(['th', 'td'], limit=3)]
            table_headers.append(ths)

    html_size = len(html.encode('utf-8'))
    return {
        'has_tables': has_tarifa and len(tables) > 0,
        'num_tables': len(tables),
        'num_iframes': len(iframes),
        'headers': table_headers,
        'redirect_url': redirect_url,
        'html_size': html_size,
    }


def main():
    print("=" * 75)
    print("BUSCA DE URLS DE TARIFAS - CONCESSIONARIAS SEM TABELA")
    print("=" * 75)

    results = {}

    for conc_slug in CONCESSIONARIAS:
        print(f"\n{'─' * 75}")
        print(f"  {conc_slug.upper()}")

        # Step 1: Find the concessionaire's main page on gov.br
        parent_url, source = find_tarifa_url(conc_slug)
        if parent_url is None:
            print(f"  [SKIP] Pagina da concessionaria nao encontrada")
            continue

        print(f"  Parent page: {parent_url[:120]}...")

        # Step 2: Find the 'Tarifas de Pedagio' link
        tarifa_url, link_text = find_tarifa_link(parent_url)
        if tarifa_url is None:
            print(f"  [SKIP] Link 'Tarifas de Pedagio' nao encontrado na pagina")
            continue

        print(f"  Link encontrado: {link_text} -> {tarifa_url[:120]}...")

        # Step 3: Validate the tarifa page
        result = validate_tarifa_page(tarifa_url, source)
        if result is None:
            print(f"  [SKIP] Falha ao acessar pagina de tarifas")
            continue

        has_tables = result['has_tables']
        num_tables = result['num_tables']
        num_iframes = result['num_iframes']
        headers = result['headers']
        redirect_url = result['redirect_url']
        html_size = result['html_size']

        print(f"  Tables: {num_tables}, Iframes: {num_iframes}")
        print(f"  Has 'tarifa': {has_tables}")

        if redirect_url:
            print(f"  JS redirects to: {redirect_url[:120]}")
            # Validate redirect target
            r_result = validate_tarifa_page(redirect_url, "redirect")
            if r_result and r_result['has_tables']:
                r_tables = r_result['num_tables']
                r_headers = r_result['headers']
                print(f"  Redirect target: {r_tables} tabela(s)")
                if r_headers:
                    print(f"  Redirect headers: {r_headers[0]}")
                tarifa_url = redirect_url
                has_tables = True
                num_tables = r_tables
                headers = r_headers

        if has_tables:
            print(f"\n  *** VALIDADO: {num_tables} tabela(s)")
            if headers:
                for i, h in enumerate(headers):
                    print(f"     Tabela {i+1} headers: {h}")
            results[conc_slug] = tarifa_url
        else:
            print(f"  [!] Sem tabelas - talvez a pagina exija JavaScript")

    # Final report
    print(f"\n{'=' * 75}")
    print("RESUMO - URLS PARA COLOCAR NO CONCESSIONARIA_URLS")
    print("=" * 75)

    all_concs = ["AUTOPISTA FERNAO DIAS", "AUTOPISTA FLUMINENSE",
                 "AUTOPISTA LITORAL SUL", "AUTOPISTA REGIS BITTENCOURT", "CONCER"]
    for conc in all_concs:
        lookup = conc.lower()
        if "fernao" in lookup:
            lookup = "fernao dias"
        elif "fluminense" in lookup:
            lookup = "fluminense"
        elif "litoral sul" in lookup:
            lookup = "litoral sul"
        elif "regis" in lookup:
            lookup = "regis bittencourt"
        elif "concer" in lookup:
            lookup = "concer"

        if lookup in results:
            print(f'    "{conc}":          "{results[lookup]}",')
        else:
            print(f'    "{conc}":          # ???')

    print("=" * 75)


if __name__ == "__main__":
    main()
