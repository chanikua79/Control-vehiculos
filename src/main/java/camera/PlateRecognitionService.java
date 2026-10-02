package camera;
import java.util.Optional;
public interface PlateRecognitionService { Optional<PlateDetection> reconocer(byte[] imagen); }
