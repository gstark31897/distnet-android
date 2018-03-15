package com.distnet.gstark31897.distnet;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class ShareContactActivity extends AppCompatActivity {
    final int PERMISSION_CODE = 0;

    SharedPreferences settings;
    String nickname;
    String identity;
    Button shareButton;

    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_contatct);

        setTitle(R.string.share_contact_title);

        settings = getSharedPreferences("distnet", 0);
        nickname = settings.getString("nickname", "");
        identity = settings.getString("public_key", "");

        String shareString = nickname + ":" + identity;

        try {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.encodeBitmap(shareString, BarcodeFormat.QR_CODE, display.getWidth(), display.getWidth());
            ImageView imageViewQrCode = (ImageView) findViewById(R.id.qr_code_display);
            imageViewQrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        shareButton = (Button)findViewById(R.id.share_qr_code);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, PERMISSION_CODE);
                } else {
                    shareContact();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PERMISSION_CODE && resultCode == RESULT_OK) {
            shareContact();
        }
    }

    public void shareContact() {
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "contact_qr", null);
        Uri uri = Uri.parse(path);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Share Contact"));
    }
}