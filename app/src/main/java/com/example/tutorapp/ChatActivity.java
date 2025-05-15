package com.example.tutorapp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.tutorapp.Message;
import com.example.tutorapp.MessageAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private String username;
    private RequestQueue requestQueue;
    private static final String BASE_URL = "https://9b4c-2407-c00-d002-5c0d-d840-37be-f21-6a5.ngrok-free.app"; // For emulator, points to localhost on host machine

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get username from intent
        username = getIntent().getStringExtra("USERNAME");
        if (username == null || username.isEmpty()) {
            username = "User";
        }

        // Initialize views
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // Initialize message list and adapter
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        
        // Set up RecyclerView
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(messageAdapter);

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Add welcome message
        Message welcomeMessage = new Message("Hi " + username + "! I'm Llama 2 ChatBot. Ask me to create a quiz on any topic!", false);
        messageList.add(welcomeMessage);
        messageAdapter.notifyItemInserted(messageList.size() - 1);

        // Set up send button click listener
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageInput.getText().toString().trim();
                if (!messageText.isEmpty()) {
                    sendMessage(messageText);
                }
            }
        });
    }

    private void sendMessage(String messageText) {
        // Add user message to the list
        Message userMessage = new Message(messageText, true);
        messageList.add(userMessage);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        
        // Clear input field
        messageInput.setText("");
        
        // Scroll to the bottom
        chatRecyclerView.scrollToPosition(messageList.size() - 1);

        // Check if message is asking for a quiz
        if (messageText.toLowerCase().contains("quiz") || 
            messageText.toLowerCase().contains("test") || 
            messageText.toLowerCase().contains("question")) {
            
            // Extract topic from message
            String topic = extractTopic(messageText);
            if (topic != null) {
                fetchQuiz(topic);
            } else {
                // If no topic found, ask for one
                Message botMessage = new Message("What topic would you like a quiz on?", false);
                messageList.add(botMessage);
                messageAdapter.notifyItemInserted(messageList.size() - 1);
                chatRecyclerView.scrollToPosition(messageList.size() - 1);
            }
        } else {
            // Assume message is a topic for quiz
            fetchQuiz(messageText);
        }
    }

    private String extractTopic(String message) {
        // Simple extraction - get text after "quiz on", "test on", etc.
        String[] patterns = {"quiz on", "test on", "questions on", "quiz about", "test about", "questions about"};
        
        for (String pattern : patterns) {
            int index = message.toLowerCase().indexOf(pattern);
            if (index != -1) {
                return message.substring(index + pattern.length()).trim();
            }
        }
        
        // If no pattern found, return the whole message as topic
        return message;
    }

    private void fetchQuiz(final String topic) {
        // Add loading message
        final Message loadingMessage = new Message("Generating quiz on " + topic + "...", false);
        messageList.add(loadingMessage);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        chatRecyclerView.scrollToPosition(messageList.size() - 1);

        // Create URL with query parameter
        String url = BASE_URL + "/getQuiz?topic=" + topic;

        // Create JSON request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Remove loading message
                            messageList.remove(loadingMessage);
                            messageAdapter.notifyDataSetChanged();

                            // Process quiz data
                            JSONArray quizArray = response.getJSONArray("quiz");
                            StringBuilder quizMessage = new StringBuilder("Here's your quiz on " + topic + ":\n\n");
                            
                            for (int i = 0; i < quizArray.length(); i++) {
                                JSONObject questionObj = quizArray.getJSONObject(i);
                                String question = questionObj.getString("question");
                                JSONArray options = questionObj.getJSONArray("options");
                                String correctAnswer = questionObj.getString("correct_answer");
                                
                                quizMessage.append("**QUESTION ").append(i + 1).append(":** ").append(question).append("\n");
                                quizMessage.append("**OPTION A:** ").append(options.getString(0)).append("\n");
                                quizMessage.append("**OPTION B:** ").append(options.getString(1)).append("\n");
                                quizMessage.append("**OPTION C:** ").append(options.getString(2)).append("\n");
                                quizMessage.append("**OPTION D:** ").append(options.getString(3)).append("\n");
                                quizMessage.append("**ANS:** ").append(correctAnswer).append("\n\n");
                            }
                            
                            // Add quiz message
                            Message quizResponseMessage = new Message(quizMessage.toString(), false);
                            messageList.add(quizResponseMessage);
                            messageAdapter.notifyItemInserted(messageList.size() - 1);
                            chatRecyclerView.scrollToPosition(messageList.size() - 1);
                            
                        } catch (JSONException e) {
                            handleError("Error parsing quiz data: " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Remove loading message
                        messageList.remove(loadingMessage);
                        messageAdapter.notifyDataSetChanged();
                        
                        handleError("Error fetching quiz: " + error.getMessage());
                    }
                });

        // Add request to queue
        requestQueue.add(jsonObjectRequest);
    }

    private void handleError(String errorMessage) {
        Message errorMsg = new Message("Sorry, I encountered an error: " + errorMessage, false);
        messageList.add(errorMsg);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        chatRecyclerView.scrollToPosition(messageList.size() - 1);
    }
}