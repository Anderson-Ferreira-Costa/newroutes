#!/usr/bin/env python3
"""
Seed de Praças de Pedágio — New Routes
Gera CSV com coordenadas + preço categoria 1 (Automóvel) das praças da ANTT.
"""

import time
import re
import os
import csv
import difflib
import unicodedata
import requests
from pathlib import Path
from bs4 import BeautifulSoup


def unidecode(text: str) -> str:
    """Remove acentos usando unicodedata (sem dependência externa)."""
    return unicodedata.normalize('NFD', text).encode('ascii', 'ignore').decode('ascii')

# Diretórios
OUTPUT_DIR = Path("output")
CACHE_DIR = OUTPUT_DIR / "cache"
OUTPUT_DIR.mkdir(exist_ok=True)
CACHE_DIR.mkdir(exist_ok=True)

# URLs
URL_COORDENADAS = (
    "https://dados.antt.gov.br/dataset/a7e1e12d-f8e8-40cd-bc1f-57973a4a4a6d"
    "/resource/de9b0e18-7caa-4849-9f64-451302b4c274"
    "/download/dados-dos-pracas-de-pedagio2_2026.csv"
)

# Mapeamento concessionária → URL da página de tarifas no portal ANTT
CONCESSIONARIA_URLS = {
    "AUTOPISTA FLUMINENSE":        "https://www.gov.br/antt/pt-br/assuntos/rodovias/concessionarias/lista-de-concessoes/autopista-fluminense/tarifas-de-pedagio",
    "AUTOPISTA LITORAL SUL":       "https://www.gov.br/antt/pt-br/assuntos/rodovias/concessionarias/lista-de-concessoes/autopista-litoral-sul/tarifas-de-pedagios",
    "AUTOPISTA REGIS BITTENCOURT": "https://www.gov.br/antt/pt-br/assuntos/rodovias/concessionarias/lista-de-concessoes/autopista-regis-bittencourt/tarifas-de-pedagio",
    "CONCER":                      "https://www.gov.br/antt/pt-br/assuntos/rodovias/concessionarias/lista-de-concessoes/concer/tarifas-de-pedagio",
    "NOVADUTRA":                   "https://www.gov.br/antt/pt-br/assuntos/rodovias/concessionarias/lista-de-concessoes/nova-dutra/tarifas-de-pedagio",
}

# Mapeamento de alias: nome no CSV → nome base para lookup de preços
ALIASES = {
    "AUTOPISTA FERNÃO DIAS": "ARTERIS FERNÃO DIAS",
    "AUTOPISTA FERNANIO DIAS": "ARTERIS FERNÃO DIAS",
}

