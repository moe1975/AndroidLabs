package com.example.moe.androidlabs;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ChatWindow extends AppCompatActivity {

    final ArrayList<String> chatArray = new ArrayList<>();

    private ChatDatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private String[] allColumns = { ChatDatabaseHelper.COLUMN_ID,
            ChatDatabaseHelper.COLUMN_MESSAGE };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);

        dbHelper = new ChatDatabaseHelper(this);

        database = dbHelper.getWritableDatabase();

        readMessages();

        Resources resources = getResources();
        final ListView listViewChat = (ListView) findViewById(R.id.listViewChat);
        final ChatAdapter chatAdapter = new ChatAdapter(this);
        listViewChat.setAdapter(chatAdapter);
        final EditText editTextChat = (EditText) findViewById(R.id.textChat);
        Button buttonSend = (Button) findViewById(R.id.sendChat);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chatString = editTextChat.getText().toString();
                chatArray.add(chatString);
                writeMessages(chatString);

                chatAdapter.notifyDataSetChanged();
                editTextChat.setText("");
            }
        });
    }

    private void readMessages() {
        // read database and save messages into the array list
        Cursor cursor = database.query(ChatDatabaseHelper.TABLE_MESSAGES,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String message = cursor.getString(cursor.getColumnIndex( ChatDatabaseHelper.COLUMN_MESSAGE));
            Log.i("Chat Window", "SQL MESSAGE:" + cursor.getString( cursor.getColumnIndex(ChatDatabaseHelper.COLUMN_MESSAGE)));
            chatArray.add(message);
            cursor.moveToNext();
        }

        Log.i("Chat Window", "Cursor’s column count =" + cursor.getColumnCount());

        for(int i = 0; i < cursor.getColumnCount(); i++) {
            Log.i("Chat Window", "Column Name: " + cursor.getColumnName(i));
        }

        // close the cursor
        cursor.close();
    }

    private void writeMessages(String message) {
        ContentValues values = new ContentValues();
        values.put(ChatDatabaseHelper.COLUMN_MESSAGE, message);
        database.insert(ChatDatabaseHelper.TABLE_MESSAGES, null,
                values);
    }

    private class ChatAdapter extends ArrayAdapter<String> {

        public ChatAdapter(Context context) {
            super(context, 0);
        }

        public int getCount() {
            return chatArray.size();
        }

        public String getItem(int position) {
            return chatArray.get(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = ChatWindow.this.getLayoutInflater();
            View result = null;
            if (position%2 == 0) {
                result = inflater.inflate(R.layout.chat_row_incoming, null);
            } else {
                result = inflater.inflate(R.layout.chat_row_outgoing, null);
            }

            TextView message = (TextView) result.findViewById((R.id.textChat));
            message.setText(getItem(position));
            return result;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        dbHelper.close();
    }
}
