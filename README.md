# WorkBuddy

Uma app Android simples para planear treinos de ginásio e registar o que fazes em cada sessão. É como uma folha de Excel, mas no telemóvel.

Está escrita em **Kotlin** com **Jetpack Compose** e guarda tudo numa base de dados local (**Room/SQLite**). Não precisa de internet nem de conta.

O código foi escrito para ser lido: está comentado em português e organizado da forma que a Google recomenda para apps Android modernas. Quando quiseres praticar, tens uma lista de desafios em [EXERCICIOS.md](EXERCICIOS.md).

---

## O que a app faz

1. **Planos**: crias um plano (ex.: "Push/Pull/Legs") e escolhes quantos dias de treino tem (1 a 7).
2. **Dias**: dás nome a cada dia (ex.: "Peito e tríceps") e adicionas exercícios com **séries, repetições, peso (kg) e notas**.
3. **Treino**: num dia, carregas em **Treinar**. A app cria uma "folha" com uma linha por série, já com o peso planeado. Escreves as repetições que fizeste (e o peso, se mudou). O objetivo aparece a cinzento e **tudo é guardado automaticamente**.
4. **Histórico**: vês todos os treinos (com quantas séries registaste) e podes abrir qualquer um para rever ou corrigir.

Se mais tarde alterares ou apagares um plano, os treinos antigos mantêm-se no histórico.

---

## Como abrir e correr

**Requisitos:** Android Studio **Narwhal Feature Drop (2025.1.2) ou mais recente**.

1. No Android Studio: **File > Open…** e escolhe a pasta `WorkBuddy`.
   (Ou **File > New > Project from Version Control…** com o URL do repositório no GitHub.)