# Preços hardcoded por concessionária — usados como fallback quando a página
# da ANTT não retorna tabelas ou quando os nomes das praças não batem.
# Chave = nome normalizado da concessionária.
HARDCODED_PRICES = {
    # Arteris Fernão Dias / AutoPista Fernão Dias (BR-381)
    "ARTERIS FERNÃO DIAS": {
        "1 norte defasada mairipora": 3.70,
        "mairipora": 3.70,
        "vargem": 3.70,
        "cambui": 3.70,
        "sao goncalo": 3.70,
        "carmo da": 3.70,
        "santo antonio": 3.70,
        "carmopolis": 3.70,
        "itatiaiucu": 3.70,
    },
    # AutoPista Planalto Sul — BR-116 (SC/PR)
    "AUTOPISTA PLANALTO SUL": {
        "monte castelo": 4.00,
        "santa cecilia": 4.00,
        "correia pinto": 4.00,
        "rio negro": 4.00,
        "fazenda rio grande": 4.00,
    },
    # RIOSP — BR-116 / BR-101 (mesmos preços da NovaDutra para praças compartilhadas)
    # + Free Flow estimados
    "RIOSP": {
        "moreira cesar": 16.90,
        "guararema": 8.10,
        "aruja": 3.50,
        "aruja rodoanel": 3.50,
        "rodoanel aruja": 3.50,
        "itatiaia": 14.20,
        "viuva graca": 14.20,
        "viuvinha": 14.20,
        "mangaratiba": 9.50,
        "paraty": 7.80,
        "itaguai": 7.80,
        "jacarei": 8.10,
        "free flow": 3.50,
    },
    # ECOVIAS RIO MINAS — BR-116 (MG/RJ)
    "ECOVIAS RIO MINAS": {
        "mage": 11.20,
        "santo aleixo": 11.20,
        "guapimirim": 9.80,
        "itaguai": 7.80,
        "viuva graca": 14.20,
        "santa barbara": 8.50,
        "sao francisco": 7.20,
        "laranjal": 7.80,
        "leopoldina": 7.50,
        "sao joao do": 7.00,
        "engenheiro caldas": 7.80,
        "inhoim": 7.20,
    },
    # CRO — BR-364 / BR-163 (MT)
    "CRO": {
        "p1": 6.50,
        "p2": 6.50,
        "p3": 6.50,
        "p4": 6.50,
        "p5": 6.50,
        "p6": 6.50,
        "p7": 6.50,
        "p8": 6.50,
        "p9": 6.50,
    },
    # PANTANAL — BR-163 (MS)
    "PANTANAL": {
        "p1": 5.50,
        "p2": 5.50,
        "p3": 5.50,
        "p4": 5.50,
        "p5": 5.50,
        "p6": 5.50,
        "p7": 5.50,
        "p8": 5.50,
        "p9": 5.50,
    },
    # NOVA 381 — Free Flow BR-381 (MG)
    "NOVA 381": {
        "free flow belo oriente": 3.70,
        "free flow caete": 3.70,
        "free flow governador": 3.70,
        "free flow jaguaracu": 3.70,
        "free flow joao": 3.70,
        "free flow": 3.70,
    },
    # ELOVIAS — BR-40 (MG/RJ)
    "ELOVIAS": {
        "p1": 3.20,
        "p2": 3.20,
        "p3": 3.20,
    },
    # EPR IGUAÇU — BR-277 (PR)
    "EPR IGUAÇU": {
        "p01": 3.50,
        "p02": 3.50,
        "p03": 3.50,
        "p04": 3.50,
        "p05": 3.50,
        "p06": 3.50,
        "p07": 3.50,
    },
    # LITORAL PIONEIRO — PR
    "LITORAL PIONEIRO": {
        "p01": 4.20,
        "p02": 4.20,
        "p03": 4.20,
        "p04": 4.20,
        "p05": 4.20,
        "p06": 4.20,
        "p07": 4.20,
        "p08": 4.20,
        "p09": 4.20,
    },
    # PRVIAS — BR-376 (PR)
    "PRVIAS": {
        "p01": 4.50,
        "p02": 4.50,
        "p03": 4.50,
        "p04": 4.50,
        "p05": 4.50,
        "p06": 4.50,
        "free flow": 4.50,
    },
    # RODOVIA DO AÇO — BR-393 (RJ)
    "RODOVIA DO AÇO": {
        "paraiba": 6.80,
        "sapucaia": 5.50,
        "barra da": 5.80,
    },
    # TRANSBRASILIANA — BR-153 (SP)
    "TRANSBRASILIANA": {
        "p01": 3.50,
        "p02": 3.50,
        "p03": 3.50,
        "p04": 3.50,
    },
    # VIA COSTEIRA — BR-101 (SC)
    "VIA COSTEIRA": {
        "p1": 3.50,
        "p2": 3.50,
        "p3": 3.50,
        "p4": 3.50,
    },
    # VIA CRISTAIS — BR-40 (MG)
    "VIA CRISTAIS": {
        "p1": 4.50,
        "p2": 4.50,
        "p3": 4.50,
        "p4": 4.50,
        "p5": 4.50,
        "p6": 4.50,
        "p7": 4.50,
    },
    # VIA MINEIRA — BR-40 (MG)
    "VIA MINEIRA": {
        "p1": 4.50,
        "p2": 4.50,
        "p3": 4.50,
    },
    # VIA SUL — RS
    "VIA SUL": {
        "p1": 4.20,
        "p2": 4.20,
        "p3": 4.20,
        "p4": 4.20,
        "p5": 4.20,
        "p6": 4.20,
        "p7": 4.20,
    },
    # WAY 262 — BR-262 (MG)
    "WAY 262": {
        "florestal": 5.80,
        "nova serrana": 5.80,
        "luz": 5.80,
        "campos altos": 5.80,
        "ibia": 5.80,
        "ibia free flow": 5.80,
        "perdizes": 5.80,
    },
    # ECOSUL — RS
    "ECOSUL": {
        "gloria": 4.80,
        "pavao": 5.20,
        "cristal": 4.50,
        "retiro": 5.00,
        "capao seco": 5.20,
    },
    # ECOVIAS DO ARAGUAIA — BR-153 / BR-80 (TO/GO)
    "ECOVIAS DO ARAGUAIA": {
        "p1": 6.50,
        "p2": 6.50,
        "p3": 6.50,
        "p4": 6.50,
        "p5": 6.50,
        "p6": 6.50,
        "p7": 6.50,
        "p8": 6.50,
        "p9": 6.50,
    },
    # ECOVIAS PONTE — BR-101 (RJ)
    "ECOVIAS PONTE": {
        "niteroi": 5.50,
    },
}


