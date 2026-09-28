# Desafios para ganhar o calo 💪

A app já funciona. Estes desafios servem para **mexeres no código** e voltares a ganhar prática, do mais fácil para o mais difícil. Cada um diz onde mexer e dá algumas dicas, mas não a solução completa.

## Como trabalhar

- **Um desafio de cada vez, num branch próprio:** `git checkout -b desafio-02-volume`. Se correr mal, voltas atrás sem medo.
- **Passos pequenos:** muda uma coisa, corre a app e vê o resultado. Faz commit quando algo funcionar.
- **Lê os erros com calma.** O Android Studio sublinha a vermelho, e o `Alt+Enter` resolve muitos sozinho (imports, por exemplo).
- **Usa o Database Inspector** (*App Inspection*) para veres o que ficou guardado nas tabelas.
- Se mudares uma `@Entity`, lê primeiro o desafio 11.

---

## Nível 0: Reconhecimento

### 0. Dá uma volta pelo projeto
1. Corre a app, cria um plano com 3 dias, adiciona exercícios e faz um "treino" a fingir.
2. Abre o **Database Inspector** e encontra as linhas que criaste em cada tabela.
3. Põe um *breakpoint* em `WorkoutRepository.startSession` e corre em **Debug**. Carrega em "Treinar" e avança linha a linha (`F8`) até perceberes como são criadas as séries.
4. Muda um texto qualquer (ex.: "Os meus planos") e vê a alteração.

**Vais praticar:** navegar no projeto, debugger, Database Inspector.

---

## Nível 1: Aquecimento (mexer no que já existe)

### 1. Muda as cores
Em `ui/theme/Theme.kt`, muda o `primary` das `LightColors`. No teu telemóvel não muda nada? Isso é o `dynamicColor`: descobre o que faz e desliga-o.

**Vais praticar:** MaterialTheme, parâmetros com valor por omissão.

### 2. Mostra o volume do treino
No ecrã do treino, mostra no topo o **volume total**: a soma de `reps × kg` de todas as séries registadas (ex.: "Volume: 3 450 kg").

- **Onde:** `SessionViewModel.kt` (acrescenta um campo ao `SessionUiState`) e `SessionScreen.kt` (um `item { }` antes dos exercícios na `LazyColumn`).
- **Dica:** as listas em Kotlin têm `sumOf { ... }`. Cuidado que `reps` e `weightKg` podem ser `null`: vê o operador `?:`.

**Vais praticar:** coleções em Kotlin, nulls, UiState.

### 3. "Tens a certeza que terminaste?"
Ao carregar em **Terminar treino**, se houver séries por preencher, mostra um diálogo: *"Ainda tens 3 séries por registar. Terminar mesmo assim?"*

- **Onde:** `SessionScreen.kt`.
- **Dica:** copia o padrão dos outros diálogos (`var showX by rememberSaveable { mutableStateOf(false) }` + `if (showX) { AlertDialog(...) }`).

**Vais praticar:** estado em Compose, diálogos.

### 4. Caça ao bug 🐛
Se carregares **duas vezes muito depressa** em "Treinar", são criados dois treinos. Confirma no Database Inspector e corrige.

- **Onde:** `PlanDetailViewModel.startSession` e `DayViewModel.startSession`.
- **Dica:** o ViewModel pode lembrar-se de que já está a começar um treino e ignorar o segundo clique.

**Vais praticar:** corrotinas, estado no ViewModel, pensar em casos-limite.

### 5. Textos em `strings.xml` (e em inglês)
Os textos estão escritos diretamente no código. Passa os de um ecrã para `res/values/strings.xml` e usa `stringResource(R.string.xxx)`. Depois cria `res/values-en/strings.xml` com a tradução e muda o idioma do emulador para ver a diferença.

**Vais praticar:** recursos Android, internacionalização.

---

## Nível 2: Funcionalidades novas (SQL + ecrã)

