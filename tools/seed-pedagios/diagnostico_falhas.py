#!/usr/bin/env python3
"""
Diagnóstico de falhas de mapeamento de praças de pedágio.

Requisitos: pip install pandas
Uso:   python diagnostico_falhas.py
"""

import os
import re
from collections import defaultdict

import pandas as pd

# ── Paths ─────────────────────────────────────────────────────────────────────
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
OUTPUT_DIR = os.path.join(SCRIPT_DIR, "output")
INPUT_FILE = os.path.join(OUTPUT_DIR, "pracas_sem_match.csv")
OUTPUT_FILE = os.path.join(OUTPUT_DIR, "diagnostico_falhas.csv")

# Referência de concessionárias que sabemos que existem no sistema (pracas_com_preco)
COM_PRECO_FILE = os.path.join(OUTPUT_DIR, "pracas_com_preco.csv")

# ── Helpers ───────────────────────────────────────────────────────────────────

def is_federal_road(road: str) -> bool:
    """BR-XXX = federal; tudo que começa com número puro ou SP-XXX = estadual."""
    road = str(road).strip()
    if road.upper().startswith("BR-"):
        return True
    if re.match(r"^\d{2,3}$", road):
        return False  # número puro sem prefixo -> estadual / acesso
    if re.match(r"^[A-Z]{2}-", road):
        return False
    if re.match(r"^Acesso", road, re.IGNORECASE):
        return False
    return False  # default = não federal


def similarity_ratio(a: str, b: str) -> float:
    """Levenshtein-like simple similarity (0-1)."""
    if a == b:
        return 1.0
    long_s, short_s = (a, b) if len(a) >= len(b) else (b, a)
    short_s = short_s[: len(long_s)]
    if not short_s:
        return 0.0
    matches = sum(1 for x, y in zip(long_s, short_s) if x == y)
    return matches / len(long_s)


# ── Main ──────────────────────────────────────────────────────────────────────

def main():
    # Load failure data
    df = pd.read_csv(INPUT_FILE, sep=",", encoding="utf-8", dtype=str)
    df["concessionaria"] = df["concessionaria"].str.strip()
    df["rodovia"] = df["rodovia"].str.strip()
    df["praca_de_pedagio"] = df["praca_de_pedagio"].str.strip()

    # ── 1. Contagem por concessionária ────────────────────────────────────
    grouped = df.groupby("concessionaria")

    rows = []
    for conces, grp in grouped:
        contagem = len(grp)
        rodovias = sorted(grp["rodovia"].unique())
        rodovias_str = "; ".join(rodovias)
        exemplo = grp["praca_de_pedagio"].iloc[0]

        federal = [r for r in rodovias if is_federal_road(r)]
        estadual = [r for r in rodovias if not is_federal_road(r)]

        rows.append({
            "concessionaria": conces,
            "contagem_falhas": contagem,
            "rodovias_afetadas": rodovias_str,
            "exemplo_nome_praca": exemplo,
            "rodovias_federais": "; ".join(federal) if federal else "-",
            "rodovias_estaduais": "; ".join(estadual) if estadual else "-",
            "total_rodovias": len(rodovias),
        })

    diag_df = pd.DataFrame(rows)
    diag_df = diag_df.sort_values("contagem_falhas", ascending=False).reset_index(drop=True)

    # ── 2. Detecção de padrões de erro ────────────────────────────────────
    print("=" * 72)
    print("  DIAGNÓSTICO DE FALHAS DE MAPEAMENTO DE PRAÇAS")
    print("=" * 72)

    # Top concessionárias
    print("\n📊 TOP 10 concessionárias com mais falhas:")
    print("-" * 72)
    for i, (_, r) in enumerate(diag_df.head(10).iterrows(), 1):
        print(f"  {i:2d}. {r['concessionaria']:<30s}  falhas={r['contagem_falhas']}  "
              f"rodovias={r['total_rodovias']}")

    # Similar name patterns
    conces_list = sorted(set(df["concessionaria"].unique()))
    print(f"\n🔍 {len(conces_list)} concessionárias diferentes com falhas")

    # Check if concessionaries from failures also exist in the price file
    if os.path.exists(COM_PRECO_FILE):
        df_preco = pd.read_csv(COM_PRECO_FILE, sep=",", encoding="utf-8", dtype=str)
        df_preco["concessionaria"] = df_preco["concessionaria"].str.strip()
        conces_com_preco = set(df_preco["concessionaria"].unique())
        conces_sem_preco = set(df["concessionaria"].unique()) - conces_com_preco

        if conces_sem_preco:
            print(f"\n⚠️  {len(conces_sem_preco)} concessionárias NÃO aparecem no arquivo "
                  f"pracas_com_preco.csv:")
            for c in sorted(conces_sem_preco):
                count = len(df[df["concessionaria"] == c])
                print(f"     - {c} ({count} falhas)")
        else:
            # Check for similar names
            similar_pairs = []
            for f_conces in df["concessionaria"].unique():
                for p_conces in df_preco["concessionaria"].unique():
                    ratio = similarity_ratio(f_conces.upper(), p_conces.upper())
                    if ratio >= 0.75 and f_conces.upper() != p_conces.upper():
                        # Verify they are truly different (not just case differences)
                        if f_conces.upper() != p_conces.upper():
                            similar_pairs.append((f_conces, p_conces, ratio))

            if similar_pairs:
                print(f"\n⚠️  Nomes similares detectados (concessão com falha vs "
                      f"concessão no sistema):")
                for f_c, p_c, r in sorted(similar_pairs, key=lambda x: -x[2]):
                    print(f"     '{f_c}' ↔ '{p_c}' (similaridade={r:.0%})")

    # Rodovia patterns
    rodovias_counts = df["rodovia"].value_counts()
    print(f"\n🛣️  Rodovias mais afetadas:")
    print("-" * 72)
    for rod, cnt in rodovias_counts.head(10).items():
        federal = is_federal_road(rod)
        tag = "FED" if federal else "EST"
        print(f"     {rod:<25s}  {cnt:>3d} falhas  [{tag}]")

    # Non-standard road names
    non_standard = df[~df["rodovia"].str.match(r"^BR-\d+", case=False, na=False)]
    if not non_standard.empty:
        print(f"\n🚨 Rodovias com formato não padrão ({len(non_standard)} ocorrências):")
        for _, r in non_standard.iterrows():
            print(f"     {r['concessionaria']:<30s} | rodovia='{r['rodovia']}' | "
                  f"praça={r['praca_de_pedagio']}")

    # Save CSV
    diag_df.to_csv(OUTPUT_FILE, index=False, encoding="utf-8",
                   columns=["concessionaria", "contagem_falhas",
                            "rodovias_afetadas", "exemplo_nome_praca"])
    print(f"\n✅ Arquivo exportado: {OUTPUT_FILE}")
    print(f"   {len(diag_df)} concessionárias diagnosticadas")
    print(f"   {len(df)} falhas totais mapeadas")


if __name__ == "__main__":
    main()
