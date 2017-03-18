package scarviz.github.com.vrexercise;

import android.os.Build;
@SuppressWarnings("WeakerAccess")
public class BoardDefaults {
    private static final String DEVICE_EDISON = "edison";
    private static final String DEVICE_RPI3 = "rpi3";
    private static final String DEVICE_NXP = "imx6ul";
    /**
     * Return the UART for loopback.
     */
    public static String getUartName() {
        switch (Build.DEVICE) {
            case DEVICE_EDISON:
                return "UART1";
            case DEVICE_RPI3:
                return "UART0";
            case DEVICE_NXP:
                return "UART3";
            default:
                throw new IllegalStateException("Unknown Build.DEVICE " + Build.DEVICE);
        }
    }
}