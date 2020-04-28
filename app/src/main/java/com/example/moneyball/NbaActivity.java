package com.example.moneyball;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class NbaActivity extends AppCompatActivity implements GameAdapter.ItemClickListener{
    GameAdapter gameAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nba);
        // how the list wll look like
        RecyclerView wagerList = findViewById(R.id.gameList);
        int numOfColumns = 1;
        RecyclerView.LayoutManager recyclerManager = new GridLayoutManager(getApplicationContext(), numOfColumns);
        wagerList.setLayoutManager(recyclerManager);
        final ArrayList<Game> Games = new ArrayList<>();

        // API Calls need to run asynchronously
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    OkHttpClient client = new OkHttpClient();
                    Response response = null;
                    Response response2 = null;

                    for (int i = 1; i <=10; i++) {
                        // request for games endpoint
                        Request request = new Request.Builder()
                                .url("https://free-nba.p.rapidapi.com/games/"+i)
                                .get()
                                .addHeader("x-rapidapi-host", "free-nba.p.rapidapi.com")
                                .addHeader("x-rapidapi-key", "5b1e3c5d43mshbff7c5d6ee6bd0bp1d7debjsnf76b534f6cc5")
                                .build();
                        // request for stats endpoint
                        Request request2 = new Request.Builder()
                                .url("https://free-nba.p.rapidapi.com/stats?game_ids[]="+i+"&page=0&per_page=25")
                                .get()
                                .addHeader("x-rapidapi-host", "free-nba.p.rapidapi.com")
                                .addHeader("x-rapidapi-key", "5b1e3c5d43mshbff7c5d6ee6bd0bp1d7debjsnf76b534f6cc5")
                                .build();

                        try {
                            response = client.newCall(request).execute();
                            response2 = client.newCall(request2).execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String res = "", res2="";
                        try {
                            res = response.body().string();
                            Log.d("NBA", res);
                            res2 = response2.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            // reading the responses from the 2 requests as JSON
                            JSONObject json = new JSONObject(res);
                            JSONObject json2 = new JSONObject(res2);
                            String home = json.getJSONObject("home_team").getString("full_name");
                            String visitor = json.getJSONObject("visitor_team").getString("full_name");
                            // Grabbing the final score for the game
                            String score = "Final Score: "+ json.getString("home_team_score") + " - " + json.getString("visitor_team_score");
                            String stats = "\n";
                            // iterating over all the players to grab individual points scored
                            for (int j = 0; j<20;j++) {
                                JSONObject player = json2.getJSONArray("data").getJSONObject(j);
                                stats += player.getJSONObject("player").getString("last_name") + ": " + player.getString("pts")+" points \n";
                            }
                            Game g =new Game(home, visitor, score, stats);
                            // adding to our list
                            Games.add(g);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        try {
            thread.join(); // waiting for thread to finish
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        gameAdapter = new GameAdapter(Games);
        wagerList.setAdapter(gameAdapter);
        ((GameAdapter) gameAdapter).setClickListener(this);



    }

    @Override
    public void onItemClick(View view, int position) {
        // displaying desired results as a toast
        String msg = ((GameAdapter) gameAdapter).getItem(position).getScore() + "\n" + ((GameAdapter) gameAdapter).getItem(position).getStats();
        Toast.makeText(getApplicationContext(),  msg, Toast.LENGTH_LONG).show();
    }
}
