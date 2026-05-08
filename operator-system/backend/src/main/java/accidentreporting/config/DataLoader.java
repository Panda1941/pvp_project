package accidentreporting.config;

import accidentreporting.model.User;
import accidentreporting.model.AccidentReport;
import accidentreporting.model.Vehicle;
import accidentreporting.model.Driver;
import accidentreporting.model.Witness;
import accidentreporting.model.Photo;
import accidentreporting.model.Damage;
import accidentreporting.repository.UserRepository;
import accidentreporting.repository.AccidentReportRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccidentReportRepository reportRepository;

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "admin@local";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword("password");
            admin.setRole("ADMIN");
            admin.setFleetId(null);
            userRepository.save(admin);
            System.out.println("Inserted admin user: " + adminEmail);
        } else {
            System.out.println("Admin user already exists: " + adminEmail);
        }

        // Seed a couple of sample reports if none exist
        if (reportRepository.count() == 0) {
            AccidentReport r1 = new AccidentReport();
            r1.setAddress("Vilniaus g. 10, Vilnius");
            r1.setLocation("Intersection near supermarket");
            r1.setDescription("Rear-end collision during slow traffic.");
            r1.setIsDraft(false);

            Driver d1 = new Driver();
            d1.setFirstName("Jonas");
            d1.setLastName("Jokubaitis");
            d1.setContact("+37061234567");

            Vehicle v1 = new Vehicle();
            v1.setVehicleRegistration("ABC123");
            v1.setVehicleCountry("LT");
            v1.setDriver(d1);

            Driver d2 = new Driver();
            d2.setFirstName("Petras");
            d2.setLastName("Petraitis");
            d2.setContact("+37069876543");

            Vehicle v2 = new Vehicle();
            v2.setVehicleRegistration("XYZ987");
            v2.setVehicleCountry("LT");
            v2.setDriver(d2);

            r1.setVehicleA(v1);
            r1.setVehicleB(v2);

            Witness w = new Witness();
            w.setFirstName("Rasa");
            w.setLastName("Kazlauskiene");
            w.setPhone("+37060011122");
            r1.getWitnesses().add(w);

            Photo p = new Photo();
            p.setUrl("/uploads/sample1.jpg");
            p.setDescription("Damage overview");
            p.setOrd(1);
            r1.getPhotos().add(p);

            Damage dam = new Damage();
            dam.setArea("rear bumper");
            dam.setSeverity("minor");
            r1.getDamages().add(dam);

            reportRepository.save(r1);

            // second report
            AccidentReport r2 = new AccidentReport();
            r2.setAddress("Kauno g. 5, Kaunas");
            r2.setLocation("Roundabout near station");
            r2.setDescription("Side-swipe while changing lanes.");
            r2.setIsDraft(false);

            Driver d3 = new Driver();
            d3.setFirstName("Linda");
            d3.setLastName("Lukas");
            d3.setContact("+37061222233");

            Vehicle v3 = new Vehicle();
            v3.setVehicleRegistration("LMN456");
            v3.setVehicleCountry("LT");
            v3.setDriver(d3);

            r2.setVehicleA(v3);
            reportRepository.save(r2);

            System.out.println("Seeded sample accident reports");
        }
    }
}
