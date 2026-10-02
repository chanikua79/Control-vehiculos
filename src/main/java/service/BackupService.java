package service;

import database.Database;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class BackupService {
    private final Path backupDir;
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    public BackupService(@Value("${fleet.backup.dir:backups}") String dir) {
        this.backupDir = Paths.get(dir);
    }

    @Scheduled(cron="${fleet.backup.cron:0 0 2 * * *}")
    public void scheduledBackup() {
        try { createBackup(); } catch (Exception e) { System.err.println("Backup automatico fallido: " + e.getMessage()); }
    }

    public Path createBackup() throws Exception {
        Files.createDirectories(backupDir);
        Path target=backupDir.resolve("vehiculos-"+LocalDateTime.now().format(FORMAT)+".db").toAbsolutePath().normalize();
        if(target.getParent()==null || !target.getParent().equals(backupDir.toAbsolutePath().normalize())) throw new IllegalStateException("Ruta de backup invalida");
        String escaped=target.toString().replace("'", "''");
        try(var c=Database.conectar(); var st=c.createStatement()) { st.execute("VACUUM INTO '"+escaped+"'"); }
        return target;
    }
}
