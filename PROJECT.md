# NewRoutes

Aplicativo Android para planejamento de rotas com cálculo de custos de pedágio.

## Tecnologias

- **Linguagem**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Mapas**: OSMDroid 6.1.18 (tiles OpenStreetMap)
- **Roteamento**: OSRM público (`router.project-osrm.org`)
- **Geocoding**: Nominatim / OpenStreetMap
- **Injeção de Dependência**: Hilt 2.51.1
- **Persistência**: Room 2.6.1
- **HTTP/JSON**: Retrofit 2.11.0 + Moshi
- **Logging**: OkHttp Interceptor 4.12.0
- **Async**: Coroutines + Flow
- **Estado**: ViewModel + StateFlow
- **Navegação**: Navigation Compose 2.7.7

## Arquitetura

```
app/
├── ui/              # Camada de apresentação
│   ├── map/         # Tela de mapa + ViewModel
│   ├── route/       # Tela de detalhes da rota + ViewModel
│   └── summary/     # Tela de resumo + ViewModel
├── domain/          # Camada de negócio
│   ├── model/       # Entidades de domínio
│   ├── usecase/     # Casos de uso
│   └── repository/  # Interfaces de repositório
└── data/            # Camada de dados
    ├── routing/     # OSRM client + repository
    ├── tolls/       # Room database + repository
    └── geocoding/   # Nominatim client + repository
```

## Convenções

- Screens e ViewModels sempre em par: `*Screen.kt` + `*ViewModel.kt`
- UI nunca acessa a camada `data` diretamente — sempre via `domain/usecase`
- Um UseCase por operação de negócio
- Repository pattern: interfaces em `domain/`, implementações em `data/`
- Sem callbacks — usar `suspend functions` e `Flow`
- Testes unitários em `test/`, testes de integração em `androidTest/`

## Decisões Arquiteturais

| Decisão | Escolha | Justificativa |
|---------|---------|---------------|
| Mapas | OSMDroid | Gratuito, sem chave de API |
| Geocoding | Nominatim | Gratuito, sem chave de API |
| Roteamento | OSRM público | Gratuito para MVP |
| Pedágios | Base local (Room) | Dados da ANTT em CSV |
| IA | Não integrada | Modelo usado apenas para desenvolvimento |

## Status

- [x] Projeto scaffold criado
- [ ] Implementação da camada de domínio
- [ ] Implementação da camada de dados
- [ ] Implementação da camada de UI
- [ ] Integração OSRM
- [ ] Integração Nominatim
- [ ] Database Room para pedágios
- [ ] Testes unitários
- [ ] Testes de UI

## Como Rodar

1. Abrir o projeto no Android Studio
2. Sync do Gradle (File > Sync Project with Gradle Files)
3. Rodar no emulador ou dispositivo físico (API 26+)

## Permissões Necessárias

- `INTERNET` — OSRM, Nominatim, tiles OSMDroid
- `ACCESS_NETWORK_STATE` — Verificar conectividade
- `ACCESS_FINE_LOCATION` — Localização do usuário
- `WRITE_EXTERNAL_STORAGE` — Cache de tiles OSMDroid (API <= 32)
