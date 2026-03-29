import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TimePicker;

public class ReservationFormView {
    private DatePicker datePicker;
    private TimePicker timePicker;
    private ComboBox<String> roomComboBox;
    private ComboBox<String> equipmentComboBox;

    public ReservationFormView() {
        datePicker = new DatePicker();
        timePicker = new TimePicker();
        roomComboBox = new ComboBox<>();
        equipmentComboBox = new ComboBox<>();
    }
}