package com.jasnymocny.apollo;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    protected static final int REQUEST_OK = 1;
    private ArrayList<Player> players;
    private final String logTag = "MainActivity";
    private Queue<Player> activePlayers;
    private Player currentPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.bAddPlayer).setOnClickListener(this);

        Intent intent = getIntent();

        players = new ArrayList<Player>();


        if (savedInstanceState == null) {
            players = intent.getParcelableArrayListExtra("players");
        } else  {
            players = savedInstanceState.getParcelableArrayList("players");
        }
        String newPlayerName = intent.getStringExtra("playerName");
        if (savedInstanceState == null && newPlayerName != null) {
            players.add(new Player(newPlayerName));
        }

        if (players == null){
            Log.v(logTag, "Assuming this is a cold run, setting players to empty ArrList");
            players = new ArrayList<Player>();
        }

        ListView lvPlayers = (ListView)findViewById(R.id.lvPlayers);
        ArrayAdapter<Player> aaPlayers = new ArrayAdapter<Player>(this, android.R.layout.simple_list_item_1, players);
        lvPlayers.setAdapter(aaPlayers);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("players", players);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.button1: //start game
                activePlayers = new LinkedList<Player>();
                activePlayers.addAll(players);
                currentPlayer = activePlayers.poll();
                listenToPlayer();
                break;

            case R.id.bAddPlayer:
                addNewPlayer();
                break;
        }
    }

    private void listenToPlayer() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        try {
            startActivityForResult(i, REQUEST_OK);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
        }
    }

    private void addNewPlayer() {
        Intent j = new Intent(MainActivity.this, AddPlayerActivity.class);
        j.putParcelableArrayListExtra("players",players);
        startActivity(j);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OK && resultCode == RESULT_OK) {
            String currentResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0); //only get the most likely result
                if (currentPlayer.getThingsYouSaid().contains(currentResult)) {
                    currentPlayer.loose();
                }
            currentPlayer.addThingYouSaid(currentResult);
            if (!currentPlayer.isLost()) {
                activePlayers.add(currentPlayer);
                currentPlayer.score();
            }
            try {
                currentPlayer = activePlayers.remove();
            } catch (Exception e) {	//last player standing wins
                return;
            }
            TextView tv = (TextView)findViewById(R.id.text1);
            tv.setText(currentPlayer.toString());
            ListView lvPlayers = (ListView)findViewById(R.id.lvPlayers);
            ArrayAdapter<Player> aaPlayers = new ArrayAdapter<Player>(this, android.R.layout.simple_list_item_1, players);
            lvPlayers.setAdapter(aaPlayers);
            listenToPlayer();
        }
    }
}