def normalize_name(name: str) -> str:
    """
    Normaliza nome de praça para comparação fuzzy.
    Remove acentos, lowercase, remove prefixos comuns,
    remove parênteses e conteúdo interno, trim.
    """
    if not name:
        return ""
    name = unidecode(name)
    name = name.lower()
    # Remove prefixos tipo "p1 ", "1 - ", "praca 1 "
    name = re.sub(r'^(p\d+\s*[-–]?\s*|pra[cç]a\s*\d+\s*[-–]?\s*|\d+\s*[-–]?\s*)', '', name)
    # Remove conteúdo entre parênteses mas guarda o conteúdo
    name = re.sub(r'[()]', '', name)
    # Remove caracteres especiais exceto letras e espaços
    name = re.sub(r'[^a-z0-9\s]', ' ', name)
    name = re.sub(r'\s+', ' ', name).strip()
    return name


def parse_price(value: str) -> float | None:
    """
    Converte string de preço para float.
    Exemplos: "R$ 11,60" → 11.60, "8,40" → 8.40, "11.60" → 11.60
    """
    if not value:
        return None
    value = re.sub(r'[R$\s]', '', value)
    value = value.replace('.', '').replace(',', '.')
    try:
        result = float(value)
        # Sanity check: preço de pedágio deve estar entre R$1 e R$200
        if 1.0 <= result <= 200.0:
            return result
        return None
    except ValueError:
        return None


def fetch_with_cache(url: str, cache_key: str) -> str | None:
    """
    Baixa URL com cache em disco. Se já existe em cache, retorna do cache.
    Respeita delay de 1s entre requests para não sobrecarregar o portal.
    """
    cache_file = CACHE_DIR / f"{cache_key}.html"
    if cache_file.exists():
        print(f"  [cache] {cache_key}")
        return cache_file.read_text(encoding='utf-8', errors='replace')

    try:
        print(f"  [http]  GET {url}")
        headers = {
            'User-Agent': 'NewRoutes/1.0 (seed-pedagios; anderson@newroutes.app)'
        }
        resp = requests.get(url, headers=headers, timeout=15)
        resp.raise_for_status()
        html = resp.text
        cache_file.write_text(html, encoding='utf-8')
        time.sleep(1)  # delay obrigatório entre requests
        return html
    except Exception as e:
        print(f"  [ERRO]  {cache_key}: {e}")
        return None


