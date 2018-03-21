package com.jasnymocny.apollo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;


public class AddPlayerActivity extends Activity implements View.OnClickListener {

    private ArrayList<Player> players;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addplayer);
        findViewById(R.id.bOkPlayerName).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

        EditText et =  (EditText)findViewById(R.id.editText);

        String proposedName = et.getText().toString();
        Intent i = getIntent();
        players = i.getParcelableArrayListExtra("players");
        Intent in = new Intent (AddPlayerActivity.this, MainActivity.class);

        Boolean isValidName = true;
        for (String n : getPlayerNames()){
            if (n.equalsIgnoreCase(proposedName)) {
                Toast.makeText(AddPlayerActivity.this, "The name is taken", Toast.LENGTH_SHORT).show();
                isValidName = false;
            }
        }

        if (isValidName) {
            in.putExtra("playerName", proposedName);
            in.putParcelableArrayListExtra("players", players);
            startActivity(in);
        }
    }

    private ArrayList<String> getPlayerNames() {

        ArrayList<String> playerNames = new ArrayList<String>();

        Intent i = getIntent();
        players = i.getParcelableArrayListExtra("players");
        for (Player p : players) {
            playerNames.add(p.getName());
        }
        return playerNames;
    }
}
