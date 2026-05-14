module security.service {

    requires java.desktop;
    requires miglayout.swing;

    requires image.service;

    requires com.google.gson;

    exports com.udacity.catpoint.application;
    exports com.udacity.catpoint.data;
    exports com.udacity.catpoint.service;

    opens com.udacity.catpoint.data to com.google.gson;

}