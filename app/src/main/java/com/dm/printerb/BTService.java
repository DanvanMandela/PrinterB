package com.dm.printerb;

import android.graphics.Bitmap;

import java.io.InputStream;

public interface BTService {
    boolean findBTDevice(String device);

    void openBTDevice();

    void dataTransferListener(
            InputStream inputStream
    );

    void closeBTDevice();

    void showMessage(String message);

    void printText(String data);

    void printImage(Bitmap bitmap);
}
