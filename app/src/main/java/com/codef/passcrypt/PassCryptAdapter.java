package com.codef.passcrypt;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.InputType;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class PassCryptAdapter extends
        RecyclerView.Adapter<PassCryptAdapter.ViewHolder> {

    private final AppCompatActivity appCompatActivity;
    private final List<PassCryptEntry> passCryptEntries;
    private final HashMap<String, Integer> resourceMap;

    private final static String ENCRYPTION_TYPE = "AES";

    public PassCryptAdapter(AppCompatActivity appCompatActivity, HashMap<String, Integer> resourceMap, List<PassCryptEntry> passCryptEntries) {
        this.passCryptEntries = passCryptEntries;
        this.appCompatActivity = appCompatActivity;
        this.resourceMap = resourceMap;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_passcrypt, parent, false);

        // Return a new holder instance
        return new ViewHolder(contactView);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data model based on position
        PassCryptEntry passCryptEntry = this.passCryptEntries.get(position);

        // Set item views based on your views and data model
        TextView textView = holder.nameTextView;
        textView.setText(passCryptEntry.getName().replace('_', ' '));
        textView.setAutoLinkMask(Linkify.WEB_URLS);
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(passCryptEntry.getUrl()));
            Context context = v.getContext();
            context.startActivity(intent);
        });

        Button button = holder.messageButton;
        button.setOnClickListener(v -> {
            Context context = v.getContext();
            askUserForCredentials(context, passCryptEntry.getName());
        });
        button.setText(R.string.decrypt);
        button.setEnabled(true);

    }


    public void askUserForCredentials(Context context, String credentialName) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Password");
        builder.setMessage("Please enter your password:");

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            try {
                String userInput = input.getText().toString();
                if (userInput.equals(readCryptKey(R.raw.passcrypt))) {
                    String cryptKey = readCryptKey(R.raw.secret);
                    int credResourceId = resourceMap.get(credentialName.toLowerCase());
                    showDecryptedCredentials(context, decryptFileToString(credResourceId, cryptKey));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void showDecryptedCredentials(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        TextView messageTextView = new TextView(context);
        messageTextView.setText(message);
        messageTextView.setTypeface(Typeface.create("monospace", Typeface.NORMAL));
        messageTextView.setPadding(80, 20, 20, 20);
        messageTextView.setTextIsSelectable(true);

        builder.setTitle("Credentials")
                .setView(messageTextView)
                .setNegativeButton("Close", null) // You can add a negative button or remove this line
                .create()
                .show();
    }

    public String readCryptKey(int resourceId) throws IOException {

        try (InputStream inputStream = appCompatActivity.getResources().openRawResource(resourceId);
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {

            String readLine;
            StringBuilder returnString = new StringBuilder();

            while ((readLine = br.readLine()) != null) {
                returnString.append(readLine);
            }

            return returnString.toString();
        }

    }


    public String decryptFileToString(int resourceId, String key) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IOException {

        SecretKey secretKey = new SecretKeySpec(key.getBytes(), ENCRYPTION_TYPE);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_TYPE);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        try (InputStream inputStream = appCompatActivity.getResources().openRawResource(resourceId);
             CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = cipherInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            cipherInputStream.close();

            return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        }
    }


    @Override
    public int getItemCount() {
        return this.passCryptEntries.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public Button messageButton;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            nameTextView = itemView.findViewById(R.id.passcrypt_name);
            messageButton = itemView.findViewById(R.id.passcrypt_button);
        }
    }
}