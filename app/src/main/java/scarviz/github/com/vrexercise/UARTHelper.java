package scarviz.github.com.vrexercise;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;
import com.google.gson.Gson;

import java.io.IOException;

/**
 * Created by satoshi on 2017/03/18.
 */

public class UARTHelper {
    private static final String TAG = UARTHelper.class.getSimpleName();
    private static UARTHelper instance;

    // UART Configuration Parameters
    private static final int BAUD_RATE = 9600;
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = 1;

    private static final int CHUNK_SIZE = 64;

    private static final String POST_URL = "http://192.168.43.209:9090/speed";

    private PeripheralManagerService mService = new PeripheralManagerService();

    private HandlerThread mInputThread;
    private Handler mInputHandler;

    private UartDevice mLoopbackDevice;

    private Runnable mTransferUartRunnable = new Runnable() {
        @Override
        public void run() {
            transferUartData();
        }
    };

    public static class Data {
        public float speed;
    }

    private UARTHelper() {
    }

    public static UARTHelper getInstance() {
        if (instance == null) {
            instance = new UARTHelper();
        }
        return instance;
    }

    public void open() {
        Log.d(TAG, "open");
        // Create a background looper thread for I/O
        mInputThread = new HandlerThread("InputThread");
        mInputThread.start();
        mInputHandler = new Handler(mInputThread.getLooper());

        // Attempt to access the UART device
        try {
            openUart(BoardDefaults.getUartName(), BAUD_RATE);
            // Read any initially buffered data
            mInputHandler.post(mTransferUartRunnable);
        } catch (IOException e) {
            Log.e(TAG, "Unable to open UART device", e);
        }
    }

    public void close() {
        Log.d(TAG, "close");
        // Terminate the worker thread
        if (mInputThread != null) {
            mInputThread.quitSafely();
        }

        // Attempt to close the UART device
        try {
            closeUart();
        } catch (IOException e) {
            Log.e(TAG, "Error closing UART device:", e);
        }
    }


    private UartDeviceCallback mCallback = new UartDeviceCallback() {
        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uart) {
            // Queue up a data transfer
            transferUartData();
            //Continue listening for more interrupts
            return true;
        }

        @Override
        public void onUartDeviceError(UartDevice uart, int error) {
            Log.w(TAG, uart + ": Error event " + error);
        }
    };

    private void openUart(String name, int baudRate) throws IOException {
        mLoopbackDevice = mService.openUartDevice(name);
        // Configure the UART
        mLoopbackDevice.setBaudrate(baudRate);
        mLoopbackDevice.setDataSize(DATA_BITS);
        mLoopbackDevice.setParity(UartDevice.PARITY_NONE);
        mLoopbackDevice.setStopBits(STOP_BITS);

        mLoopbackDevice.registerUartDeviceCallback(mCallback, mInputHandler);
    }

    private void closeUart() throws IOException {
        if (mLoopbackDevice != null) {
            mLoopbackDevice.unregisterUartDeviceCallback(mCallback);
            try {
                mLoopbackDevice.close();
            } finally {
                mLoopbackDevice = null;
            }
        }
    }

    private void transferUartData() {
        Log.d(TAG, "transferUartData");
        if (mLoopbackDevice != null) {
            try {
                byte[] buffer = new byte[CHUNK_SIZE];
                int read;
                StringBuilder sb = new StringBuilder();
                while ((read = mLoopbackDevice.read(buffer, buffer.length)) > 0) {
                    Log.d(TAG, "Read " + read + " bytes from peripheral");
                    Log.d(TAG, String.valueOf(buffer));
                    String uartdata = new String(buffer.clone());
                    Log.d(TAG, uartdata);
                    sb.append(uartdata);
                }

                String data = sb.toString();
                Log.d(TAG, data);
                if (TextUtils.isEmpty(data)){
                    Log.d(TAG, "nothing data");
                    return;
                }

                if(!data.contains("V")) return;

                data.replace("V", "");
                data.replace("\r\n", "");
                Log.d(TAG, data);

                if (TextUtils.isEmpty(data)) return;
                float value = Float.parseFloat(data);
                Log.d(TAG, String.valueOf(value));

                Data sendData = new Data();
                sendData.speed = value;

                Gson gson = new Gson();
                String json = gson.toJson(sendData);

                HttpClient client = new HttpClient();
                HttpClient.Resp resp = client.postJson(POST_URL, json);
                Log.d(TAG, String.valueOf(resp.code));

            } catch (IOException e) {
                Log.w(TAG, "Unable to transfer data over UART", e);
            }
        }
    }
}
