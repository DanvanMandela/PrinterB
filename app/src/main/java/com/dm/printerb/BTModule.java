package com.dm.printerb;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BTModule implements BTService {

    private BluetoothAdapter bTAdapter;
    private BluetoothSocket bTSocket;
    private BluetoothDevice bTDevice;

    private volatile boolean stopWorker;

    private int readBufferPosition;
    public static String printerId;

    private Context context;

    private OutputStream outputStream;
    private InputStream inputStream;


    Executor executor = Executors.newFixedThreadPool(10);

    public BTModule(Context context) {
        this.context = context;
        bTAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean findBTDevice(String device) {
        try {
            if (device == null || device.equals("")) {
                showMessage("Enter printer name");
                return false;
            }
            if (bTAdapter == null) {
                showMessage("No device found");
                return false;
            }
            if (!bTAdapter.isEnabled()) {
                showMessage("Bluetooth permission not granted");
                return false;
            }
            Set<BluetoothDevice> pairedDevices = bTAdapter.getBondedDevices();

            Optional<BluetoothDevice> paired =
                    pairedDevices.stream().filter(bluetoothDevice ->
                            bluetoothDevice.getName().equals(device)).findFirst();

            if (paired.isPresent()) {
                bTDevice = paired.get();
            } else {
                showMessage("Bluetooth device not found");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    @SuppressLint("MissingPermission")
    @Override
    public void openBTDevice() {
        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            bTSocket = bTDevice.createRfcommSocketToServiceRecord(uuid);
            bTSocket.connect();
            outputStream = bTSocket.getOutputStream();
            inputStream = bTSocket.getInputStream();
            dataTransferListener(bTSocket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dataTransferListener(InputStream inputStream) {
        try {
            final byte delimiter = 10;
            stopWorker = false;
            readBufferPosition = 0;
            byte[] readBuffer = new byte[1024];

            executor.execute(() -> {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = inputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            inputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(
                                            readBuffer, 0,
                                            encodedBytes, 0,
                                            encodedBytes.length
                                    );
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        showMessage(data);
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeBTDevice() {
        try {
            stopWorker = true;
            outputStream.close();
            inputStream.close();
            bTSocket.close();
            showMessage("Printing done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void printText(String data) {
        try {
            outputStream.write(data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void printImage(Bitmap bitmap) {
        try {
            ImageData imageData = ImageData.getInstance();
            imageData.init(bitmap);
            byte[] data = imageData.printDraw();
            outputStream.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
