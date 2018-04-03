package com.jasnymocny.apollo;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
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

    protected static final int RECOGNIZE_SPEECH = 1;
    private ArrayList<Player> players;
    private final String logTag = "MainActivity";
    private Queue<Player> activePlayers;
    private Player currentPlayer;
    private Character previousLastLetter;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.bAddPlayer).setOnClickListener(this);

        Intent intent = getIntent();

        players = new ArrayList<>();


        if (savedInstanceState == null) {
            players = intent.getParcelableArrayListExtra("players");
        } else  {
            players = savedInstanceState.getParcelableArrayList("players");
        }

        String newPlayerName = intent.getStringExtra("playerName");
        int newPlayerColor = intent.getIntExtra("color",0);
        if (savedInstanceState == null && newPlayerName != null) {
            players.add(new Player(newPlayerName, newPlayerColor));
        }

        if (players == null){
            Log.v(logTag, "Assuming this is a cold run, setting players to empty ArrList");
            players = new ArrayList<>();
        }

        updateDisplayPlayerList();

        
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

                activePlayers = new LinkedList<>();
                activePlayers.addAll(players);
                currentPlayer = activePlayers.poll();
                previousLastLetter = null;

                listenToPlayer();
                break;

            case R.id.bAddPlayer:
                addNewPlayer();
                break;
        }
    }

    private void listenToPlayer() {
        Log.v("MainActivity","listenToPlayer(): Name of current player: " + currentPlayer.getName());
        final TextView tvPlayer = findViewById(R.id.tvCurrentPlayer);
        tvPlayer.setText(currentPlayer.getName());
        tvPlayer.invalidate();

        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        startActivityForResult(i, RECOGNIZE_SPEECH);
        try {
           timer = new CountDownTimer(9000, 1000){
                public void onTick(long millisUntilFinished) {
                    tvPlayer.setText(currentPlayer.getName() + " " + millisUntilFinished/1000);
                }
                public void onFinish() {
                    Toast.makeText(MainActivity.this,  currentPlayer.getName() + " lost: time out", Toast.LENGTH_SHORT).show();
                    finishActivity(RECOGNIZE_SPEECH);
                    currentPlayer.loose();
                    try {
                        currentPlayer = activePlayers.remove();
                        listenToPlayer();
                    } catch (Exception e) {	//last player standing wins
                        Toast.makeText(MainActivity.this,  currentPlayer.getName() + " wins!", Toast.LENGTH_LONG).show();
                        return;
                    }
               }
            }.start();



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
        timer.cancel();
        if (requestCode == RECOGNIZE_SPEECH && resultCode == RESULT_OK) {
            String currentResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0).toLowerCase(); //only get the most likely result
            char currentFirstLetter = currentResult.charAt(0);
            if (previousLastLetter != null) {                                                       //first player in a game can never loose
                if (currentPlayer.getThingsYouSaid().contains(currentResult) || currentFirstLetter != previousLastLetter) {
                    Toast.makeText(MainActivity.this,  currentPlayer.getName() + "lost: wrong word", Toast.LENGTH_SHORT).show();
                    currentPlayer.loose(); // TODO reduce indent
                }
            }
            currentPlayer.addThingYouSaid(currentResult);
            if (!currentPlayer.isLost()) {
                activePlayers.add(currentPlayer);
                currentPlayer.score();
            }
            try {
                currentPlayer = activePlayers.remove();
                TextView tvPlayer = findViewById(R.id.tvCurrentPlayer);
                tvPlayer.setText(currentPlayer.getName());
                tvPlayer.setBackgroundColor(currentPlayer.getColor());
                tvPlayer.invalidate();
            } catch (Exception e) {	//last player standing wins
                Toast.makeText(MainActivity.this,  currentPlayer.getName() + " wins!", Toast.LENGTH_SHORT).show();
                return;
            }

            updateDisplayPlayerList();
            previousLastLetter = currentResult.charAt(currentResult.length() - 1);
            listenToPlayer();
        }
    }

    private void updateDisplayPlayerList() {
        ListView lvPlayers = findViewById(R.id.lvPlayers);
        ArrayAdapter<Player> aaPlayers = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, players);
        lvPlayers.setAdapter(aaPlayers);
    }

}