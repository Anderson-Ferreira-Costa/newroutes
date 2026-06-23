# Seed de Pedágios — New Routes

Script para gerar CSV de praças de pedágio com coordenadas e
preço para categoria 1 (Automóvel) a partir de dados abertos da ANTT.

## Pré-requisitos
- mise instalado (https://mise.jdx.dev)

## Setup
```bash
cd tools/seed-pedagios

# Instala Python 3.12.3 via mise
mise install

# Cria virtualenv e instala dependências
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

## Uso
```bash
source .venv/bin/activate
python seed_pedagios.py
```

## Outputs
- output/pracas_com_preco.csv — todas as praças ativas com preço CAR quando disponível
- output/pracas_sem_match.csv — praças sem preço para revisão manual
- output/cache/ — HTMLs baixados (delete para forçar re-download)

## Próximo passo
Quando satisfatório, copiar pracas_com_preco.csv para:
app/src/main/assets/pracas_pedagio.csv
(importação no Room será feita em sessão futura)
