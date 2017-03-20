package com.example.moe.androidlabs;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ChatWindow extends AppCompatActivity {

    final ArrayList<ChatData> chatArray = new ArrayList<>();

    private ChatDatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private String[] allColumns = { ChatDatabaseHelper.COLUMN_ID,
            ChatDatabaseHelper.COLUMN_MESSAGE };
    private boolean isFrameLoaded;
    private FrameLayout chatFrame;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);

        dbHelper = new ChatDatabaseHelper(this);

        database = dbHelper.getWritableDatabase();

        readMessages();

        Resources resources = getResources();
        final ListView listViewChat = (ListView) findViewById(R.id.listViewChat);
        chatAdapter = new ChatAdapter(this);
        listViewChat.setAdapter(chatAdapter);
        final EditText editTextChat = (EditText) findViewById(R.id.textChat);
        Button buttonSend = (Button) findViewById(R.id.sendChat);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chatString = editTextChat.getText().toString();
                writeMessages(chatString);

                editTextChat.setText("");
            }
        });


        listViewChat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {

                Object o = listViewChat.getItemAtPosition(position);
                ChatData str=(ChatData)o;
                Bundle data = new Bundle();

                data.putString("messageText", str.getMessage());
                data.putString("messageID", Integer.toString(position));

                if(!isFrameLoaded) {        // phone
                    Intent intent = new Intent(ChatWindow.this, MessageDetails.class);
                    intent.putExtras(data);
                    startActivityForResult(intent, 5);
                } else {                    // tablet
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    MessageFragment f = new MessageFragment(1);
                    f.setArguments(data);

                    ft.replace(R.id.chatFrame, f);
                    ft.commit();
                }
            }
        });

        chatFrame = (FrameLayout)findViewById(R.id.chatFrame);

        isFrameLoaded = (chatFrame != null);


    }

    private void readMessages() {
        // read database and save messages into the array list
        Cursor cursor = database.query(ChatDatabaseHelper.TABLE_MESSAGES,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String message = cursor.getString(cursor.getColumnIndex( ChatDatabaseHelper.COLUMN_MESSAGE));
            long id = cursor.getLong(cursor.getColumnIndex(ChatDatabaseHelper.COLUMN_ID));

            Log.i("Chat Window", "ID: " + cursor.getString( cursor.getColumnIndex(ChatDatabaseHelper.COLUMN_ID)) + " SQL MESSAGE:" + cursor.getString( cursor.getColumnIndex(ChatDatabaseHelper.COLUMN_MESSAGE)));

            ChatData data = new ChatData(message, id);

            chatArray.add(data);
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
        long id = database.insert(ChatDatabaseHelper.TABLE_MESSAGES, null,
                values);

        ChatData data = new ChatData(message, id);
        chatAdapter.notifyDataSetChanged();
        chatArray.add(data);

    }

    public void deleteMessage(int id) {
        database.delete(ChatDatabaseHelper.TABLE_MESSAGES, "_id=?",
                new String[]{Long.toString(chatArray.get(id).get_id())});

        chatArray.remove(id);
        chatAdapter.notifyDataSetChanged();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        MessageFragment f = (MessageFragment)getSupportFragmentManager().findFragmentById(R.id.chatFrame);

        ft.remove(f);
        ft.commit();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 5) {
            Bundle extras = data.getExtras();
            int id = Integer.parseInt(extras.getString("id"));

            database.delete(ChatDatabaseHelper.TABLE_MESSAGES, "_id=?",
                    new String[]{Long.toString(chatArray.get(id).get_id())});

            chatArray.remove(id);
            chatAdapter.notifyDataSetChanged();
        }
    }

    private class ChatAdapter extends ArrayAdapter<ChatData> {

        public ChatAdapter(Context context) {
            super(context, 0);
        }

        public int getCount() {
            return chatArray.size();
        }

        public long getItemId(int position) { return chatArray.get(position).get_id();}
        public ChatData getItem(int position) {
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
            message.setText(getItem(position).getMessage());
            return result;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        dbHelper.close();
    }
}
