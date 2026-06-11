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

## Histórico de Sessões

### Sessão 1 — domain/model (concluída)
- Waypoint, Route, TollPlaza, Vehicle criados em domain/model/
- TollCategory enum em TollPlaza.kt, reutilizado por Vehicle
- Route.kt tem validação via init/require (mínimo 2 waypoints)
- IDs são java.util.UUID (sem imports de Android)
- TODO: TypeConverter para TollCategory quando implementar Room em data/tolls/

### Sessão 2 — domain/repository (concluída)
- IRouteRepository, ITollRepository, IVehicleRepository criados em domain/repository/
- saveRoute retorna Unit — ID gerado no model, não no repository
- getTollPlazasNearRoute retorna List (não Flow) — consulta pontual, não stream
- setDefault só recebe o ID a promover — implementação em data/ faz a transação de desmarcar os demais
- Zero imports de framework — apenas Flow, UUID e modelos de domain/model/

### Sessão 3 — domain/usecase (concluída)
- CalculateRouteUseCase, EstimateCostUseCase, GetRoutesUseCase,
  SaveRouteUseCase, DeleteRouteUseCase, ManageVehicleUseCase criados
- Result<T> via kotlin.Result + mapCatching (sem classe própria)
- distanceMeters e durationSeconds são 0L com TODO — depende de OsrmClient (data/)
- Filtro de TollPlaza por category feito no UseCase após busca
- getTollPlazasNearRoute com falha retorna emptyList() — revisar quando OSRM integrado
- Zero imports de framework em toda a camada domain/

### Sessão 4 — data/geocoding (concluída)
- NominatimApi, NominatimPlace (DTO), NominatimRepository, GeocodingModule criados
- Base URL: https://nominatim.openstreetmap.org/
- @Query por método (limit=5 em search, limit=1 em reverse)
- User-Agent header obrigatório: NewRoutes/1.0
- BuildConfig.DEBUG adicionado ao build.gradle.kts (buildFeatures)
- TODO: rate limiting 1 req/s no NominatimRepository
- NominatimRepository NÃO implementa interface — domain/repository não tem contrato

### Sessão 5 — data/routing (concluída)
- OsrmApi, OsrmResponse (DTOs), OsrmRouteResult, OsrmRepository, RoutingModule criados
- Base URL: https://router.project-osrm.org/
- Coordenadas no formato lon,lat separadas por ";" (longitude primeiro — padrão OSRM)
- @Named("osrm") em OkHttpClient e Retrofit para não colidir com GeocodingModule
- Timeout 30s (vs 15s do Nominatim)
- CalculateRouteUseCase atualizado — distanceMeters e durationSeconds reais, TODOs removidos
- TODO: migrar para instância self-hosted do OSRM
- OsrmRepository NÃO implementa IRouteRepository — responsabilidade separada

### Sessão 6 — data/tolls / Room completo (concluída)
- Converters, Entities (3), DAOs (3), Repositories (3), NewRoutesDatabase, DatabaseModule criados
- @Upsert do Room 2.6.1 em todos os DAOs
- TypeConverters via Moshi (UUID, TollCategory, List<TollPlaza>, List<Waypoint>, Vehicle)
- bounding box com fator 111_000m/grau — aproximação aceitável para MVP
  TODO: corrigir fator de longitude por latitude (cos(lat) * 111_320m)
- setDefault sem @Transaction — TODO para versão futura
- fallbackToDestructiveMigration() — TODO para migrations reais antes de produção
- schemas/ criado, ksp schemaLocation configurado no build.gradle.kts
- @Binds em RepositoryBindings (abstract class) separado dos @Provides (object)

