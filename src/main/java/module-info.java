module dev.lego.app {
    requires javafx.controls;
    requires javafx.graphics;

    exports dev.lego.app;
    opens dev.lego.app to javafx.graphics;
}