### 6. "Da última vez fizeste…"
No ecrã do treino, mostra por baixo do nome de cada exercício o que fizeste **no treino anterior** (ex.: *"Última vez: 10, 9, 8 @ 60 kg"*). É talvez a funcionalidade mais útil no ginásio!

- **Onde:** `WorkoutDao` → `WorkoutRepository` → `SessionViewModel` → `SessionScreen`.
- **Dica (SQL):** estas são as séries do último treino anterior em que registaste este exercício:
  ```sql
  SELECT * FROM set_logs
  WHERE exerciseId = :exerciseId
    AND sessionId = (
      SELECT MAX(sessionId) FROM set_logs
      WHERE exerciseId = :exerciseId
        AND sessionId < :currentSessionId
        AND reps IS NOT NULL
    )
  ORDER BY setNumber
  ```
- **Dica (Kotlin):** o `ExerciseLog` pode ganhar um campo `previousSets: List<SetLog>`. Para ler os dados de vários exercícios, uma função `suspend` no DAO é mais simples do que um `Flow`.

**Vais praticar:** SQL com subconsultas, passar dados por todas as camadas.

### 7. Reordenar exercícios
No ecrã do dia, põe setas ↑/↓ em cada exercício para o subir ou descer.

- **Onde:** `DayScreen.kt` (ícones `Icons.Default.KeyboardArrowUp`/`KeyboardArrowDown`), `DayViewModel`, `WorkoutRepository`, `WorkoutDao`.
- **Dica:** mover um exercício é **trocar o `position`** com o vizinho. São duas atualizações, por isso usa `database.withTransaction { }`. O DAO pode ter `@Update suspend fun updateExercises(exercises: List<Exercise>)`.

**Vais praticar:** transações, listas e índices.

### 8. Duplicar um plano
Um botão no ecrã do plano que cria uma cópia ("Push/Pull/Legs (cópia)") com todos os dias e exercícios.

- **Onde:** `WorkoutRepository.duplicatePlan(planId)` e o que for preciso no DAO e no ecrã.
- **Dica:** lê tudo, insere o plano novo e guarda o id. Depois, para cada dia, insere uma cópia com `copy(id = 0, planId = novoId)` e faz o mesmo aos exercícios. Tudo dentro de `withTransaction`.

**Vais praticar:** transações, `copy()`, ciclos.

### 9. Subir a carga a partir do treino
Levantaste mais do que o planeado? Põe um botão em cada exercício do treino: **"Usar como novo peso"**. Esse botão atualiza o `weightKg` do exercício no plano com o peso mais alto que usaste hoje.

- **Dica:** `SetLog.exerciseId` pode ser `null` (se o exercício já tiver sido apagado do plano). Nesse caso, esconde o botão.

**Vais praticar:** queries `UPDATE`, nulls, UX.

### 10. Separar o ecrã do estado (+ Preview)
Divide o `PlansScreen` em duas funções:
- `PlansScreen(viewModel)`: lê o estado do ViewModel e passa-o à de baixo.
- `PlansContent(uiState, onPlanClick, onCreatePlan, ...)`: só desenha. Não sabe o que é um ViewModel.

Depois cria um `@Preview` do `PlansContent` com planos inventados.

