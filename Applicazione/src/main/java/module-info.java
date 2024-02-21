module it.unipi.valtriani.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires xstream;
    requires java.sql;
    requires java.base;
    requires java.desktop;
    requires com.google.gson;
    
    opens it.unipi.valtriani.client to javafx.fxml;
    exports it.unipi.valtriani.client;
}
