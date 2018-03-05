package com.distnet.gstark31897.distnet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class NewContactActivity extends AppCompatActivity {
    final int SELECT_FILE_CODE = 0;

    EditText identityInput;
    Button submitButton;
    Button scanQrCodeButton;
    Button openQrCodeButton;

    IntentIntegrator integrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_contatct);

        setTitle(R.string.new_contact_title);

        identityInput = (EditText)findViewById(R.id.identity_input);
        submitButton = (Button)findViewById(R.id.identity_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra(MainActivity.NEW_CONTACT_IDENTITY, identityInput.getText().toString());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Scan a QR code");
        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
        integrator.setOrientationLocked(true);

        scanQrCodeButton = (Button)findViewById(R.id.scan_qr_code);
        scanQrCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                integrator.initiateScan();
            }
        });

        openQrCodeButton = (Button)findViewById(R.id.open_qr_code);
        openQrCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "Select an Image"), SELECT_FILE_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_FILE_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "selected file", Toast.LENGTH_LONG).show();
            Uri uri = data.getData();
            readQrFromFile(uri);
        } else if (requestCode == IntentIntegrator.REQUEST_CODE && resultCode == RESULT_OK) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if(result != null) {
                if(result.getContents() == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                    identityInput.setText(result.getContents());
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void readQrFromFile(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            int[] data = new int[bitmap.getWidth() * bitmap.getHeight()];
            bitmap.getPixels(data, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

            LuminanceSource luminance = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), data);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(luminance));

            Reader reader = new MultiFormatReader();
            Result result = reader.decode(binaryBitmap);
            identityInput.setText(result.getText());
        } catch (IOException e) {
            Toast.makeText(this, "Unable to load image", Toast.LENGTH_LONG).show();
        } catch (FormatException e) {
            Toast.makeText(this, "Improper image format", Toast.LENGTH_LONG).show();
        } catch (ChecksumException e) {
            Toast.makeText(this, "QR code checksum failed", Toast.LENGTH_LONG).show();
        } catch (NotFoundException e) {
            Toast.makeText(this, "Unable to decode QR code", Toast.LENGTH_LONG).show();
        }
    }
}
