package com.dm.printerb;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.dm.printerb.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private BTService btService;

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;

    private StringBuilder receipt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initialize();
        checkBluetoothPermissions();
        clicks();
        receipt();
    }

    private void initialize() {
        btService = new BTModule(this);
    }

    private void clicks() {
        binding.printText.setOnClickListener(v -> {
            if (btService.findBTDevice(binding.printerName.getText().toString())) {
                btService.openBTDevice();
                btService.printText(String.valueOf(receipt));
                btService.closeBTDevice();
            }
        });
    }

    private void checkBluetoothPermissions() {
        List<String> permissions = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            permissions.add(Manifest.permission.BLUETOOTH);
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN);
        }
        String[] array = permissions.toArray(new String[0]);
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, array, REQUEST_BLUETOOTH_PERMISSIONS);
                return;
            }
        }

    }

    @SuppressLint("DefaultLocale")
    private void receipt() {
        String restaurantName = "QuickBite Burger";
        String location = "123 Fast Lane, CityVille";
        String phoneNumber = "(555) 123-4567";
        String receiptNumber = "F123456";
        String cashierName = "Jane Cashier";

        // Menu items and prices
        String[] items = {
                "Burger Combo: $5.99",
                "French Fries: $2.49",
                "Soda: $1.50"
        };

        // Calculate the total
        double total = 5.99 + 2.49 + 1.50;

        // Get the current date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String date = dateFormat.format(new Date());

        // Construct the fast food receipt string
        receipt = new StringBuilder();
        receipt.append("===== Fast Food Receipt =====\n");
        receipt.append("\n");
        receipt.append("Restaurant: ").append(restaurantName).append("\n");
        receipt.append("Location: ").append(location).append("\n");
        receipt.append("Phone: ").append(phoneNumber).append("\n");
        receipt.append("Receipt #: ").append(receiptNumber).append("\n");
        receipt.append("Cashier: ").append(cashierName).append("\n");
        receipt.append("Date: ").append(date).append("\n");
        receipt.append("\n");
        receipt.append("============================\n");
        receipt.append("Your Order:\n");
        receipt.append("\n");
        for (String item : items) {
            receipt.append(item).append("\n");
        }
        receipt.append("\n");
        receipt.append("============================\n");
        receipt.append("\n");
        receipt.append("Total: $").append(String.format("%.2f", total)).append("\n");
        receipt.append("Payment method: Credit Card\n");
        receipt.append("Thank you for choosing QuickBite Burger!\n");
        receipt.append("\n");
        receipt.append("============================\n");

        binding.receipt.setText(receipt);
    }

    public static Bitmap resizeBitmap(Bitmap originalBitmap, int newWidth, int newHeight) {
        Bitmap resizedBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(resizedBitmap);

        Matrix matrix = new Matrix();
        matrix.setScale((float) newWidth / originalBitmap.getWidth(), (float) newHeight / originalBitmap.getHeight());

        canvas.drawBitmap(originalBitmap, matrix, null);
        return resizedBitmap;
    }
}