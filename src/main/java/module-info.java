module com.ameen.meal_chooser {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.ameen.meal_chooser to javafx.fxml;
    exports com.ameen.meal_chooser;
}