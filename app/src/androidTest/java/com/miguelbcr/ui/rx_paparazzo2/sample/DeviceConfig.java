package com.miguelbcr.ui.rx_paparazzo2.sample;


public abstract class DeviceConfig {

    public static final DeviceConfig GOOGLE_NEXUS = new DeviceConfig() {
        @Override
        int getMultipleImageConfirmOffset() {
            return 100;
        }
    };

    public static final DeviceConfig SAMSUNG_GALAXY = new DeviceConfig() {
        @Override
        int getMultipleImageConfirmOffset() {
            return 250;
        }
    };

    public static DeviceConfig CURRENT = GOOGLE_NEXUS;
    public static double WAIT_TIME_FUDGE_FACTOR = 1.0;

    abstract int getMultipleImageConfirmOffset();

    public long getShortWaitTime() {
        double waitTime = DeviceConfig.WAIT_TIME_FUDGE_FACTOR * 100;

        return (long) waitTime;
    }

    public long getLongWaitTime() {
        double waitTime = DeviceConfig.WAIT_TIME_FUDGE_FACTOR * 500;

        return (long) waitTime;
    }

}
