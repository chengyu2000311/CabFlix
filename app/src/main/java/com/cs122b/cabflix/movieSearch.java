package com.cs122b.cabflix;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class movieSearch extends AppCompatActivity {

    public static final String baseUrl = "http://192.168.31.6:12345/api/g/";
    public static final String browseUrl = "movies/browse";
    public static final String searchUrl = "movies/search";
    public static final String reportUrl = "report";

    private static final String TAG = "movieSearch";
    private ArrayList<String> movieTitles = new ArrayList<>();
    private ArrayList<String> movieUrls = new ArrayList<>();
    private ArrayList<String> movieIDs = new ArrayList<>();
    
    private RecyclerView movies;


    private void getReport(HashMap<String, String> tMap, RequestQueue queue, Context context) {
        movieTitles.clear();
        movieUrls.clear();
        movieIDs.clear();
        StringRequest jsonRequestForReport = new StringRequest(Request.Method.GET, baseUrl + reportUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        Toast.makeText(context, jsonObject.get("message").toString(), Toast.LENGTH_LONG).show();
                        if (jsonObject.get("resultCode").equals(210)) {
                            JSONArray movies = (JSONArray) jsonObject.get("movies");
                            System.out.println(movies.toString());
                            for (int i = 0 ; i < movies.length(); i++) {
                                JSONObject movie = movies.getJSONObject(i);
                                if (movieTitles.size() == 10) break;
                                movieTitles.add((String) movie.get("title"));
                                movieUrls.add("https://image.tmdb.org/t/p/w500"+(String) movie.get("poster_path"));
                                movieIDs.add((String) movie.get("movie_id"));
                            }
                            initImageBitMaps();
                        }

                    }catch (JSONException err){
                        err.printStackTrace();
                    }

                }
            }
        }, error -> {
            error.printStackTrace();
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return tMap;
            }
        };

        for (int i=0; i<10; ++i) { // 10 is the number of thread in my backend
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    queue.add(jsonRequestForReport);
                }
            }, 300L);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_search);
        setTitle("Movies");

        Log.d(TAG, "started for movies page");
        getResponse();
    }

    private void getResponse() {
        EditText director = findViewById(R.id.searchDirector);
        EditText genre = findViewById(R.id.searchGenre);
        EditText year = findViewById(R.id.searchYear);
        EditText title = findViewById(R.id.searchTitle);
        EditText keyword = findViewById(R.id.searchKeyword);
        Button search = findViewById(R.id.searchButton);

        RequestQueue queue = Volley.newRequestQueue(this);
        Context context = this;

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String directorText = director.getText().toString();
                String genreText = genre.getText().toString();
                String yearText = year.getText().toString();
                String titleText = title.getText().toString();
                String keywordText = keyword.getText().toString();
                System.out.println(String.format("%s %s %s %s %s", directorText, genreText, yearText, titleText, keywordText));
                System.out.println("-----------------------------------");
                if (!keywordText.isEmpty() && directorText.isEmpty() && genreText.isEmpty() && yearText.isEmpty() && titleText.isEmpty()) {
                    System.out.println("keyword");
                    gateWayGetHeader request = new gateWayGetHeader(Request.Method.GET,baseUrl + browseUrl + "/" + keywordText, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try{
                                HashMap<String, String> tMap=new HashMap<>();
                                tMap.put("transaction_id", (String) response.get("transaction_id"));
                                getReport(tMap, queue, context);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, error -> {
                        error.printStackTrace();
                    });
                    queue.add(request);
                } else {
                    System.out.println("other");
                    HashMap<String, String> map = new HashMap<>();
                    if (!directorText.isEmpty()) map.put("director", directorText);
                    if (!genreText.isEmpty()) map.put("genre", genreText);
                    if (!yearText.isEmpty()) map.put("year", yearText);
                    if (!titleText.isEmpty()) map.put("title", titleText);
                    String query = "";
                    if (map.size() == 1) {
                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            query += String.format("?%s=%s", entry.getKey(), entry.getValue());
                        }
                    } else if (map.size() > 1) {
                        int index = 0;
                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            if (index == 0) {
                                query += String.format("?%s=%s", entry.getKey(), entry.getValue());
                            } else {
                                query += String.format("&%s=%s", entry.getKey(), entry.getValue());
                            }
                        }
                    }
                    gateWayGetHeader request = new gateWayGetHeader(Request.Method.GET,baseUrl + searchUrl + "/" + query, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try{
                                HashMap<String, String> tMap=new HashMap<>();
                                tMap.put("transaction_id", (String) response.get("transaction_id"));
                                getReport(tMap, queue, context);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, error -> {
                        error.printStackTrace();
                    });
                    queue.add(request);
                }
            }
        });

    }


    private void initImageBitMaps() {
        Log.d(TAG, "initializing image bit map");


        initRecyclerView();
    }

    private void initRecyclerView() {
        Log.d(TAG, "initializing Recycler View");

        RecyclerView recyclerView = findViewById(R.id.result);
        movieAdapter adapter = new movieAdapter(movieUrls, movieTitles, movieIDs, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
