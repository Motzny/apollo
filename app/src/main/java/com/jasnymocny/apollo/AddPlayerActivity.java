package com.jasnymocny.apollo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;


public class AddPlayerActivity extends Activity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private ArrayList<Player> players;
    private int color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addplayer);
        findViewById(R.id.bOkPlayerName).setOnClickListener(this);

        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, r.getDisplayMetrics());//TODO: to res
        LinearGradient test = new LinearGradient(0.0f, 0.8f, px, 1.0f,
                new int[]{0x0000FF0, 0xFF0000FF, 0xFF00FF00, 0xFF00FFFF,
                        0xFFFF0000, 0xFFFF00FF, 0xFFFFFF00, 0xFFFFFFFF},
                null, Shader.TileMode.CLAMP);
        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setShader(test);

        SeekBar seekBar = findViewById(R.id.seekbar_font);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setMax(256 * 7 - 300);
        seekBar.setProgress(300);

        seekBar.setBackground(shape);
    }


    @Override
    public void onClick(View view) {

        EditText et = findViewById(R.id.editText);

        String proposedName = et.getText().toString();
        Intent i = getIntent();
        players = i.getParcelableArrayListExtra("players");
        Intent in = new Intent(AddPlayerActivity.this, MainActivity.class);

        Boolean isValidName = true;
        for (String n : getPlayerNames()) {
            if (n.equalsIgnoreCase(proposedName)) {
                Toast.makeText(AddPlayerActivity.this, "The name is taken", Toast.LENGTH_SHORT).show();
                isValidName = false;
            }
        }

        if (isValidName) {
            in.putExtra("playerName", proposedName);
            in.putParcelableArrayListExtra("players", players);
            in.putExtra("color", color);
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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            int r = 0;
            int g = 0;
            int b = 0;

            if (progress < 256) {
                b = progress;
            } else if (progress < 256 * 2) {
                g = progress % 256;
                b = 256 - progress % 256;
            } else if (progress < 256 * 3) {
                g = 255;
                b = progress % 256;
            } else if (progress < 256 * 4) {
                r = progress % 256;
                g = 256 - progress % 256;
                b = 256 - progress % 256;
            } else if (progress < 256 * 5) {
                r = 255;
                g = 0;
                b = progress % 256;
            } else if (progress < 256 * 6) {
                r = 255;
                g = progress % 256;
                b = 256 - progress % 256;
            } else if (progress < 256 * 7) {
                r = 255;
                g = 255;
                b = progress % 256;
            }
            Log.v("AddPlayerActivity","r:" + r + "g:" + g + "b:" + b);


            EditText et = (EditText) findViewById(R.id.editText);
            color = Color.argb(100,r,g,b);
            et.setBackgroundColor(Color.argb(75,r,g,b));

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}