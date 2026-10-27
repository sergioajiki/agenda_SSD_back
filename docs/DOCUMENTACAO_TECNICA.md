# Agenda SSD — Backend: documentação técnica

Complementa o [README](../README.md) (que cobre como rodar, endpoints e regras de negócio) com **como as coisas funcionam por dentro e por quê**. Front-end correspondente: [`agenda_ssd_front/docs/DOCUMENTACAO_TECNICA.md`](../../agenda_ssd_front/docs/DOCUMENTACAO_TECNICA.md).

Atualizado em 27/10/2026.

---

## 1. Visão geral

- **Spring Boot 3.5.4 / Java 21**, API REST **stateless** (sem sessão HTTP nem cookie).
- **H2 em modo arquivo** (`./data/agendadb.mv.db`) — é o único banco, sem servidor de banco separado. O schema é criado/ajustado por `spring.jpa.hibernate.ddl-auto=update` (sem Flyway/Liquibase).
- Hospedagem: um notebook dedicado na rede local, com o front-end (Next.js) na mesma máquina. Ver o runbook de hospedagem.

```
Navegador (agenda / painel)  ──┐
                               ├──►  Spring Boot :8080  ──►  H2 (data/agendadb.mv.db)
TV / monitor (/display)      ──┘          │
                                          └──► backups/ (BACKUP TO, semanal)
```

### Perfis

| Perfil | Ativação | Diferenças |
|---|---|---|
| `dev` | padrão (`spring.profiles.default=dev`) | console H2 aberto, log SQL verboso, `JWT_SECRET`/`CORS_ALLOWED_ORIGINS` com fallback fixo |
| `prod` | `SPRING_PROFILES_ACTIVE=prod` | console H2 fechado, log discreto, sem fallback de segredo/CORS |

**`data.sql`:** nenhum perfil define `spring.sql.init.mode`. No padrão do Spring Boot (`embedded`), o script só roda em banco em memória — com `jdbc:h2:file:` ele **não** roda (um banco novo sobe sem os usuários do seed). Se alguém definir `spring.sql.init.mode=always`, ele passa a rodar no banco real, que mistura dados reais com seed de dev: não fazer isso.

---

## 2. Modelo de dados

| Tabela | Entidade | Observações |
|---|---|---|
| `users` | `User` | `role` (`USER`/`ADMIN`), `enabled`, `mustChangePassword`, senha em bcrypt |
| `meetings` | `Meeting` | sala (`MeetingRoom`), data, início/fim, dono (`hostUser`) |
| `log` | `LogUpdate` | auditoria de criação/edição/exclusão de reunião |
| `display_settings` | `DisplaySettings` | **linha única** (`id = 1`): carrossel e configuração do banner |
| `display_banner` | `DisplayBanner` | **linha única** (`id = 1`): a imagem do banner |

### `display_settings`

| Coluna | Tipo | Padrão | Faixa |
|---|---|---|---|
| `seconds_per_card` | double | 6.0 | 2–15 |
| `mode` | ENUM | `MARQUEE` | `MARQUEE`, `PAGED` |
| `banner_enabled` | boolean | `false` | só `true` com imagem enviada |
| `banner_duration_seconds` | int | 15 | 5–60 |
| `banner_interval_minutes` | int | 2 | 1–30 |
| `banner_size` | varchar(20) | `FULL` | `FULL`, `QUARTER`, `NINTH`, `SIXTEENTH` |
| `banner_position` | varchar(20) | `BOTTOM_RIGHT` | `TOP_LEFT`, `TOP_RIGHT`, `BOTTOM_LEFT`, `BOTTOM_RIGHT` |
| `banner_effect` | varchar(20) | `FADE` | `FADE`, `SLIDE_UP` |

A linha nasce sozinha na primeira leitura (`createDefaultSettings`). Colunas acrescentadas depois que a linha já existia chegam `null` pelo `ddl-auto=update`; `DisplaySettingsService.applyDefaults` trata `null` como o valor padrão, então nenhuma migração manual é necessária.

### `display_banner`

| Coluna | Conteúdo |
|---|---|
| `content` | bytes do arquivo, sem alteração (`@Lob`, até 1 MB) |
| `content_type` | `image/png` ou `image/jpeg` |
| `width`, `height` | dimensões lidas do cabeçalho |
| `version` | momento do upload (epoch ms) |

