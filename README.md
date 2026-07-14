# Agenda SSD — Backend

API REST da aplicação de agendamento de reuniões da **Superintendência de Saúde Digital (SSD)** — Governo do Estado de Mato Grosso do Sul. Gerencia usuários, reuniões (em três salas: Apoio, CIEGES e Sala Web) e o log de auditoria das alterações feitas em reuniões. Consumida pelo front-end em [`agenda_ssd_front`](../agenda_ssd_front).

## Funcionalidades

- **Autenticação por JWT** — login emite um token (válido por 15 minutos) que precisa ser reenviado em `Authorization: Bearer <token>` em toda chamada autenticada. Consultar a agenda (`GET /api/meeting`) continua público, sem login — é exibida em vários lugares só pra consulta.
- **Cadastro de usuário restrito ao admin**, sem autoatendimento: o admin escolhe nome, email, matrícula (opcional) e role na criação. O sistema gera uma **senha temporária** (mostrada uma única vez pro admin copiar e repassar) e força a troca no primeiro login.
- **Gestão de acessos** (admin): listar, buscar, trocar role, desativar/reativar (sem apagar histórico) e resetar senha de qualquer usuário — com trava pra impedir que o admin mexa na própria conta por esses endpoints.
- **Autoatendimento de conta**: qualquer usuário logado troca o próprio nome/email/senha, sempre confirmando a senha atual.
- **Agendamento de reuniões**, com validação de horário (início antes do término) e bloqueio de sobreposição na mesma sala. O dono é sempre resolvido a partir do usuário autenticado, nunca de um campo enviado pelo cliente.
- **Edição e exclusão de reunião**, restritas ao dono da reunião ou a usuários com role `ADMIN`.
- **Log de auditoria** de criação, edição e exclusão de reuniões (admin-only), consultável por reunião, por usuário ou por período.
- **Health check** (`/api/health`) com status da aplicação, uptime, memória e status do banco — público.
- **Documentação interativa** via Swagger UI.

## Stack

