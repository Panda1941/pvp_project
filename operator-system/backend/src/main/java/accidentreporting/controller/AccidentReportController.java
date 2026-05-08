package accidentreporting.controller;

import accidentreporting.model.AccidentReport;
import accidentreporting.repository.AccidentReportRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class AccidentReportController {

    private final AccidentReportRepository repo;

    public AccidentReportController(AccidentReportRepository repo) { this.repo = repo; }

    @GetMapping
    public List<AccidentReport> list() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccidentReport> getOne(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AccidentReport> create(@RequestBody AccidentReport dto) {
        AccidentReport saved = repo.save(dto);
        return ResponseEntity.ok(saved);
    }
}
