# New Routes — PROJECT.md

## Visão geral

App Android nativo de planejamento de rotas com cálculo de custo de combustível, inspirado no WikiRota.

## Stack

| Categoria | Tecnologia | Versão |
|-----------|-----------|--------|
| Linguagem | Kotlin | 2.0.21 |
| UI | Jetpack Compose + Material 3 | Compose BOM 2024.12.01 |
| Mapas | OSMDroid | 6.1.18 |
| Roteamento | OSRM público | router.project-osrm.org |
| Geocoding | Photon (Komoot) | photon.komoot.io |
| DI | Hilt | 2.51.1 |
| Persistência | Room | 2.6.1 |
| HTTP | Retrofit + Moshi | 2.11.0 + 1.15.2 |
| Navegação | Navigation Compose | 2.7.7 |
| Async | Kotlin Coroutines | 1.9.0 |
| Build | Gradle | 8.9 |
| SDK | Min: 26 / Target: 35 | Android 8.0 / 15 |

## Arquitetura

Clean Architecture em 3 camadas:

- **Domain** (`domain/`): modelos, interfaces de repositório e use cases. Não depende de nenhuma biblioteca de infraestrutura.
- **Data** (`data/`): implementações concretas de repositórios (Room), clients HTTP (Retrofit/OkHttp), e módulos Hilt de injeção de dependência.
- **UI** (`ui/`): telas Jetpack Compose com ViewModel + StateFlow. Navegação via Navigation Compose com bottom navigation.

## Estrutura de pacotes

```
com.newroutes.app/
├── NewRoutesApplication.kt
├── data/
│   ├── geocoding/
│   │   ├── GeocodingModule.kt
│   │   ├── PhotonApi.kt
│   │   ├── PhotonPlace.kt
│   │   ├── PhotonRepository.kt
│   │   └── PhotonResponse.kt
│   ├── routing/
│   │   ├── OsrmApi.kt
│   │   ├── OsrmRepository.kt
│   │   ├── OsrmResponse.kt
│   │   ├── OsrmRouteResult.kt
│   │   ├── RouteDao.kt
│   │   ├── RouteEntity.kt
│   │   ├── RouteRepository.kt
│   │   └── RoutingModule.kt
│   └── tolls/
│       ├── Converters.kt
│       ├── DatabaseModule.kt
│       ├── NewRoutesDatabase.kt
│       ├── VehicleDao.kt
│       ├── VehicleEntity.kt
│       └── VehicleRepository.kt
├── domain/
│   ├── model/
│   │   ├── Route.kt
│   │   ├── TollCategory.kt    (enum, re-nomeado de TollPlaza.kt)
│   │   ├── Vehicle.kt
│   │   └── Waypoint.kt
│   ├── repository/
│   │   ├── IRouteRepository.kt
│   │   └── IVehicleRepository.kt
│   └── usecase/
│       ├── CalculateRouteUseCase.kt
│       ├── DomainModule.kt
│       ├── GetRoutesUseCase.kt
│       ├── ManageVehicleUseCase.kt
│       └── SaveRouteUseCase.kt
└── ui/
    ├── MainActivity.kt
    ├── map/
    │   ├── MapScreen.kt
    │   └── MapViewModel.kt
    ├── navigation/
    │   ├── AppBottomNavigation.kt
    │   ├── AppNavigation.kt
    │   └── SharedRouteConfig.kt
    ├── route/
    │   ├── RouteScreen.kt
    │   └── RouteViewModel.kt
    ├── routes/
    │   ├── SavedRoutesScreen.kt
    │   └── SavedRoutesViewModel.kt
    ├── summary/
    │   ├── SummaryScreen.kt
    │   └── SummaryViewModel.kt
    └── vehicle/
        ├── VehicleScreen.kt
        └── VehicleViewModel.kt
```

## APIs externas

- **Geocoding**: Photon (photon.komoot.io) — motivação: Nominatim proíbe autocomplete client-side e possui rate limit restritivo; Photon é open source, baseado em OSM, sem essas restrições.
- **Roteamento**: OSRM público (router.project-osrm.org) — instância gratuita da comunidade, sem autenticação necessária.
- **Mapas**: OSMDroid 6.1.18 + tiles OpenStreetMap (Mapnik).

## Features implementadas

- [x] Busca de origem/destino (Photon)
- [x] Cálculo de rota (OSRM — distância, duração, polilinha)
- [x] Cálculo de custo de combustível (fórmula: distância / consumo × preço)
- [x] Cadastro/gerenciamento de veículos (nomes, categorias, consumo, preço)
- [x] Waypoints intermediários (paradas adicionais na rota)
- [x] Histórico de rotas (salvamento automático ao calcular)
- [x] Bottom navigation (Mapa / Veículos / Rotas)
- [x] Tela de resumo detalhado de rota

## Features desativadas/pendentes

- [ ] **Pedágios** — desativado em 2025. Motivo: dados da ANTT inconsistentes entre fontes (scraping + hardcoded), valores não confiáveis.
  - Código preservado intencionalmente: `TollCategory` (enum em `domain/model/TollPlaza.kt`) ainda existe pois é usado para categorização visual de veículos.
  - Arquivos de pedágio removidos: `TollPlaza` (data class), `ITollRepository`, `TollRepository`, `TollPlazaDao`, `TollPlazaEntity`.
  - Script de seed em `tools/seed-pedagios/` (Python, não integrado ao app).

## Decisões técnicas

1. **Photon sobre Nominatim**: Nominatim exige header `User-Agent` específico e proíbe autocomplete client-side. Photon (Komoot) é open source, baseado em OSM, sem rate limit restritivo.
2. **OSRM público sobre self-hosted**: para MVP, o OSRM público é suficiente. Em produção, considerar instância própria para garantir disponibilidade e evitar rate limiting.
3. **Salvamento automático sobre botão manual**: rotas são salvas automaticamente ao serem calculadas (`SaveRouteUseCase.invoke(route)` dentro de `MapViewModel.calculateRoute()`). Elimina o passo adicional de "salvar" e garante que nenhuma rota seja perdida.
4. **Clean Architecture com @Deprecated para tolls**: a interface `ITollRepository` foi marcada `@Deprecated` e as classes concretas foram removidas. O enum `TollCategory` foi mantido pois é usado para categorização visual de veículos em todas as telas.

## Convenções de código

- Kotlin com estilo ofical do Google
- Corrutinas + StateFlow para estado assíncrono
- UseCases seguem padrão `invoke()` para chamada direta
- Repositórios seguem nomenclatura consistente (`saveX`, `deleteX`, `getAllX`)
- ViewModel: funções `onXChanged` para atualização de estado, `clearX` para limpeza
- KDoc em classes públicas (Repository, UseCase, ViewModel)
- Comentários explicam decisões, não repetem nomes de funções

## Como rodar

1. Instalar Android SDK com API 35
2. Abrir o projeto no Android Studio
3. Sync do Gradle e rodar o app

## TODOs conhecidos

1. **Migrations formais do Room** — atualmente usa `fallbackToDestructiveMigration()`. Em produção, criar `Migration` objects explícitos.
2. **OSRM self-hosted** — migrar para instância própria do OSRM para garantir disponibilidade e controle de rate limiting.
3. **Testes** — adicionar testes unitários e instrumentados. Stubs existem em `test/` e `androidTest/` mas não foram implementados.
