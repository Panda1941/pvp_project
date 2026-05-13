package accidentreporting.repository;

import accidentreporting.model.AccidentReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccidentReportRepository extends JpaRepository<AccidentReport, Long> {
}
