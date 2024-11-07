package com.example.andro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import java.text.SimpleDateFormat;
import java.util.Date;


import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class MainActivity extends AppCompatActivity {

    private Connection connect;
    private String connectionResult = "";
    private TableView<String[]> tableView;
    private Button loadDataButton;
    private Button pdfButton;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private TextView[] textViews;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String PRINTER_NAME = "P810-11F1"; // printer's name(Seethawaka Regency->"BlueTooth Printer")

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the FloatingActionButton by its ID
        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton);

        // Set an OnClickListener on the FloatingActionButton
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to ViewActivity.java
                Intent intent = new Intent(MainActivity.this, ViewActivity.class);
                startActivity(intent);
            }
        });

        tableView = findViewById(R.id.tableView);
        String[] headers = {"Description", "Quantity", "Total"};
        tableView.setHeaderAdapter(new SimpleTableHeaderAdapter(this, headers));

        pdfButton = findViewById(R.id.printBill);
        loadDataButton = findViewById(R.id.button);

        textViews = new TextView[]{
                findViewById(R.id.SubTotaltextView),
                findViewById(R.id.DiscounttextView),
                findViewById(R.id.GrandTotaltextView),
                findViewById(R.id.CashtextView),
                findViewById(R.id.CardtextView)
        };

        Typeface monoTypeface = Typeface.create("monospace", Typeface.NORMAL);
        for (TextView textView : textViews) {
            textView.setTypeface(monoTypeface);
        }

        pdfButton.setOnClickListener(view -> {
            if (checkPermission()) {
                try {
                    createPDFAndPrint(textViews);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("BluetoothPrint", "Exception: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "Error printing or creating PDF", Toast.LENGTH_LONG).show();
                }
            } else {
                requestPermission();
            }
        });

        loadDataButton.setOnClickListener(view -> getDataForTableView());
    }

    public void getDataForTableView() {
        try {
            ConnectionHelper connectionHelper = new ConnectionHelper();
            connect = connectionHelper.conclass();
            if (connect != null) {
                String query = "SELECT cast(STT.PSaleTransID as varchar(max)),\n" +
                        "isnull((Select PItemName From PastryItemTable Where PItemID = STT.FK_PItemID),'') AS ItemName,\n" +
                        "cast(STT.SubTotal as varchar(max))  AS SellingPrice, \n" +
                        "cast(STT.SellingQuantity as varchar(max)) AS Quantity,\n" +
                        "cast((select sum(SubTotal) from PastrySaleTransactionTable where FK_PSaleID = STT.FK_PSaleID) as varchar(max)) AS SubTotal,\n" +
                        "cast((select sum(Discount) from PastrySaleTransactionTable where FK_PSaleID = STT.FK_PSaleID) as varchar(max)) AS Discount, \n" +
                        "cast((select sum(TotalWithTax) from PastrySaleTransactionTable where FK_PSaleID = STT.FK_PSaleID) as varchar(max)) as TotalX,\n" +
                        "cast((Select isNull(Sum(PayingAmount),0) From PastrySalePaymentTable Where PaymentType = 'Cash' AND FK_PSaleID = STT.FK_PSaleID)as varchar(max)) AS Cash,\n" +
                        "cast((Select isNull(Sum(PayingAmount),0) From PastrySalePaymentTable Where PaymentType = 'Card' AND FK_PSaleID = STT.FK_PSaleID)as varchar(max)) AS Card\n" +
                        "\n" +
                        "FROM PastrySaleTransactionTable AS STT\n" +
                        "WHERE STT.FK_PSaleID = (select top 1 PSaleID from PastrySaleTable order by PSaleID desc)\n" +
                        "order by STT.PSaleTransID ";
                Statement smt = connect.createStatement();
                ResultSet rs = smt.executeQuery(query);

                String[][] data = new String[10][3];
                int index = 0;
                boolean firstRecord = true;

                while (rs.next()) {
                    if (index < 10) {
                        data[index][0] = rs.getString("ItemName");
                        data[index][1] = rs.getString("Quantity");
                        data[index][2] = rs.getString("SellingPrice");
                        index++;
                    }

                    if (firstRecord) {
                        textViews[0].setText(rs.getString("SubTotal"));
                        textViews[1].setText(rs.getString("Discount"));
                        textViews[2].setText(rs.getString("TotalX"));
                        textViews[3].setText(rs.getString("Cash"));
                        textViews[4].setText(rs.getString("Card"));
                        firstRecord = false;
                    }
                }

                tableView.setDataAdapter(new SimpleTableDataAdapter(this, data));

            } else {
                connectionResult = "Check Connection";
                Toast.makeText(this, connectionResult, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            Log.e("DataRetrieval", "Error: " + ex.getMessage());
            Toast.makeText(this, "Error retrieving data", Toast.LENGTH_SHORT).show();
        }
    }

    /*private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }


  private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }
*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int bluetoothResult = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT);
            return result == PackageManager.PERMISSION_GRANTED && bluetoothResult == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.BLUETOOTH_CONNECT
            }, PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }
    private List<String> wrapText(String text, int maxLength) {
        List<String> lines = new ArrayList<>();
        while (text.length() > maxLength) {
            int spaceIndex = text.lastIndexOf(' ', maxLength);
            if (spaceIndex == -1) {
                spaceIndex = maxLength;
            }
            lines.add(text.substring(0, spaceIndex));
            text = text.substring(spaceIndex).trim();
        }
        lines.add(text);
        return lines;
    }




    // Your createPDFAndPrint method
    private void createPDFAndPrint(TextView[] textViews) {
        StringBuilder printData = new StringBuilder();

        // ESC/POS command to set text size
        String doubleSize = new String(new byte[]{0x1B, 0x21, 0x10});  // ESC ! n (n=48 sets double height and width)
        String normalSize = new String(new byte[]{0x1B, 0x21, 0x00});  // ESC ! n (n=0 sets normal size)
        String alignCenter = new String(new byte[]{0x1B, 0x61, 0x01});  // ESC a n (n=1 centers the text)
        String alignLeft = new String(new byte[]{0x1B, 0x61, 0x00});    // ESC a n (n=0 aligns the text to the left)

        // Header with larger font
        printData.append(alignCenter);
        printData.append(doubleSize);
        printData.append("              RANSIRI FOOD TRUCK              \n");
        printData.append("                 NARAMMALA                 \n");

        // Normal size for the rest of the content
        printData.append(normalSize);
        printData.append(" 152, Negombo Road, Narammala\n");
        printData.append("         0760982163 \n");
        printData.append("  Ransiri Bake & Sweet\n");
        printData.append("\n");

        // Add the system date and time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateTime = sdf.format(new Date());
        printData.append("Date/Time: " + currentDateTime + "\n");
        printData.append("---------------------------------------------\n");

        printData.append("Description              Qty        Total\n");
        printData.append("---------------------------------------------\n");

        // Fetch data for each item from the database and add it to the printout
        try {
            ConnectionHelper connectionHelper = new ConnectionHelper();
            Connection connect = connectionHelper.conclass();
            if (connect != null) {
                String query = "SELECT cast(STT.PSaleTransID as varchar(max)),\n" +
                        "isnull((Select PItemName From PastryItemTable Where PItemID = STT.FK_PItemID),'') AS ItemName,\n" +
                        "cast(STT.SubTotal as varchar(max))  AS SellingPrice, \n" +
                        "cast(STT.SellingQuantity as varchar(max)) AS Quantity\n" +
                        "FROM PastrySaleTransactionTable AS STT\n" +
                        "WHERE STT.FK_PSaleID = (select top 1 PSaleID from PastrySaleTable order by PSaleID desc)\n" +
                        "order by STT.PSaleTransID ";
                Statement smt = connect.createStatement();
                ResultSet rs = smt.executeQuery(query);

                while (rs.next()) {
                    String itemName = rs.getString("ItemName");
                    String quantity = rs.getString("Quantity");
                    String total = rs.getString("SellingPrice");

                    // Split item name if it's too long to fit in one line
                    List<String> wrappedItemName = wrapText(itemName, 25); // Increased wrap limit for description

                    for (int i = 0; i < wrappedItemName.size(); i++) {
                        if (i == 0) {
                            // First line, add all columns
                            printData.append(String.format("%-25s %7s %10s\n", wrappedItemName.get(i), quantity, total));
                        } else {
                            // Additional lines, only add the item name column, leave spaces for the other columns
                            printData.append(String.format("%-25s %7s %10s\n", wrappedItemName.get(i), "", ""));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("DataRetrieval", "Error: " + e.getMessage());
            Toast.makeText(this, "Error retrieving data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add the totals to the printout
        printData.append("---------------------------------------------\n");
        printData.append("\n");
        printData.append(String.format("SubTotal :%30s\n", textViews[0].getText().toString()));
        printData.append(String.format("Discount :%30s\n", textViews[1].getText().toString()));
        printData.append(doubleSize);
        printData.append(String.format("Grand Total :%28s\n", textViews[2].getText().toString()));
        printData.append("\n");
        printData.append(normalSize);
        printData.append("      *****Payment Breakdown*****      \n");

        // Conditionally add Cash and Card amounts based on availability
        String cashAmount = textViews[3].getText().toString();
        String cardAmount = textViews[4].getText().toString();

        boolean isCashAvailable = !cashAmount.isEmpty() && Double.parseDouble(cashAmount) > 0;
        boolean isCardAvailable = !cardAmount.isEmpty() && Double.parseDouble(cardAmount) > 0;

        if (isCashAvailable) {
            printData.append(String.format("Cash :%33s\n", cashAmount));
        }
        if (isCardAvailable) {
            printData.append(String.format("Card :%33s\n", cardAmount));
        }

        printData.append("---------------------------------------------\n");
        printData.append("\n");
        printData.append("Thank You, Come Again!!\n");
        printData.append("Powered By Ceylon Innovation\n");
        printData.append("\n\n\n");

        // Print via Bluetooth
        printBillViaBluetooth(printData.toString());
    }


    private String centerAlignText(String text, int lineWidth) {
        text = text.trim();
        int padding = (lineWidth - text.length()) / 2;
        String paddingSpaces = new String(new char[padding]).replace('\0', ' ');
        return paddingSpaces + text + paddingSpaces + "\n";
    }

    private void drawText(Canvas canvas, Paint paint, String text, int x, int y, int maxWidth, boolean isBold, float textSize, boolean isCentered, boolean alignRight) {
        Paint textPaint = new Paint(paint);
        textPaint.setTextSize(textSize);
        if (isBold) {
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        } else {
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        }

        // Split the text into lines that fit within maxWidth
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            if (textPaint.measureText(line + word) <= maxWidth) {
                line.append(word).append("  ");
            } else {
                // Calculate the x position based on the alignment
                float textWidth = textPaint.measureText(line.toString().trim());
                if (isCentered) {
                    canvas.drawText(line.toString().trim(), (maxWidth - textWidth) / 2 + x, y, textPaint);
                } else if (alignRight) {
                    canvas.drawText(line.toString().trim(), maxWidth - textWidth + x, y, textPaint);
                } else {
                    canvas.drawText(line.toString().trim(), x, y, textPaint);
                }
                y += textPaint.getTextSize();
                line = new StringBuilder().append(word).append(" ");
            }
        }

        // Draw the last line
        float textWidth = textPaint.measureText(line.toString().trim());
        if (isCentered) {
            canvas.drawText(line.toString().trim(), (maxWidth - textWidth) / 2 + x, y, textPaint);
        } else if (alignRight) {
            canvas.drawText(line.toString().trim(), maxWidth - textWidth + x, y, textPaint);
        } else {
            canvas.drawText(line.toString().trim(), x, y, textPaint);
        }
    }



    private void printBillViaBluetooth(String printData) {
        try {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!bluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_SHORT).show();
                return;
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
                return;
            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals(PRINTER_NAME)) {
                        bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                        bluetoothSocket.connect();
                        outputStream = bluetoothSocket.getOutputStream();
                        break;
                    }
                }
            }

            if (outputStream != null) {
                outputStream.write(printData.getBytes());
                // Send form feed command to stop paper feeding
                outputStream.write("\f".getBytes());

                // Optionally, send ESC/POS command for paper cut (works for printers supporting ESC/POS)
                //byte[] cutCommand = {0x1D, 0x56, 0x42, 0x00}; // Full cut command
                //outputStream.write(cutCommand);

                // Close the output stream and socket
                outputStream.close();
                bluetoothSocket.close();
            } else {
                Toast.makeText(this, "Printer not found", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("BluetoothPrint", "Error: " + e.getMessage());
            Toast.makeText(this, "Error printing bill", Toast.LENGTH_LONG).show();
        }
    }
}



