package com.selvendran.smarthome;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sensor")
@CrossOrigin
public class SensorController {

    private static final DateTimeFormatter CSV_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final SensorDataRepository repository;

    public SensorController(SensorDataRepository repository) {
        this.repository = repository;
    }

    /* =====================================
       ESP32 SENDS SENSOR DATA HERE
       POST http://SERVER_IP:8080/sensor
    ====================================== */
    @PostMapping
    public SensorData saveData(@RequestBody SensorData data) {
        return repository.save(data);
    }

    /* =====================================
       GET LATEST SENSOR DATA
       GET http://SERVER_IP:8080/sensor/latest
    ====================================== */
    @GetMapping("/latest")
    public ResponseEntity<?> getLatest() {
        List<SensorData> list = repository.findAll();
        if (list.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(list.get(list.size() - 1));
    }

    /* =====================================
       GET FULL SENSOR HISTORY
       GET http://SERVER_IP:8080/sensor/all
    ====================================== */
    @GetMapping("/all")
    public List<SensorData> getAll() {
        return repository.findAll();
    }

    /**
     * Recent readings for dashboards (newest first). Filtered by {@code recordedAt} when set;
     * rows with no timestamp (legacy) are included if they appear in the fetched page.
     */
    @GetMapping("/history")
    public List<SensorData> getHistory(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "500") int limit) {
        return loadHistoryWindow(days, limit);
    }

    /**
     * CSV aligned for Excel: UTF-8 BOM, fixed column order, RFC-style numeric/boolean fields.
     * Two paths: some setups handle {@code /export.csv} oddly; {@code /export-csv} is the safe alias.
     */
    @GetMapping(value = {"/export-csv", "/export.csv"}, produces = "text/csv;charset=UTF-8")
    public ResponseEntity<String> exportCsv(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "2000") int limit) {
        List<SensorData> rows = loadHistoryWindow(days, limit);
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("id,recorded_at_iso,temperature_c,humidity_pct,motion,flame,mq2_smoke,mq6_lpg,ldr_value,soil_moisture,water_level,mq7_gas,ultrasonic_cm\n");
        for (SensorData s : rows) {
            sb.append(s.getId()).append(',');
            sb.append(s.getRecordedAt() != null ? CSV_TIME.format(s.getRecordedAt()) : "").append(',');
            sb.append(s.getTemperature()).append(',');
            sb.append(s.getHumidity()).append(',');
            sb.append(s.isMotionDetected()).append(',');
            sb.append(s.isFlameDetected()).append(',');
            sb.append(s.getMq2Smoke()).append(',');
            sb.append(s.getMq6Lpg()).append(',');
            sb.append(s.getLdrValue()).append(',');
            sb.append(s.getSoilMoisture()).append(',');
            sb.append(s.getWaterLevel()).append(',');
            sb.append(s.getMq7Gas()).append(',');
            sb.append(s.getUltraSonic()).append('\n');
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sensor_history.csv\"")
                .body(sb.toString());
    }

    private List<SensorData> loadHistoryWindow(int days, int limit) {
        int safeDays = Math.min(Math.max(days, 1), 366);
        int safeLimit = Math.min(Math.max(limit, 1), 5000);
        LocalDateTime cutoff = LocalDateTime.now().minusDays(safeDays);
        Pageable page = PageRequest.of(0, safeLimit, Sort.by("id").descending());
        return repository.findAll(page).getContent().stream()
                .filter(s -> s.getRecordedAt() == null || !s.getRecordedAt().isBefore(cutoff))
                .sorted((a, b) -> {
                    if (a.getRecordedAt() != null && b.getRecordedAt() != null) {
                        return b.getRecordedAt().compareTo(a.getRecordedAt());
                    }
                    long ida = a.getId() != null ? a.getId() : 0L;
                    long idb = b.getId() != null ? b.getId() : 0L;
                    return Long.compare(idb, ida);
                })
                .collect(Collectors.toList());
    }
}
