# Agenda SSD — Backend

API REST da aplicação de agendamento de reuniões da **Superintendência de Saúde Digital (SSD)** — Governo do Estado de Mato Grosso do Sul. Gerencia usuários, reuniões (em três salas: Apoio, CIEGES e Sala Web) e o log de auditoria das alterações feitas em reuniões. Consumida pelo front-end em [`agenda_ssd_front`](../agenda_ssd_front).

## Funcionalidades

- **Cadastro e login de usuário**, com senha armazenada em hash (BCrypt).
- **Agendamento de reuniões**, com validação de horário (início antes do término) e bloqueio de sobreposição na mesma sala.
- **Edição e exclusão de reunião**, restritas ao dono da reunião ou a usuários com role `ADMIN`.
- **Log de auditoria** de criação, edição e exclusão de reuniões, consultável por reunião, por usuário ou por período.
- **Health check** (`/api/health`) com status da aplicação, uptime, memória e status do banco.
- **Documentação interativa** via Swagger UI.

## Stack

- [Spring Boot 3.5.4](https://spring.io/projects/spring-boot) + Java 21
- Spring Data JPA + [H2](https://www.h2database.com/) em modo arquivo (persistente em `./data/agendadb.mv.db`)
- [springdoc-openapi](https://springdoc.org/) (Swagger UI)
- `spring-security-crypto` — usado apenas para hash de senha (`BCryptPasswordEncoder`); **não há Spring Security configurado como framework de autenticação/autorização ainda**

## Como rodar localmente

Pré-requisito: JDK 21 instalado (o Maven Wrapper cuida do Maven em si).

1. Suba a aplicação:

   ```bash
   ./mvnw spring-boot:run
   ```

2. A API sobe em [http://localhost:8080](http://localhost:8080). O banco H2 é criado automaticamente em `./data/agendadb.mv.db` na primeira execução, com dados de exemplo carregados via `data.sql`.

3. Endpoints úteis em desenvolvimento:
   - Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
   - Console H2: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:file:./data/agendadb`, usuário `sa`, sem senha)

## Scripts (Maven Wrapper)

| Comando | O que faz |
|---|---|
| `./mvnw spring-boot:run` | Sobe a aplicação em modo desenvolvimento |
| `./mvnw clean package` | Gera o `.jar` de produção em `target/` |
| `./mvnw test` | Roda os testes |

## Endpoints da API

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/user` | Cadastra um novo usuário (`name`, `email`, `password`, `matricula`, `role`) |
| `POST` | `/api/user/login` | Autentica por email/senha, retorna `{id, name, email, role}` (sem token) |
| `POST` | `/api/meeting` | Cadastra uma reunião |
| `GET` | `/api/meeting` | Lista todas as reuniões |
| `GET` | `/api/meeting/{id}` | Busca uma reunião pelo ID |
| `PUT` | `/api/meeting/{id}` | Atualiza uma reunião (dono ou ADMIN) |
| `DELETE` | `/api/meeting/{id}` | Remove uma reunião (dono ou ADMIN) |
| `GET` | `/api/logs` | Lista todos os logs de auditoria |
| `GET` | `/api/logs/meeting/{meetingId}` | Logs de uma reunião específica |
| `GET` | `/api/logs/user/{userId}` | Logs feitos por um usuário |
| `GET` | `/api/logs/period` | Logs num período (`startDate`, `startTime`, `endDate`, `endTime`) |
| `GET` | `/api/health` | Status da aplicação e do banco |

Detalhes de request/response de cada endpoint estão no Swagger UI.

## Regras de negócio

- Horário de início de uma reunião deve ser anterior ao horário de término.
- Não é permitido agendar duas reuniões na mesma sala com horários sobrepostos.
- Apenas o usuário dono da reunião (`hostUser`) ou um usuário com role `ADMIN` pode editar ou excluir uma reunião.

## Estrutura do projeto

```
src/main/java/com/ssd/agenda_SSD_back/
├─ controller/   # UserController, MeetingController, LogUpdateController, HealthController
├─ service/      # UserService, MeetingService, LogUpdateService
├─ repository/   # interfaces JpaRepository (User, Meeting, LogUpdate)
├─ entity/       # User, Meeting, LogUpdate
├─ dto/          # DTOs de request/response, com toEntity/fromEntity estáticos
├─ enums/        # UserRole (USER, ADMIN)
├─ exception/    # exceções de domínio (NotFoundException, ScheduleOverlapException, ...)
├─ advice/       # GeneralControllerAdvice — tratamento padronizado de erro
└─ util/         # EmailValidator, LogUtils (diff de campos para o log de auditoria)
```

## Estado atual / limitações conhecidas

Este backend ainda não tem autenticação real:

- Não há JWT, sessão ou qualquer token — `/api/user/login` retorna só os dados do usuário.
- Não há Spring Security: os controllers não exigem usuário autenticado nem verificam role antes de processar a requisição (inclusive `POST /api/user`, que aceita `role` livremente).
- `MeetingController` recebe `requestingUserId` como parâmetro vindo do próprio cliente, usado para checar permissão de edição/exclusão — sem verificação de identidade real.
- CORS está liberado para qualquer origem e o console H2 está exposto publicamente.

Introduzir Spring Security + JWT é o próximo passo planejado; até lá, trate esta API como adequada apenas para ambiente de desenvolvimento/rede interna, não para exposição pública.
