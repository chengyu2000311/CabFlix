package com.cs122b.cabflix;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class movieDetail extends AppCompatActivity {

    private static final String TAG = "movieDetail";

    private static final String baseUrl = "http://192.168.31.6:12345/api/g/";
    private static final String getMovie = "movies/get";
    private static final String reportUrl = "report";
    private Bundle extras;
    private TextView title;
    private TextView overview;
    private TextView year;
    private TextView director;
    private TextView rating;
    private TextView votes;
    private TextView genres;
    private TextView people;
    private ImageView photo;

    public void getReport(HashMap<String, String> tMap, RequestQueue queue, Context context) {

        StringRequest jsonRequestForReport = new StringRequest(Request.Method.GET, baseUrl + reportUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONObject movie = (JSONObject) jsonObject.get("movie");
                        title.setText(movie.get("title").toString());
                        overview.setText(movie.get("overview").toString());
                        year.setText(movie.get("year").toString());
                        director.setText(movie.get("director").toString());
                        rating.setText(movie.get("rating").toString());
                        votes.setText(movie.get("num_votes").toString());

                        String theGenre = "";
                        JSONArray genres2 = (JSONArray) movie.get("genres");
                        for (int i = 0 ; i < genres2.length(); i++) {
                            JSONObject g = genres2.getJSONObject(i);
                            theGenre += g.get("name").toString() + " ";
                        }
                        genres.setText(theGenre);

                        JSONArray people2 = (JSONArray) movie.get("people");
                        String thePeople = "";
                        for (int i = 0 ; i < people2.length()-1; i++) {
                            JSONObject g = people2.getJSONObject(i);
                            thePeople += g.get("name").toString() + ", ";
                        }
                        JSONObject g = people2.getJSONObject(people2.length()-1);
                        thePeople += g.get("name").toString() + ".";
                        people.setText(thePeople);

                        Glide.with(context).fromUri().asBitmap().load(Uri.parse("https://image.tmdb.org/t/p/w500"+(String) movie.get("poster_path"))).into(photo);
                        Log.d(TAG, "assigned every details");
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_detail);

        extras = getIntent().getExtras();
        title = findViewById(R.id.movie_title);
        overview = findViewById(R.id.theOverview);
        year = findViewById(R.id.thYear);
        director = findViewById(R.id.theDirector);
        votes = findViewById(R.id.theVotes);
        rating = findViewById(R.id.theRating);
        genres = findViewById(R.id.theGenres);
        people = findViewById(R.id.thePeople);
        photo = findViewById(R.id.photo);
        Context context = this;

        overview.setMovementMethod(new ScrollingMovementMethod());
        people.setMovementMethod(new ScrollingMovementMethod());
        genres.setMovementMethod(new ScrollingMovementMethod());


        if (extras != null) {
            String movie_id = (String) extras.get("movie_id");
            RequestQueue queue = Volley.newRequestQueue(this);
            gateWayGetHeader jsonObjectRequest = new gateWayGetHeader
                    (Request.Method.GET, baseUrl + getMovie + "/" + movie_id, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                HashMap<String, String> tMap=new HashMap<>();
                                tMap.put("transaction_id", (String) response.get("transaction_id"));
                                getReport(tMap, queue, context);
                            }catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, error -> {
                        error.printStackTrace();
                    });
            queue.add(jsonObjectRequest);
        }
    }
}