2. Espera pelo **Gradle sync** (a barra de progresso em baixo). Da primeira vez demora uns minutos, porque descarrega o Gradle e as bibliotecas.
3. Escolhe onde correr a app:
   - **Emulador:** *Device Manager* > *+* > escolhe um telemóvel (ex.: Pixel) com uma imagem recente do Android. [Guia](https://developer.android.com/studio/run/managing-avds)
   - **O teu telemóvel:** ativa as *Opções de programador* e a *Depuração USB*. [Guia](https://developer.android.com/studio/run/device)
4. Carrega em **Run ▶** (configuração `app`).

> **O Android Studio sugere atualizar o "Android Gradle Plugin"?** Podes ignorar por agora. O projeto usa a versão 8.12 de propósito, para funcionar também em Android Studios um pouco mais antigos. Atualizar é um dos desafios em EXERCICIOS.md.

> **Da primeira vez que compilares**, o Room cria o ficheiro `app/schemas/.../1.json` com a "fotografia" da base de dados. Faz commit dele: vais precisar dele quando mudares a base de dados (ver abaixo).

---

## Como o código está organizado

A app está dividida em camadas. Cada camada só fala com a de baixo:

```
  ┌──────────────┐  ações (ex.: updateReps)   ┌─────────────┐        ┌────────────┐        ┌─────────────┐
  │  Ecrã        │ ─────────────────────────▶ │  ViewModel  │ ─────▶ │ Repository │ ─────▶ │ DAO (Room)  │ ──▶ SQLite
  │  (Compose)   │ ◀───────────────────────── │             │ ◀───── │            │ ◀───── │             │
  └──────────────┘  estado (UiState)           └─────────────┘  Flow  └────────────┘  Flow  └─────────────┘
```

**Exemplo:** escreves "8" nas repetições de uma série.

1. O `SessionScreen` chama `viewModel.updateReps(id, 8)`.
2. O `SessionViewModel` pede ao `WorkoutRepository`, que pede ao `WorkoutDao`: `UPDATE set_logs SET reps = 8 ...`.
3. O Room percebe que a tabela `set_logs` mudou e o `Flow` de `observeSetLogs` emite a lista nova.
4. O ViewModel transforma essa lista num novo `SessionUiState`.
5. O Compose vê que o estado mudou e volta a desenhar o ecrã.

Os dados vão sempre num só sentido: **as ações descem e o estado sobe**. Por isso nunca tens de "atualizar o ecrã à mão".

### Mapa dos ficheiros

Todos os ficheiros estão em `app/src/main/java/com/campsc/workbuddy/`.

| Ficheiro | O que faz |
|---|---|
| `data/Entities.kt` | As **tabelas** (Plan, PlanDay, Exercise, WorkoutSession, SetLog) e como se ligam |
| `data/WorkoutDao.kt` | As **consultas SQL** (ler, inserir, atualizar, apagar) |
| `data/WorkBuddyDatabase.kt` | Cria a base de dados |
| `data/WorkoutRepository.kt` | A porta de entrada para os dados. Tem a lógica de **criar um plano** e **começar um treino** |
| `WorkBuddyApplication.kt` | Cria o repositório uma única vez, para toda a app |
| `MainActivity.kt` | O ponto de entrada da app: aplica o tema e mostra a navegação |
| `ui/WorkBuddyNavHost.kt` | As **rotas** e o mapa de navegação entre ecrãs |
| `ui/AppViewModelProvider.kt` | Sabe criar cada ViewModel com aquilo de que precisa |
| `ui/Format.kt` | Funções para formatar e validar pesos, datas, etc. (com testes!) |
| `ui/components/` | Peças reutilizáveis: diálogos, seletor de número, texto de lista vazia |
| `ui/plans/` | Ecrã inicial: lista de planos e criar plano |
| `ui/plan/` | Um plano: os seus dias, com botão **Treinar** |
| `ui/day/` | Um dia: adicionar/editar/apagar exercícios |
| `ui/session/` | **O treino**: registar repetições e peso de cada série |
| `ui/history/` | Histórico de treinos |
| `ui/theme/Theme.kt` | Cores da app |

**Ordem de leitura sugerida:** `Entities.kt` → `WorkoutDao.kt` → `WorkoutRepository.kt` → `ui/plans/` (o ecrã mais simples) → `WorkBuddyNavHost.kt` → `ui/session/` → `AppViewModelProvider.kt`.

Cada ecrã tem dois ficheiros:
- `XxxScreen.kt` desenha a interface (funções `@Composable`).
- `XxxViewModel.kt` guarda o estado e trata das ações.

---

## Atenção: mudar a base de dados

Se mudares alguma `@Entity` (por exemplo, adicionar uma coluna), a base de dados que já está no telemóvel fica diferente da do código e **a app fecha-se ao abrir**. Tens duas opções:

- **Fazer bem:** subir o `version` em `WorkBuddyDatabase.kt` e criar uma migração. O Room consegue fazê-la sozinho na maioria dos casos (`AutoMigration`). Está explicado no desafio 11 de EXERCICIOS.md.
- **Atalho enquanto testas:** desinstalar a app do emulador/telemóvel. **Perdes os dados**, por isso não faças isto no telemóvel onde registas os treinos a sério.

---

## Dicas do Android Studio

- **Database Inspector** (*View > Tool Windows > App Inspection*): com a app a correr, vês as tabelas **ao vivo, como num Excel**. Podes até escrever SQL. É a melhor forma de perceber o que a app está a guardar. [Guia](https://developer.android.com/studio/inspect/database)
- **Preview:** abre `PlanDetailScreen.kt` ou `SessionScreen.kt` e escolhe a vista *Split*. Vês os `@Preview` sem correr a app. [Guia](https://developer.android.com/develop/ui/compose/tooling/previews)
- **Testes:** abre `app/src/test/.../FormatTest.kt` e carrega no ▶ verde ao lado da classe.
- **Debugger:** clica na margem de uma linha para pôr um *breakpoint* e corre com **Debug** 🐞 em vez de Run.
- **Logcat:** para "prints", usa `android.util.Log.d("WorkBuddy", "valor = $x")` e filtra por `WorkBuddy` no Logcat.
- **Atalhos úteis** (Windows/Linux, no Mac troca Ctrl por Cmd):
  - `Shift` `Shift`: procurar qualquer coisa
  - `Ctrl+B`: ir para a definição do que está debaixo do cursor
  - `Alt+Enter`: sugestões e correções rápidas (incluindo imports)
  - `Shift+F6`: mudar o nome de algo em todo o projeto
  - `Ctrl+Alt+L`: formatar o código

---

## Para aprender mais

- [Tour do Kotlin](https://kotlinlang.org/docs/kotlin-tour-welcome.html): refrescar a linguagem
- [Android Basics with Compose](https://developer.android.com/courses/android-basics-compose/course): o curso oficial e gratuito da Google. O codelab do [Room](https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room) usa a mesma estrutura desta app
- [Tutorial de Compose](https://developer.android.com/develop/ui/compose/tutorial) e [Estado no Compose](https://developer.android.com/develop/ui/compose/state)
- [Room](https://developer.android.com/training/data-storage/room) · [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) · [Flow](https://developer.android.com/kotlin/flow) · [Navegação com tipos](https://developer.android.com/guide/navigation/design/type-safety)

---

## Versões

Todas as versões estão em [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

| | Versão |
|---|---|
| Android Gradle Plugin | 8.12.0 |
| Gradle | 8.14.3 (corre com JDK 17 a 24) |
| Kotlin / KSP | 2.2.21 / 2.2.21-2.0.5 |
| Compose BOM | 2025.11.01 (Material 3 1.4) |
| Room | 2.8.4 |
| minSdk / targetSdk | 26 (Android 8.0) / 36 |
