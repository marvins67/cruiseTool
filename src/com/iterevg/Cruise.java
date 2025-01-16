package com.iterevg;

import java.io.IOException;

public class Cruise {
    VolumeConfigReader volume = new VolumeConfigReader();
    //Volume volume = new Volume();
    void run() throws IOException {
        initialize();
    }

    void initialize() throws IOException {
        volume.readVolCnf();
    }
}
