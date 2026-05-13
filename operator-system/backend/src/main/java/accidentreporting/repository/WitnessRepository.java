package accidentreporting.repository;

import accidentreporting.model.Witness;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WitnessRepository extends JpaRepository<Witness, Long> {
}
