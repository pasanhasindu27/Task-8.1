package com.example.tutorapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tutorapp.ChatActivity;
import com.example.tutorapp.R;

public class MainActivity extends AppCompatActivity {

    private EditText usernameInput;
    private Button goButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameInput = findViewById(R.id.usernameInput);
        goButton = findViewById(R.id.goButton);

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString().trim();
                if (username.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
                } else {
                    // Start chat activity with username
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                    intent.putExtra("USERNAME", username);
                    startActivity(intent);
                }
            }
        });
    }
}