### Sessão 7+8 — ui/map + ui/route (concluída)
- MapScreen: OSMDroid fullscreen + search bar + FAB + bottom sheet + polyline
- MapViewModel: NominatimRepository + CalculateRouteUseCase + SaveRouteUseCase
- RouteScreen: seleção de veículo (LazyRow) + waypoints com up/down reorder
- RouteViewModel: ManageVehicleUseCase + NominatimRepository
- AppNavigation: NavHost map/route/summary + SharedRouteConfig para comunicação entre telas
- decodePolyline implementado localmente (sem dependência extra)
- Markers via android.R.drawable — TODO substituir por assets próprios
- SharedRouteConfig em MapScreen.kt — TODO mover para arquivo próprio
- Drag-and-drop de waypoints não implementado (BOM 2024.12.01 sem suporte)
  TODO: adicionar dependência de reorder quando disponível

### Sessão 9 — ui/summary (concluída)
- SummaryScreen: LazyColumn com 6 seções (métricas, custos, veículo, pedágios, itinerário, salvar)
- SummaryViewModel: GetRoutesUseCase + SaveRouteUseCase + SavedStateHandle para routeId
- formatDuration implementada como função local privada no arquivo da Screen
- Ícones de veículo via when(category) com TollCategory
- No nested LazyColumn — items() direto no LazyColumn pai para pedágios
- Divider vertical entre waypoints do itinerário (conector visual)
- SavedStateHandle["routeId"] para recuperar ID da rota via Navigation Compose

### Sessão 10 — Correções pós-build
- DomainModule.kt criado em domain/usecase/ — @Provides para UseCases
  (Hilt não injeta UseCases automaticamente sem binding explícito)
- Markers OSMDroid via Canvas customizado (createMarkersOverlay)
  TODO: substituir por Marker nativo do OSMDroid
- collectAsState() obrigatório para StateFlow em Compose

### Sessão 11 — Correções pós-build

- Moshi deserialização de `List<T>`: `GeocodingModule.kt` e `RoutingModule.kt`
  atualizados com `Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()`
- Nominatim 403: User-Agent corrigido para `"NewRoutes/1.0 (newroutes@example.com)"`
  (sem `contato:` — Nominatim rejeita strings genéricas)
- Accept-Language: header `pt-BR,pt;q=0.9` adicionado ao interceptor
- Ordem dos interceptors: User-Agent antes do logging interceptor na cadeia

### Sessão 12 — Migração Nominatim → Photon (concluída)
- Geocoding migrado de Nominatim para Photon (Komoot)
- Motivo: Nominatim bloqueia autocomplete/search client-side (403) —
  explicitamente proibido pela política de uso público
- Photon: `photon.komoot.io` — sem rate limit restritivo, aceita busca client-side
- Novos arquivos: `PhotonPlace.kt` (DTO GeoJSON), `PhotonApi.kt`, `PhotonRepository.kt`
- Arquivos removidos: `NominatimApi.kt`, `NominatimPlace.kt`, `NominatimRepository.kt`
- `GeocodingModule.kt`: base URL → `https://photon.komoot.io/`, providers atualizados
- `MapViewModel.kt` + `RouteViewModel.kt`: `NominatimRepository` → `PhotonRepository`
- Photon API: bbox limita resultados ao Brasil (`-73.98,-33.75,-34.79,5.27`)
- DTO GeoJSON: `PhotonFeature(geometry.coordinates: [lon, lat], properties: {name, country, state, locality, postcode})`

## Bug fixes pós-build
- GeocodingModule e RoutingModule: `MoshiConverterFactory.create()` sem instância
  de Moshi não deserializa `List<T>` de data classes Kotlin
  Correção: `Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()`
  Dependência: `moshi-kotlin:1.15.2`
- GeocodingModule: Nominatim retorna 403 quando User-Agent não contém identificação
  ou interceptor vem após logging interceptor
  Correção: `"NewRoutes/1.0 (email)"` + `Accept-Language: pt-BR,pt;q=0.9` +
  ordem User-Agent → logging
- GeocodingModule: Nominatim bloqueia autocomplete/search client-side (política de uso)
  Correção: migração completa para Photon API (`photon.komoot.io`)
