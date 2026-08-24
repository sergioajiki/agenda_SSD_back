package com.ssd.agenda_SSD_back.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Backup do banco H2 via o comando nativo {@code BACKUP TO} — uma operação
 * online, que roda com a aplicação no ar e não bloqueia escritas. Existe
 * porque a única cópia do banco até agora dependia de alguém copiar o
 * arquivo {@code .mv.db} manualmente — e uma vez essa cópia já saiu
 * incompleta por ter sido feita com a aplicação ainda aberta (ver runbook
 * de hospedagem).
 *
 * Três caminhos:
 * - {@link #scheduledBackup()} — agendado (ver {@code backup.cron}), grava
 *   sempre em {@code backup.dir} e aplica a limpeza de backups antigos.
 * - {@link #manualBackup(String)} — sob demanda (ver {@code BackupController}),
 *   aceita uma pasta customizada e nunca apaga nada — quem pede um backup
 *   avulso decide o que fazer com ele depois.
 * - {@link #createTempBackup()} — sob demanda, pra download direto no
 *   navegador do admin (fica fora do servidor de verdade) — grava numa
 *   pasta temporária que o próprio chamador apaga depois de ler o
 *   conteúdo, não polui {@code backup.dir} nem some limitado.
 */
@Service
public class DatabaseBackupService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseBackupService.class);
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final String FILE_PREFIX = "agendadb_backup_";
    private static final String FILE_SUFFIX = ".zip";

    private final JdbcTemplate jdbcTemplate;

    @Value("${backup.dir:./backups}")
    private String defaultBackupDir;

    @Value("${backup.keep:4}")
    private int backupsToKeep;

    public DatabaseBackupService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(cron = "${backup.cron:0 0 3 * * SUN}")
    public void scheduledBackup() {
        try {
            Path created = createBackup(defaultBackupDir);
            log.info("Backup agendado do banco criado: {}", created);
            cleanupOldBackups(Paths.get(defaultBackupDir));
        } catch (IOException e) {
            log.error("Falha ao criar o backup agendado do banco", e);
        }
    }

    /**
     * Backup avulso, sob demanda — não aplica a limpeza de backups antigos
     * (só o backup agendado faz isso). {@code customDir} nulo/vazio cai no
     * mesmo diretório padrão do backup agendado.
     */
    public Path manualBackup(String customDir) throws IOException {
        String targetDir = (customDir == null || customDir.isBlank()) ? defaultBackupDir : customDir;
        Path created = createBackup(targetDir);
        log.info("Backup manual do banco criado: {}", created);
        return created;
    }

    /**
     * Gera um backup numa pasta temporária do sistema — quem chama é
     * responsável por ler o conteúdo e apagar o arquivo (e a pasta) logo em
     * seguida. Existe só pro fluxo de download direto no navegador
     * (BackupController.downloadBackup) — não fica de pé no servidor.
     */
    public Path createTempBackup() throws IOException {
        Path tempDir = Files.createTempDirectory("agenda-backup-");
        return createBackup(tempDir.toString());
    }

    private Path createBackup(String targetDir) throws IOException {
        Path dir = Paths.get(targetDir);
        Files.createDirectories(dir);

        String filename = FILE_PREFIX + LocalDateTime.now().format(TIMESTAMP) + FILE_SUFFIX;
        Path target = dir.resolve(filename);

        // Aspas simples dentro do caminho quebrariam a sintaxe do comando —
        // escapa dobrando, como o próprio H2 espera em literais SQL.
        String escapedPath = target.toAbsolutePath().toString().replace("'", "''");
        jdbcTemplate.execute("BACKUP TO '" + escapedPath + "'");

        return target;
    }

    private void cleanupOldBackups(Path dir) throws IOException {
        List<Path> backups;
        try (Stream<Path> files = Files.list(dir)) {
            backups = files
                    .filter(p -> p.getFileName().toString().startsWith(FILE_PREFIX))
                    .sorted(Comparator.comparing(this::lastModifiedSafe).reversed())
                    .collect(Collectors.toList());
        }

        for (Path old : backups.subList(Math.min(backupsToKeep, backups.size()), backups.size())) {
            Files.deleteIfExists(old);
            log.info("Backup agendado antigo removido: {}", old);
        }
    }

    private FileTime lastModifiedSafe(Path p) {
        try {
            return Files.getLastModifiedTime(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
