package camera;
import org.springframework.stereotype.Service;
import java.util.Optional;
@Service
public class ManualPlateRecognitionService implements PlateRecognitionService {
    @Override public Optional<PlateDetection> reconocer(byte[] imagen){ return Optional.empty(); }
}