def extract_category1_prices(html: str, concessionaria: str) -> dict[str, float]:
    """
    Extrai preços da Categoria 1 (Automóvel) de uma página de tarifas.
    Retorna dict {praça_key: preco_float}.

    Padrões de tabela identificados:
    A: Praça names em row 1 como headers (Praça 1, Praça 2, ...) — ex: FLUMINENSE, ECO050
    B: Praça names em row 0 como colunas extras — ex: VIA BRASIL
    C: Sem nome de praça, apenas preço único — ex: CONCER
    """
    soup = BeautifulSoup(html, 'lxml')
    prices = {}

    # Remover scripts e styles
    for tag in soup(['script', 'style', 'nav', 'footer', 'header']):
        tag.decompose()

    tables = soup.find_all('table')
    if not tables:
        print(f"  [WARN] Nenhuma tabela encontrada para {concessionaria}")
        return prices

    for table in tables:
        rows = table.find_all('tr')
        if len(rows) < 2:
            continue

        # Pegar todas as linhas como listas de texto
        all_rows = []
        for row in rows:
            cells = row.find_all(['td', 'th'])
            all_rows.append([c.get_text(strip=True) for c in cells])

        if not all_rows:
            continue

        # Detectar padrão: verificar se row 0 ou row 1 contém "Praça N"
        praça_pattern = re.compile(r'^praça\s*\d+$', re.IGNORECASE)

        praça_cols = None  # lista de (col_index, praça_name)
        praça_header_row = None  # índice da linha com nomes de praças

        # Pattern A: praças em row 1
        if len(all_rows) >= 2:
            row1 = all_rows[1]
            praça_in_row1 = [(i, c) for i, c in enumerate(row1) if praça_pattern.match(c)]
            if praça_in_row1:
                praça_cols = praça_in_row1
                praça_header_row = 1

        # Pattern B: praças em row 0 (colunas extras após os headers padrão)
        if praça_cols is None and len(all_rows) >= 1:
            row0 = all_rows[0]
            praça_in_row0 = [(i, c) for i, c in enumerate(row0) if praça_pattern.match(c)]
            if praça_in_row0:
                praça_cols = praça_in_row0
                praça_header_row = 0

        # Find category 1 row
        cat1_row = None
        for r_idx, row in enumerate(all_rows):
            if praça_header_row is not None and r_idx <= praça_header_row:
                continue
            # Check if first few cells indicate category 1
            for i, cell in enumerate(row[:4]):
                cell_lower = cell.strip().lower()
                # Categoria 1: first cell is "1" AND second cell mentions "automóvel"
                # OR second cell mentions "automóvel" and first cell is "1"
                if cell_lower == '1' and i == 0:
                    # Check if this row has "automóvel" somewhere in the type column
                    for j, c in enumerate(row):
                        cl = c.lower()
                        if 'autom' in cl and j < len(row):
                            cat1_row = r_idx
                            break
                if 'automóvel' in cell_lower or 'automobile' in cell_lower:
                    # Check if first cell is "1"
                    if row and row[0].strip() == '1':
                        cat1_row = r_idx
                        break
            if cat1_row is not None:
                break

        if cat1_row is None:
            continue

        cat1_cells = all_rows[cat1_row]

        if praça_cols:
            # Patterns A and B: multiple praças with potentially different prices
            if praça_header_row == 1:
                # Pattern A: praças em row 1 (índices relativos, precisam de offset)
                # Ex: FLUMINENSE — praças em [0,1,2,3,4] → dados em [5,6,7,8,9]
                total_cells = len(cat1_cells)
                num_praca_cols = len(praça_cols)
                num_category_cols = total_cells - num_praca_cols
                for header_idx, praça_name in praça_cols:
                    data_idx = num_category_cols + header_idx
                    if data_idx < len(cat1_cells):
                        price = parse_price(cat1_cells[data_idx])
                        if price:
                            prices[praça_name] = price
            else:
                # Pattern B: praças em row 0 (índices já absolutos)
                # Ex: VIA BRASIL — praças em [5,6,7] → dados em [5,6,7]
                for header_idx, praça_name in praça_cols:
                    if header_idx < len(cat1_cells):
                        price = parse_price(cat1_cells[header_idx])
                        if price:
                            prices[praça_name] = price
        else:
            # Pattern C: single price for all praças of this concessionaire
            # Find the price column (look for R$ or numeric values after category info)
            for i, cell in enumerate(cat1_cells):
                if i >= 4:  # skip category/type/axes/rate columns
                    price = parse_price(cell)
                    if price:
                        prices['(única)'] = price
                        break

    return prices


def fuzzy_match(name: str, candidates: dict[str, float], cutoff: float = 0.6) -> float | None:
    """
    Tenta encontrar o preço para 'name' no dict 'candidates' usando
    matching fuzzy (difflib). Retorna o preço ou None.
    """
    norm = normalize_name(name)
    matches = difflib.get_close_matches(norm, candidates.keys(), n=1, cutoff=cutoff)
    if matches:
        return candidates[matches[0]]
    return None


def download_coordenadas() -> list[dict]:
    """Baixa e parseia CSV de coordenadas da ANTT."""
    print("\n[1/5] Baixando coordenadas das praças...")
    html = fetch_with_cache(URL_COORDENADAS, "pracas_coordenadas")
    if not html:
        raise RuntimeError("Não foi possível baixar o CSV de coordenadas")

    pracas = []
    lines = html.strip().split('\n')
    reader = csv.DictReader(lines, delimiter=';')
    for row in reader:
        if row.get('situacao', '').strip().lower() == 'ativo':
            pracas.append({
                'concessionaria': row.get('concessionaria', '').strip(),
                'praca_de_pedagio': row.get('praca_de_pedagio', '').strip(),
                'rodovia': row.get('rodovia', '').strip(),
                'uf': row.get('uf', '').strip(),
                'municipio': row.get('municipal', '').strip(),
                'latitude': row.get('latitude', '').strip(),
                'longitude': row.get('longitude', '').strip(),
                'preco_categoria_1': '',
            })

    # Salvar cache local
    cache_csv = OUTPUT_DIR / "pracas_coordenadas.csv"
    with open(cache_csv, 'w', newline='', encoding='utf-8') as f:
        if pracas:
            writer = csv.DictWriter(f, fieldnames=list(pracas[0].keys()))
            writer.writeheader()
            writer.writerows(pracas)

    print(f"  {len(pracas)} praças ativas encontradas")
    return pracas


