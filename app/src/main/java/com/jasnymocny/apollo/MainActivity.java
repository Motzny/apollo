package com.jasnymocny.apollo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vikramezhil.droidspeech.DroidSpeech;
import com.vikramezhil.droidspeech.OnDSListener;
import com.vikramezhil.droidspeech.OnDSPermissionsListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnDSListener, OnDSPermissionsListener {

    private ArrayList<Player> players;
    private final String logTag = "MainActivity";
    private Queue<Player> activePlayers;
    private Player currentPlayer;
    private String previousLastLetter;
    private CountDownTimer timer;
    private DroidSpeech droidSpeech;

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

        // Initializing the droid speech and setting the listener
        droidSpeech = new DroidSpeech(this, getFragmentManager());
        droidSpeech.setShowRecognitionProgressView(false);
        droidSpeech.setOneStepResultVerify(false);
        droidSpeech.setRecognitionProgressMsgColor(Color.WHITE);
        droidSpeech.setOneStepVerifyConfirmTextColor(Color.WHITE);
        droidSpeech.setOneStepVerifyRetryTextColor(Color.WHITE);
        droidSpeech.setOnDroidSpeechListener(this);

        
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
                firstGameSetup();
                listenToPlayer();
                break;

            case R.id.bAddPlayer:
                addNewPlayer();
                break;
        }
    }

    private void firstGameSetup() {
        activePlayers = new LinkedList<>();
        activePlayers.addAll(players);
        currentPlayer = activePlayers.poll();
        TextView tvCurrPlayer = findViewById(R.id.tvCurrentPlayer);
        tvCurrPlayer.setTextColor(currentPlayer.getColor());
        tvCurrPlayer.setText(currentPlayer.getName());
        previousLastLetter = null;
    }

    private void listenToPlayer() {
        Log.v("MainActivity","listenToPlayer(): Name of current player: " + currentPlayer.getName());

        final TextView tvTimer = findViewById(R.id.tvTimer);

        TextView tvLastResult = findViewById(R.id.lastResult);

        if (previousLastLetter != null) {
            tvLastResult.setText(previousLastLetter);
        }

        droidSpeech.startDroidSpeechRecognition();

        try {
           timer = new CountDownTimer(9000, 1000){
                public void onTick(long millisUntilFinished) {
                    tvTimer.setText(String.valueOf(millisUntilFinished/1000));
                }
                public void onFinish() {
                    droidSpeech.closeDroidSpeechOperations();
                    Toast.makeText(MainActivity.this,  currentPlayer.getName() + " lost: time out", Toast.LENGTH_SHORT).show();
                    currentPlayer.loose();
                    try {
                        currentPlayer = activePlayers.remove();
                        listenToPlayer();
                    } catch (Exception e) {	//last player standing wins
                        Toast.makeText(MainActivity.this,  currentPlayer.getName() + " wins!", Toast.LENGTH_LONG).show();
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
    }

    @Override
    public void onDroidSpeechSupportedLanguages(String currentSpeechLanguage, List<String> supportedSpeechLanguages) {
        Log.i(logTag, "Current speech language = " + currentSpeechLanguage);
        Log.i(logTag, "Supported speech languages = " + supportedSpeechLanguages.toString());
    }

    @Override
    public void onDroidSpeechRmsChanged(float rmsChangedValue) {
        TextView tvRms = findViewById(R.id.tvRdms);
        tvRms.setText(String.valueOf(rmsChangedValue));

    }

    @Override
    public void onDroidSpeechLiveResult(String liveSpeechResult) {
        Log.v(logTag, "Live result: " + liveSpeechResult);
    }

    @Override
    public void onDroidSpeechFinalResult(String finalSpeechResult) {
        Log.v(logTag, "Final result: " + finalSpeechResult);

        timer.cancel();

        String currentFirstLetter = String.valueOf(finalSpeechResult.charAt(0));
        if (previousLastLetter != null) {                                                       //first player in a game can never loose
            if (currentPlayer.getThingsYouSaid().contains(finalSpeechResult) || currentFirstLetter != previousLastLetter) {
                Toast.makeText(MainActivity.this,  currentPlayer.getName() + "lost: wrong word", Toast.LENGTH_SHORT).show();
                currentPlayer.loose(); // TODO reduce indent
            }
        }
        currentPlayer.addThingYouSaid(finalSpeechResult);
        if (!currentPlayer.isLost()) {
            activePlayers.add(currentPlayer);
            currentPlayer.score();
        }
        try {
            currentPlayer = activePlayers.remove();  //last player standing wins
        } catch (Exception e) {
            Toast.makeText(MainActivity.this,  currentPlayer.getName() + " wins!", Toast.LENGTH_SHORT).show();
            return;
        }

        updateDisplayPlayerList();
        updateCurrentPlayerDisplay();
        previousLastLetter = String.valueOf(finalSpeechResult.charAt(finalSpeechResult.length() - 1));
        listenToPlayer();

    }

    private void updateCurrentPlayerDisplay() {
        TextView tvCurrPlayer = findViewById(R.id.tvCurrentPlayer);
        tvCurrPlayer.setText(currentPlayer.getName());
        tvCurrPlayer.invalidate();
    }

    private void updateDisplayPlayerList() {
        ListView lvPlayers = findViewById(R.id.lvPlayers);
        ArrayAdapter<Player> aaPlayers = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, players);
        lvPlayers.setAdapter(aaPlayers);
    }

    @Override
    public void onDroidSpeechClosedByUser() {
        Log.v(logTag, "Droid speech closed by user");

    }

    @Override
    public void onDroidSpeechError(String errorMsg) {
        Log.v(logTag, errorMsg);
        TextView tvRms = findViewById(R.id.tvRdms);
        tvRms.setText(errorMsg);
    }

    @Override
    public void onDroidSpeechAudioPermissionStatus(boolean audioPermissionGiven, String errorMsgIfAny) {

    }
}