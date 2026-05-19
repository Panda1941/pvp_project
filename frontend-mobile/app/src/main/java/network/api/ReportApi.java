package network.api;
import com.example.accidentreportingapp.models.AccidentReport;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ReportApi {
    @POST("/api/reports")
    Call<AccidentReport> createReport(@Body AccidentReport report);

    @GET("/api/reports")
    Call<List<AccidentReport>> getReports();

    @GET("/api/reports/{id}")
    Call<AccidentReport> getReport(@Path("id") Long id);
}
