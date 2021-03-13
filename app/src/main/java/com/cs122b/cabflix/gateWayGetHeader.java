package com.cs122b.cabflix;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class gateWayGetHeader extends JsonObjectRequest {
    public gateWayGetHeader(int post, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(post, url, jsonRequest, listener, errorListener);
    }



    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            HashMap<String, String> map=new HashMap<String, String>();
            map.put("transaction_id", response.headers.get("transaction_id"));
            JSONObject jsonResponse = new JSONObject(map);
            return Response.success(jsonResponse, HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }
}