**Vais praticar:** *state hoisting*, o padrão mais importante do Compose. [Guia](https://developer.android.com/develop/ui/compose/state#state-hoisting)

---

## Nível 3: Mais a sério

### 11. Tempo de descanso (a tua primeira migração de base de dados)
Adiciona ao `Exercise` o tempo de descanso entre séries, mostra-o no ecrã do dia e deixa editá-lo no `ExerciseDialog`.

Isto muda uma tabela, por isso tens de dizer ao Room como passar da versão 1 para a 2:

1. **Antes de mudares qualquer coisa**, confirma que existe `app/schemas/com.campsc.workbuddy.data.WorkBuddyDatabase/1.json` (é criado quando compilas) e faz commit dele.
2. No `Exercise`: `@ColumnInfo(defaultValue = "90") val restSeconds: Int = 90`
3. No `WorkBuddyDatabase`: `version = 2` e `autoMigrations = [AutoMigration(from = 1, to = 2)]`
4. Compila (aparece o `2.json`) e corre **por cima** da versão antiga: os teus dados têm de continuar lá.

**Extra:** no treino, um botão que faz a contagem decrescente do descanso. Dica: `LaunchedEffect` + `delay(1000)`.

**Vais praticar:** migrações ([guia](https://developer.android.com/training/data-storage/room/migrating-db-versions)), efeitos em Compose.

### 12. Gráfico de progresso
Um ecrã novo com a evolução do peso de um exercício ao longo do tempo.

- **Dica (SQL):**
  ```sql
  SELECT sessions.startedAt AS date, MAX(set_logs.weightKg) AS maxWeightKg
  FROM set_logs
  JOIN sessions ON sessions.id = set_logs.sessionId
  WHERE set_logs.exerciseName = :name AND set_logs.reps IS NOT NULL
  GROUP BY sessions.id
  ORDER BY sessions.startedAt
  ```
- **Dica (desenho):** `Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) { drawLine(...) }`. Tens de converter datas e pesos em coordenadas x/y.
- Vais precisar de uma rota nova em `WorkBuddyNavHost.kt` e de um ViewModel novo em `AppViewModelProvider.kt`.

**Vais praticar:** navegação, desenho com Canvas, um pouco de matemática.

### 13. Exportar para Excel (CSV)
No histórico, um botão que partilha todos os treinos em formato CSV (`data;plano;dia;exercício;série;reps;kg`), para abrires no Excel ou mandares por email.

- **Dica:** constrói o texto com `buildString { appendLine(...) }` e partilha-o com um `Intent`. [Guia](https://developer.android.com/training/sharing/send)
  ```kotlin
  val intent = Intent(Intent.ACTION_SEND).apply {
      type = "text/csv"
      putExtra(Intent.EXTRA_TEXT, csv)
  }
  context.startActivity(Intent.createChooser(intent, "Exportar treinos"))
  ```
  No Compose, o `context` vem de `LocalContext.current`.

**Vais praticar:** Intents, texto, integração com outras apps.

### 14. Testes a sério
- **Fácil:** acrescenta testes a `FormatTest.kt` para as funções que criaste nos desafios.
- **Médio:** testa o `WorkoutDao` com uma base de dados em memória (`Room.inMemoryDatabaseBuilder`) em `app/src/androidTest/`. Vais precisar das dependências `androidx.test.ext:junit`, `androidx.test:runner` e `kotlinx-coroutines-test`, e de definir `testInstrumentationRunner` no `build.gradle.kts`.
- **Difícil:** testa um ViewModel com um repositório "falso". Para isso, o `WorkoutRepository` tem de passar a ser uma `interface`.

**Vais praticar:** testes, interfaces, injeção de dependências.

### 15. Atualizar as ferramentas
Atualiza o Android Studio para a versão mais recente (*Help > Check for Updates*). Depois usa o **AGP Upgrade Assistant** (*Tools > AGP Upgrade Assistant*) para passar para o Android Gradle Plugin 9. Lê o que ele muda (o Kotlin passa a vir "incluído" no AGP) e atualiza as bibliotecas em `libs.versions.toml`. O Android Studio sublinha as que têm versões novas. [Guia](https://developer.android.com/build/agp-upgrade-assistant)

**Vais praticar:** sistema de build (Gradle), ler notas de versão, resolver conflitos de versões.

---

Terminaste tudo? Então já não estás enferrujado. Algumas ideias para continuar: temporizador de treino com notificação, calculadora de discos, modo escuro manual, sincronizar com a cloud…