**Por que no banco e não numa pasta:** o backup (`BACKUP TO`) copia só o banco — com a imagem lá dentro, ela entra no backup e na restauração sem nada a mais. **Por que numa tabela separada:** a TV consulta `GET /api/display-settings` a cada 30 s; com a imagem na mesma linha, cada consulta carregaria até 1 MB. A configuração devolve só `bannerVersion`, obtida por uma consulta que lê apenas a coluna `version` (`DisplayBannerRepository.findVersionById`).

### ENUM nativo do H2 — cuidado

O Hibernate 6 cria colunas `@Enumerated(EnumType.STRING)` como `ENUM('A','B')` no H2, e o `ddl-auto=update` **não acrescenta valores** a um ENUM existente — gravar um valor novo daria erro numa base já criada. Por isso `bannerSize`, `bannerPosition` e `bannerEffect` usam:

```java
@Enumerated(EnumType.STRING)
@JdbcTypeCode(SqlTypes.VARCHAR)
@Column(length = 20)
```

Com esse mapeamento, o próprio `ddl-auto=update` converte uma coluna que já era ENUM (`alter column ... set data type varchar(20)`), mantendo os valores gravados. `mode` e `users.role` continuam ENUM: um valor novo nelas pede o mesmo ajuste.

---

## 3. Segurança

### JWT

- `JwtService` assina com HMAC-SHA (`jwt.secret`), `sub` = e-mail, claim `role`, validade `jwt.expiration-ms` (900000 = 15 min).
- `JwtAuthenticationFilter` roda antes do filtro de login: lê `Authorization: Bearer`, carrega o usuário pelo e-mail e só autentica se o token for válido **e** o usuário estiver `enabled` — desativar alguém corta o acesso na próxima requisição, mesmo com token dentro da validade.
- **Token inválido/expirado não gera erro no filtro**: a requisição segue como anônima. Rotas públicas (ex.: `GET /api/meeting`) respondem normalmente; rotas protegidas caem no 401 do `SecurityConfig`.

### Rotas públicas (`SecurityConfig`)

`OPTIONS /**`, `POST /api/user/login`, `GET /api/meeting/**`, `GET /api/display-settings`, `GET /api/display-settings/banner`, `/api/health/**`, Swagger, `/error`, `/h2-console/**`. Todo o resto exige token; restrições por role ficam em `@PreAuthorize` nos controllers.

### Renovação de token (sessão por inatividade)

`POST /api/user/refresh-token` (autenticado — **não** é público):

1. Só passa com um token ainda válido (um token vencido não "ressuscita" a sessão).
2. `UserService.getActiveUserByEmail` recarrega o usuário e recusa desativado (401).
3. Gera um token novo com a validade completa e devolve o mesmo `LoginResponseDto` do login. A role é relida do banco, então uma troca de role passa a valer na renovação seguinte.

Quem decide **quando** renovar é o front (só enquanto há atividade do usuário). O limite de inatividade do front não deve passar de `jwt.expiration-ms`.

### Formato de erro

401 e 403 do Spring Security e todos os erros tratados em `GeneralControllerAdvice` devolvem o mesmo corpo (`Problem`): `{status, message, detail, errors}`, em UTF-8.

O login é exceção: responde **sem corpo** — 401 para senha errada ou usuário desativado, 400 para e-mail não cadastrado. O front mostra a mesma mensagem nos casos, para não revelar quais e-mails existem.

---

## 4. Tela de recepção e banner

### Configuração (`DisplaySettingsService`)

- `PUT /api/display-settings` exige `secondsPerCard` e `mode`. **Campos de banner ausentes (`null`) mantêm o valor atual** — o formulário do letreiro salva sem apagar a configuração do banner, e o botão "Pausar/Retomar" envia só `bannerEnabled`.
- Validações: velocidade 2–15 s, duração 5–60 s (teto pensado na queima de tela), intervalo 1–30 min, `bannerEnabled=true` só com imagem ("Envie uma imagem antes de ativar o banner.").
- `bannerVersion` é somente leitura (ignorado no PUT).
- `setBannerEnabled(boolean)` liga/desliga sem a checagem de imagem — usado pelo `DisplayBannerService`, que acabou de gravar ou apagar a imagem.

### Upload (`DisplayBannerService.upload`)

Ordem das validações (todas respondem 400 com mensagem em português):

| Checagem | Mensagem |
|---|---|
| sem arquivo / campo `file` ausente | "Selecione uma imagem para enviar." |
| acima de 1 MB | "A imagem passa de 1 MB." |
| primeiros bytes não são PNG (`89 50 4E 47`) nem JPG (`FF D8 FF`) | "Formato não aceito. Envie um arquivo PNG ou JPG." |
| cabeçalho ilegível | "Não foi possível ler a imagem. O arquivo pode estar corrompido." |
| mais de 8000 px num lado | "A imagem tem W x H px. O máximo é 8000 px de cada lado." |