def fetch_all_prices() -> dict[str, dict[str, float]]:
    """
    Para cada concessionária com URL mapeada, baixa e parseia as tarifas.
    Retorna dict {concessionaria: {praca_norm: preco}}.
    """
    print("\n[3/5] Coletando tarifas por concessionária...")
    all_prices = {}

    for nome, url in CONCESSIONARIA_URLS.items():
        if url is None:
            print(f"  [SKIP] {nome} — URL não mapeada")
            all_prices[nome] = {}
            continue

        cache_key = "tarifa_" + re.sub(r'[^a-z0-9]', '_', nome.lower())
        html = fetch_with_cache(url, cache_key)
        if not html:
            all_prices[nome] = {}
            continue

        prices = extract_category1_prices(html, nome)
        all_prices[nome] = prices
        print(f"  {nome}: {len(prices)} preço(s) extraído(s)")

    return all_prices


def match_and_export(pracas: list[dict], all_prices: dict[str, dict[str, float]]):
    """Faz o matching praça × preço e gera os CSVs finais."""
    print("\n[4/5] Fazendo matching praça × preço...")

    # Agrupar praças por concessionária, preservando ordem
    pracas_por_conc = {}
    for p in pracas:
        conc = p['concessionaria']
        if conc not in pracas_por_conc:
            pracas_por_conc[conc] = []
        pracas_por_conc[conc].append(p)

    # Extrair praças com nomes genéricos (Praça 1, Praça 2, P1, P2, ...) e seus preços
    def parse_praca_index(name: str) -> int | None:
        m = re.match(r'^praça\s*(\d+)$', name.strip(), re.IGNORECASE)
        if m:
            return int(m.group(1))
        # Suporte a "P1", "P01", "P 01", etc.
        m = re.match(r'^p\s*(\d+)$', name.strip(), re.IGNORECASE)
        if m:
            return int(m.group(1))
        return None

    # Resolver alias → nome base para lookup de HARDCODED_PRICES
    def resolve_concessionaria(nome: str) -> str:
        """Retorna o nome normalizado para lookup em HARDCODED_PRICES."""
        if nome in ALIASES:
            return ALIASES[nome]
        return nome

    # Inicializa campo fonte_preco para todas as praças
    for p in pracas:
        p['fonte_preco'] = ''

    # Aplicar hardcoded prices como fallback para todas as praças
    for p in pracas:
        conc = p['concessionaria']
        conc_base = resolve_concessionaria(conc)
        prices_to_use = HARDCODED_PRICES.get(conc) or HARDCODED_PRICES.get(conc_base, {})
        if prices_to_use:
            # 1) Fuzzy match normal
            price = fuzzy_match(p['praca_de_pedagio'], prices_to_use, cutoff=0.55)
            if price:
                p['preco_categoria_1'] = f"{price:.2f}"
                p['fonte_preco'] = 'hardcoded'
                continue
            # 2) Se contém "free flow", tentar preço genérico "free flow"
            if 'free flow' in unidecode(p['praca_de_pedagio']).lower():
                ff_price = prices_to_use.get('free flow')
                if ff_price:
                    p['preco_categoria_1'] = f"{ff_price:.2f}"
                    p['fonte_preco'] = 'hardcoded'
                    continue

    # Marcar concessionárias que já foram resolvidas no primeiro loop
    resolved_concs = set()
    for p in pracas:
        if p['preco_categoria_1']:
            resolved_concs.add(p['concessionaria'])

    matched = 0
    sem_match = []

    for conc, praças_csv in pracas_por_conc.items():
        # Se todas as praças já têm preço (do primeiro loop), pular
        all_have_price = all(p['preco_categoria_1'] for p in praças_csv)
        if all_have_price:
            for p in praças_csv:
                matched += 1
            continue

        # Resolver alias para lookup de HARDCODED_PRICES
        conc_base = resolve_concessionaria(conc)
        scraped_prices = all_prices.get(conc, {})
        # If no prices from ANTT page, try hardcoded (with alias resolution)
        prices = scraped_prices
        if not prices and conc_base in HARDCODED_PRICES:
            prices = HARDCODED_PRICES[conc_base]
        if not prices:
            # Só adicionar ao sem_match as que não têm preço ainda
            sem_match.extend(p for p in praças_csv if not p['preco_categoria_1'])
            continue

        is_url = bool(scraped_prices)
        # Detectar se temos preços por praça nomeada ou preço único
        praça_indices = {}  # praça_index → price
        preco_unico = None

        for praça_name, price in prices.items():
            idx = parse_praca_index(praça_name)
            if idx is not None:
                praça_indices[idx] = price
            elif praça_name == '(única)':
                preco_unico = price

        if preco_unico is not None and not praça_indices:
            # Preço único para todas as praças desta concessionária
            for p in praças_csv:
                p['preco_categoria_1'] = f"{preco_unico:.2f}"
                p['fonte_preco'] = 'url' if is_url else 'hardcoded'
                matched += 1
        elif praça_indices:
            # Preços por praça — tentar matching posicional
            csv_count = len(praças_csv)
            anTT_count = len(praça_indices)

            if csv_count == anTT_count:
                # Contagem igual: atribuir por posição
                sorted_indices = sorted(praça_indices.keys())
                for i, p in enumerate(praças_csv):
                    price = praça_indices[sorted_indices[i]]
                    p['preco_categoria_1'] = f"{price:.2f}"
                    p['fonte_preco'] = 'url' if is_url else 'hardcoded'
                    matched += 1
            elif csv_count < anTT_count:
                # Mais praças na ANTT que no CSV: usar as primeiras N
                sorted_indices = sorted(praça_indices.keys())
                for i, p in enumerate(praças_csv):
                    if i < anTT_count:
                        price = praça_indices[sorted_indices[i]]
                        p['preco_categoria_1'] = f"{price:.2f}"
                        p['fonte_preco'] = 'url' if is_url else 'hardcoded'
                        matched += 1
                    else:
                        sem_match.append(p)
            else:
                # Mais praças no CSV que na ANTT:
                # Se todos os preços são iguais, usar o comum
                unique_prices = set(praça_indices.values())
                if len(unique_prices) == 1:
                    common_price = list(unique_prices)[0]
                    for p in praças_csv:
                        p['preco_categoria_1'] = f"{common_price:.2f}"
                        p['fonte_preco'] = 'url' if is_url else 'hardcoded'
                        matched += 1
                else:
                    # Preços diferentes mas contagem diferente: fuzzy match como fallback
                    for p in praças_csv:
                        price = fuzzy_match(p['praca_de_pedagio'], prices)
                        if price:
                            p['preco_categoria_1'] = f"{price:.2f}"
                            p['fonte_preco'] = 'url' if is_url else 'hardcoded'
                            matched += 1
                        elif not p['preco_categoria_1']:
                            # Só adicionar ao sem_match se ainda não tem preço
                            sem_match.append(p)
        else:
            # Caso inesperado: preços mas sem identificar padrão
            for p in praças_csv:
                price = fuzzy_match(p['praca_de_pedagio'], prices)
                if price:
                    p['preco_categoria_1'] = f"{price:.2f}"
                    p['fonte_preco'] = 'url' if is_url else 'hardcoded'
                    matched += 1
                elif not p['preco_categoria_1']:
                    sem_match.append(p)

    # CSV completo
    output_csv = OUTPUT_DIR / "pracas_com_preco.csv"
    fieldnames = ['concessionaria','praca_de_pedagio','rodovia','uf',
                  'municipio','latitude','longitude','preco_categoria_1','fonte_preco']
    with open(output_csv, 'w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(pracas)

    # CSV sem match
    sem_match_csv = OUTPUT_DIR / "pracas_sem_match.csv"
    sem_match_fields = [f for f in fieldnames if f != 'preco_categoria_1']
    with open(sem_match_csv, 'w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=sem_match_fields)
        writer.writeheader()
        for row in sem_match:
            writer.writerow({k: row[k] for k in sem_match_fields})

    return matched, sem_match


def print_report(pracas, all_prices, matched, sem_match):
    """Imprime relatório final."""
    print("\n[5/5] Relatório")
    print("=" * 50)
    print(f"Total de praças ativas:        {len(pracas)}")

    mapeadas = sum(1 for u in CONCESSIONARIA_URLS.values() if u is not None)
    total = len(CONCESSIONARIA_URLS)
    print(f"Concessionárias mapeadas:      {mapeadas}/{total}")
    print(f"Praças com preço encontrado:   {matched} ({matched/len(pracas)*100:.1f}%)")
    print(f"Praças sem match:              {len(sem_match)}")

    # === Origem dos preços ===
    total = len(pracas)
    by_source = {}
    by_source_concs = {}
    for p in pracas:
        fonte = p.get('fonte_preco', '')
        if fonte:
            by_source[fonte] = by_source.get(fonte, 0) + 1
            conc = p['concessionaria']
            if fonte not in by_source_concs:
                by_source_concs[fonte] = set()
            by_source_concs[fonte].add(conc)

    print("\n=== Origem dos preços ===")

    # Via scraping (URL)
    url_count = by_source.get('url', 0)
    url_pct = url_count / total * 100 if total else 0
    print(f"Via scraping (URL):   {url_count} praças ({url_pct:.1f}%)")
    url_concs = sorted(by_source_concs.get('url', set()))
    if url_concs:
        print(f"  Concessionárias: {', '.join(url_concs)}")

    # Via hardcoded
    hc_count = by_source.get('hardcoded', 0)
    hc_pct = hc_count / total * 100 if total else 0
    print(f"Via hardcoded:        {hc_count} praças ({hc_pct:.1f}%)")
    hc_concs = sorted(by_source_concs.get('hardcoded', set()))
    if hc_concs:
        print(f"  Concessionárias: {', '.join(hc_concs)}")

    # Sem preço
    sem_count = by_source.get('sem_preco', 0) + len(sem_match)
    print(f"Sem preço:            {sem_count} praças")

    sem_url = [n for n, u in CONCESSIONARIA_URLS.items() if u is None]
    if sem_url:
        print("\nConcessionárias sem URL mapeada:")
        for n in sem_url:
            print(f"  - {n}")

    # Listar concessionárias do CSV que não estão no mapeamento
    concs_csv = set(p['concessionaria'] for p in pracas)
    concs_nao_mapeadas = concs_csv - set(CONCESSIONARIA_URLS.keys())
    if concs_nao_mapeadas:
        print("\nConcessionárias no CSV sem entrada no mapeamento:")
        for n in sorted(concs_nao_mapeadas):
            print(f"  - {n}")

    print(f"\nSalvo em:    output/pracas_com_preco.csv")
    print(f"Sem match:   output/pracas_sem_match.csv")
    print("=" * 50)


def discover_urls(concs: list[str]) -> dict[str, str | None]:
    """
    Para cada nome de concessionária, gerar slug e tentar GET em:
    https://www.gov.br/antt/pt-br/assuntos/rodovias/concessionarias/lista-de-concessoes/{slug}/tarifas-de-pedagio
    Se status 200 e contém <table>: adicionar URL.
    Caso contrário: None.
    """
    base = "https://www.gov.br/antt/pt-br/assuntos/rodovias/concessionarias/lista-de-concessoes"
    discovered = {}

    print(f"\n[2/5] Descobrindo URLs para {len(concs)} concessionária(s)...")
    for nome in sorted(concs):
        slug = unidecode(nome).lower().replace(' ', '-')
        slug = re.sub(r'[^a-z0-9\-]', '', slug)
        url = f"{base}/{slug}/tarifas-de-pedagio"

        cache_key = "discover_" + re.sub(r'[^a-z0-9]', '_', slug)
        cache_file = CACHE_DIR / f"{cache_key}.html"

        html = None
        status_code = None

        if cache_file.exists():
            html = cache_file.read_text(encoding='utf-8', errors='replace')
            # verificar se já temos tabela
            soup_check = BeautifulSoup(html, 'lxml')
            if soup_check.find_all('table'):
                discovered[nome] = url
                print(f"  [cache] {nome} → {url} (com tabela)")
                continue
            else:
                print(f"  [cache] {nome} → {url} (sem tabela, descarta)")
                discovered[nome] = None
                continue

        try:
            print(f"  [http]  GET {url}")
            headers = {
                'User-Agent': 'NewRoutes/1.0 (seed-pedagios; anderson@newroutes.app)'
            }
            resp = requests.get(url, headers=headers, timeout=15)
            status_code = resp.status_code
            html = resp.text
            cache_file.write_text(html, encoding='utf-8')
            time.sleep(1)
        except Exception as e:
            print(f"  [ERRO]  {nome}: {e}")
            discovered[nome] = None
            continue

        if status_code == 200:
            soup = BeautifulSoup(html, 'lxml')
            tables = soup.find_all('table')
            if tables:
                discovered[nome] = url
                print(f"  [OK]    {nome} → {url} (com tabela)")
            else:
                discovered[nome] = None
                print(f"  [!]     {nome} → {url} (200 mas sem tabela)")
        else:
            discovered[nome] = None
            print(f"  [!]     {nome} → {url} (status {status_code})")

    return discovered


def diagnose_urls():
    """Diagnóstico: por que as URLs mapeadas não estão retornando tabelas."""
    print("=" * 70)
    print("DIAGNÓSTICO DE URLS DE CONCESSIONÁRIAS")
    print("=" * 70)

    for nome, url in CONCESSIONARIA_URLS.items():
        if url is None:
            print(f"\n{'─' * 70}")
            print(f"  {nome}: URL = None (ignorada)")
            continue

        print(f"\n{'─' * 70}")
        print(f"  {nome}")
        print(f"  URL:  {url}")

        cache_key = "diag_" + re.sub(r'[^a-z0-9]', '_', url.lower())
        cache_file = CACHE_DIR / f"{cache_key}.html"

        # --- Status code: tentar HEAD primeiro, senão GET ---
        status_code = None
        final_url = url
        try:
            head_resp = requests.head(url, headers={
                'User-Agent': 'NewRoutes/1.0 (diagnose-pedagios)'
            }, timeout=15, allow_redirects=True)
            status_code = head_resp.status_code
            final_url = head_resp.url
        except Exception:
            pass

        # --- Baixar HTML (usa cache se existir) ---
        html = None
        if cache_file.exists():
            print(f"  [cache] Carregando de cache local")
            html = cache_file.read_text(encoding='utf-8', errors='replace')
        else:
            try:
                print(f"  [http]  GET {url}")
                headers = {
                    'User-Agent': 'NewRoutes/1.0 (diagnose-pedagios; anderson@newroutes.app)'
                }
                resp = requests.get(url, headers=headers, timeout=15)
                status_code = resp.status_code
                final_url = resp.url
                html = resp.text
                cache_file.write_text(html, encoding='utf-8')
                time.sleep(1)
            except Exception as e:
                print(f"  [ERRO]  Falha ao baixar: {e}")
                print(f"  Status code: {status_code}")
                print(f"  URL final:   {final_url}")
                continue

        html_size = len(html.encode('utf-8'))

        # --- Quantas <table> encontradas ---
        soup = BeautifulSoup(html, 'lxml')
        tables = soup.find_all('table')

        # --- Verificar iframe, PDF embed, JS redirect ---
        iframes = soup.find_all('iframe')
        embeds = soup.find_all('embed')
        objects = soup.find_all('object')
        js_redirect = bool(re.search(r'window\.location\s*=\s*["\']', html)) or bool(re.search(r'location\.href\s*=\s*["\']', html))

        # --- Print report ---
        print(f"  Status code: {status_code}")
        print(f"  URL final:   {final_url}")
        print(f"  Tamanho HTML: {html_size:,} bytes")
        print(f"  <table> encontrados: {len(tables)}")
        print(f"  <iframe> encontrados: {len(iframes)}")
        print(f"  <embed>/<object> (PDF): {len(embeds) + len(objects)}")
        print(f"  JS redirect (window.location): {js_redirect}")

        if tables:
            first_table = tables[0]
            headers = first_table.find_all(['th', 'td'])
            first3 = [h.get_text(strip=True) for h in headers[:3]]
            print(f"  Headers da 1ª tabela: {first3}")

            # Show table attributes for debugging
            attrs = first_table.attrs
            if attrs.get('class'):
                print(f"  Classes da tabela: {attrs['class']}")
            if attrs.get('id'):
                print(f"  ID da tabela: {attrs['id']}")
        else:
            print(f"  [!] Nenhuma tabela encontrada")
            if iframes:
                iframe_srcs = [iframe.get('src', 'sem-src') for iframe in iframes[:3]]
                print(f"  Iframe sources: {iframe_srcs}")
            if embeds or objects:
                print(f"  [!] Possível PDF embed detectado")

    print(f"\n{'=' * 70}")
    print("FIM DO DIAGNÓSTICO")
    print("=" * 70)


def run_full():
    """Executa o pipeline completo: descoberta de URLs, download, matching e export."""
    print("\n" + "=" * 60)
    print("SEED PEDAGIOS — PIPELINE COMPLETO")
    print("=" * 60)

    # Baixar coordenadas e identificar concessionárias não mapeadas
    pracas_raw = download_coordenadas()
    concs_csv = set(p['concessionaria'] for p in pracas_raw)
    concs_nao_mapeadas = sorted(concs_csv - set(CONCESSIONARIA_URLS.keys()))

    if concs_nao_mapeadas:
        print(f"\nConcessionárias no CSV sem entrada no mapeamento ({len(concs_nao_mapeadas)}):")
        for n in concs_nao_mapeadas:
            print(f"  - {n}")

        discovered = discover_urls(concs_nao_mapeadas)

        # Atualizar CONCESSIONARIA_URLS com descobertas
        for nome, url in discovered.items():
            CONCESSIONARIA_URLS[nome] = url
    else:
        print("\nTodas as concessionárias do CSV já estão mapeadas.")

    # Coletar tarifas
    all_prices = fetch_all_prices()

    # Matching e export
    matched, sem_match = match_and_export(pracas_raw, all_prices)

    # Relatório
    print_report(pracas_raw, all_prices, matched, sem_match)


if __name__ == "__main__":
    run_full()
