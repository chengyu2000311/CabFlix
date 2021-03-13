package com.cs122b.cabflix;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class registerActivity extends AppCompatActivity {
    public static final String baseUrl = "http://192.168.31.6:12345/api/g/";
    public static final String registerUrl = "idm/register";
    public static final String reportUrl = "report";

    public void getReport(HashMap<String, String> tMap, RequestQueue queue, Context context) {

        StringRequest jsonRequestForReport = new StringRequest(Request.Method.GET, baseUrl + reportUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        Toast.makeText(context, jsonObject.get("message").toString(), Toast.LENGTH_LONG).show();
                        if (jsonObject.get("resultCode").equals(110)) {
                            Intent intent=new Intent(registerActivity.this, loginActivity.class);
                            startActivity(intent);
                            finish();
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
        setContentView(R.layout.register);

        Context context = this;

        EditText email = findViewById(R.id.registerEmail);
        EditText password = findViewById(R.id.registerPassword);

        Button register = findViewById(R.id.registerButton);

        RequestQueue queue = Volley.newRequestQueue(this);


        HashMap<String, Object> map=new HashMap<>();

        register.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        map.put("email", email.getText().toString());
                        map.put("password", password.getText().toString().split(""));
                        JSONObject jsonRequest = new JSONObject(map);
                        gateWayGetHeader jsonObjectRequest = new gateWayGetHeader
                                (Request.Method.POST, baseUrl+registerUrl, jsonRequest, new Response.Listener<JSONObject>() {

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
                });

    }
}