- O formato vem dos **bytes**, não da extensão nem do tipo informado pelo navegador (os dois mudam com um renomear). SVG fica de fora de propósito (pode carregar script).
- As dimensões são lidas **só do cabeçalho** (`ImageReader`), sem decodificar a imagem: um PNG de 1 MB pode declarar dimensões gigantes e estourar a memória de quem decodificar — inclusive a TV.
- Grava na linha `id = 1` com `version = System.currentTimeMillis()`.
- **Primeira imagem liga o banner**; numa troca de imagem o estado fica como estava (pausado continua pausado). `DELETE` apaga a imagem e desliga o banner.

### Limites de upload

```properties
spring.servlet.multipart.max-file-size=1MB
spring.servlet.multipart.max-request-size=2MB
server.tomcat.max-swallow-size=10MB
```

Sem o `max-swallow-size`, um upload bem acima de 1 MB faz o Tomcat derrubar a conexão e o navegador só vê "erro de rede". Com ele, arquivos até 10 MB recebem o 400 com mensagem (`MaxUploadSizeExceededException` tratado no advice).

### Download e cache

`GET /api/display-settings/banner?v=<versão>`:

- `v` igual à versão atual → `Cache-Control: max-age=365d, public, immutable`. Imagem nova = versão nova = URL nova, então o cache permanente nunca mostra a imagem antiga.
- Sem `v` ou com versão antiga → `no-cache`.
- Sem imagem → 404.

---

## 5. Backup

`DatabaseBackupService` usa o comando nativo `BACKUP TO` do H2 (online, não bloqueia escrita). Três caminhos:

| Caminho | Gatilho | Destino | Limpeza |
|---|---|---|---|
| agendado | `backup.cron` (domingo 3h) | `backup.dir` (`./backups`) | mantém os últimos `backup.keep` (4) |
| avulso | `POST /api/backup` | `backup.dir` ou pasta informada | nunca apaga |
| download | `POST /api/backup/download` | pasta temporária, apagada depois | — |

A imagem do banner está no banco, então já vai em qualquer backup.

---

## 6. Operação

- **Build na máquina de hospedagem: sempre `./mvnw clean package -DskipTests`.** O teste `contextLoads()` sobe o contexto completo no perfil `dev` contra o mesmo arquivo de banco, e o `ddl-auto=update` pode alterar o banco real (em 24/08 o arquivo mudou depois de um build e foi restaurado do backup).
- **Banco versionado no git**: `data/agendadb.mv.db` e `backups/*.zip` estão no repositório. No notebook, antes de `git pull`: parar o backend, copiar o banco para fora do repositório, fazer o pull e devolver a cópia. Não usar `git rm --cached` no banco sem combinar antes — o pull no notebook apagaria o arquivo.
- **Mudanças de schema** acontecem sozinhas ao subir (`ddl-auto=update`): faça um backup antes de subir uma versão nova.
- Subir em produção: `SPRING_PROFILES_ACTIVE=prod` + `JWT_SECRET` + `CORS_ALLOWED_ORIGINS`.

### Testar sem tocar no banco de desenvolvimento

```bash
./mvnw -q spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081 \
  --spring.datasource.url=jdbc:h2:file:/caminho/scratch/testdb;MODE=LEGACY;AUTO_SERVER=TRUE"
```

Sobe uma segunda instância na porta 8081 com um banco descartável (`--jwt.expiration-ms=20000` ajuda a testar expiração). Num banco novo, o `data.sql` pode não criar usuários de teste — inserir via JDBC. O `spring-boot:run` deixa um processo java filho vivo ao ser interrompido: encerre-o pelo PID.

---

## 7. Configuração (`application.properties`)

| Propriedade | Valor | Para quê |
|---|---|---|
| `spring.datasource.url` | `jdbc:h2:file:./data/agendadb;...;MODE=LEGACY` | banco em arquivo |
| `spring.jpa.hibernate.ddl-auto` | `update` | schema automático |
| `jwt.expiration-ms` | `900000` | validade do token (= limite de inatividade) |
| `backup.cron` / `backup.dir` / `backup.keep` | `0 0 3 * * SUN` / `./backups` / `4` | backup agendado |
| `spring.servlet.multipart.*` | 1MB / 2MB | upload do banner |
| `server.tomcat.max-swallow-size` | `10MB` | 400 limpo em vez de conexão derrubada |
| `jwt.secret`, `cors.allowed-origins` | por perfil | `prod` exige as variáveis de ambiente |