- [Spring Boot 3.5.4](https://spring.io/projects/spring-boot) + Java 21
- Spring Data JPA + [H2](https://www.h2database.com/) em modo arquivo (persistente em `./data/agendadb.mv.db`)
- **Spring Security + JWT** ([jjwt](https://github.com/jwtk/jjwt) 0.13.0) — API stateless, sem sessão/cookie
- [springdoc-openapi](https://springdoc.org/) (Swagger UI)

## Como rodar localmente

Pré-requisito: JDK 21 instalado (o Maven Wrapper cuida do Maven em si).

1. Suba a aplicação:

   ```bash
   ./mvnw spring-boot:run
   ```

   Sem nenhuma variável de ambiente, sobe no perfil `dev` (ver [Perfis de ambiente](#perfis-de-ambiente) abaixo).

2. A API sobe em [http://localhost:8080](http://localhost:8080). O banco H2 é criado automaticamente em `./data/agendadb.mv.db` na primeira execução, com dados de exemplo carregados via `data.sql`.

3. Endpoints úteis em desenvolvimento:
   - Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
   - Console H2: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:file:./data/agendadb`, usuário `sa`, sem senha) — só existe no perfil `dev`.

## Perfis de ambiente

| Arquivo | Quando é usado |
|---|---|
| `application.properties` | Sempre carregado — configurações comuns (datasource, JPA, tempo de expiração do token). |
| `application-dev.properties` | Ativo por padrão (`spring.profiles.default=dev`) quando nenhum perfil é especificado. Console H2 aberto, log SQL/bind params verboso, `JWT_SECRET`/`CORS_ALLOWED_ORIGINS` com fallback fixo pra rodar sem configurar nada. |
| `application-prod.properties` | Ativar explicitamente com `SPRING_PROFILES_ACTIVE=prod`. Console H2 fechado, log discreto, e **sem fallback** pra `JWT_SECRET`/`CORS_ALLOWED_ORIGINS` — a aplicação recusa subir se essas variáveis não estiverem definidas no ambiente. |

Pra rodar como em produção:

```bash
JWT_SECRET="um-segredo-forte-com-pelo-menos-32-bytes" \
CORS_ALLOWED_ORIGINS="https://dominio-do-front-em-producao" \
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

Sem essas duas variáveis, a subida falha de propósito (erro de placeholder não resolvido) — é melhor do que rodar em produção com o segredo de dev que está no repositório ou aceitando qualquer origem.

## Scripts (Maven Wrapper)

| Comando | O que faz |
|---|---|
| `./mvnw spring-boot:run` | Sobe a aplicação (perfil `dev` por padrão) |
| `./mvnw clean package` | Gera o `.jar` de produção em `target/` |
| `./mvnw test` | Roda os testes |

## Endpoints da API

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| `POST` | `/api/user/login` | Autentica por email/senha, retorna `{id, name, email, role, token, mustChangePassword}` | Público |
| `POST` | `/api/user` | Cadastra usuário (`name`, `email`, `matricula?`, `role`); gera senha temporária | ADMIN |
| `GET` | `/api/user` | Lista todos os usuários | ADMIN |
| `GET` | `/api/user/search?q=` | Busca usuário por nome ou email | ADMIN |
| `PUT` | `/api/user/{id}` | Atualiza dados/role de um usuário | ADMIN |
| `DELETE` | `/api/user/{id}` | Desativa um usuário (não apaga — preserva histórico) | ADMIN |
| `POST` | `/api/user/{id}/reactivate` | Reativa um usuário desativado | ADMIN |
| `POST` | `/api/user/{id}/reset-password` | Gera nova senha temporária pro usuário | ADMIN |
| `PUT` | `/api/user/me` | Atualiza o próprio nome/email (exige senha atual) | Autenticado |
| `PUT` | `/api/user/me/password` | Troca a própria senha (exige senha atual) | Autenticado |
| `GET` | `/api/meeting` | Lista todas as reuniões | Público |
| `GET` | `/api/meeting/{id}` | Busca uma reunião pelo ID | Público |
| `POST` | `/api/meeting` | Cadastra uma reunião (dono = usuário autenticado) | Autenticado |
| `PUT` | `/api/meeting/{id}` | Atualiza uma reunião | Dono ou ADMIN |
| `DELETE` | `/api/meeting/{id}` | Remove uma reunião | Dono ou ADMIN |
| `GET` | `/api/logs`, `/api/logs/meeting/{id}`, `/api/logs/user/{id}`, `/api/logs/period` | Logs de auditoria | ADMIN |
| `GET` | `/api/health` | Status da aplicação e do banco | Público |

Detalhes de request/response de cada endpoint estão no Swagger UI.

## Regras de negócio

- Horário de início de uma reunião deve ser anterior ao horário de término.
- Não é permitido agendar duas reuniões na mesma sala com horários sobrepostos.
- Apenas o usuário dono da reunião (`hostUser`) ou um usuário com role `ADMIN` pode editar ou excluir uma reunião.
- Ninguém desativa a própria conta, troca a própria role ou reseta a própria senha pelos endpoints de admin — isso é o que garante que o sistema nunca fica sem nenhum `ADMIN`.
- Usuário desativado não consegue logar, e uma sessão já aberta perde acesso na próxima chamada (o filtro JWT reconsulta o status a cada requisição).

## Estrutura do projeto

```
src/main/java/com/ssd/agenda_SSD_back/
├─ controller/   # UserController, MeetingController, LogUpdateController, HealthController
├─ service/      # UserService, MeetingService, LogUpdateService
├─ repository/   # interfaces JpaRepository (User, Meeting, LogUpdate)
├─ entity/       # User, Meeting, LogUpdate
├─ dto/          # DTOs de request/response, com toEntity/fromEntity estáticos
├─ security/     # JwtService, JwtAuthenticationFilter, CustomUserDetailsService
├─ config/       # SecurityConfig (CORS, regras de autorização, tratamento de 401/403)
├─ enums/        # UserRole (USER, ADMIN)
├─ exception/    # exceções de domínio (NotFoundException, ScheduleOverlapException, ...)
├─ advice/       # GeneralControllerAdvice — tratamento padronizado de erro
└─ util/         # EmailValidator, LogUtils, PasswordGenerator
```

## Estado atual / limitações conhecidas

- **Sem revogação de token**: o JWT é stateless — trocar a senha não invalida tokens já emitidos, que continuam válidos até expirar (15 min). Mitigado pela janela curta, não resolvido de verdade (exigiria uma blocklist).
- **`data.sql` mistura dado real com seed de dev**: tem nomes/emails institucionais reais com hash bcrypt real, junto com usuários de teste. Precisa de atenção antes de apontar esse projeto pra um banco que não seja o de desenvolvimento local.
- **Sem testes automatizados**: toda a superfície de autenticação, gestão de acessos e senha temporária foi validada manualmente — nada está protegido contra regressão por CI.
- **Sem migração de schema versionada** (Flyway/Liquibase): `ddl-auto=update` cria/ajusta tabelas automaticamente, o que não é ideal pra um ambiente de produção de verdade.
- Checagem de sobreposição de horário é check-then-act sem lock — pode falhar sob concorrência real.
