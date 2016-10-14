package spartansaferide.sjsu.edu.driver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class DriverLoginActivity extends AppCompatActivity {

    public static final String LOGIN_URL = "http://saferide.nagkumar.com/login/";

    public static final String KEY_USERNAME="username";
    public static final String KEY_PASSWORD="password";

    private Button mLogin;
    private EditText mUsername, mPassword;
    private TextView invalidLoginMsg;
    private String username, password;
    final int test = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);


        mUsername   = (EditText)findViewById(R.id.username);
        mPassword   = (EditText)findViewById(R.id.password);
        invalidLoginMsg = (TextView) findViewById(R.id.InvalidLogin);
        invalidLoginMsg.setVisibility(View.GONE);
        Log.d("Username : ","On Create");

        mLogin = (Button)findViewById(R.id.login_button);
        mLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                username = mUsername.getText().toString().trim();
                password = mPassword.getText().toString().trim();
                Log.d("Username :", username);
                Log.d("password :", password);

//                if(username.equals("test@sjsu.edu")  && password.equals("test")){
//
//                    Log.d("Success "," Login");
//                    Intent intent = new Intent(DriverLoginActivity.this, DriverMapsActivity.class);
//                    startActivity(intent);
//                }
//
//                else
//                {
//                    invalidLoginMsg = (TextView) findViewById(R.id.InvalidLogin);
//                    invalidLoginMsg.setVisibility(View.VISIBLE);
//
//                }

                StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if(response.trim().equals("success")){
                                    goToNextPage();
                                }else{
                                    Toast.makeText(DriverLoginActivity.this,response,Toast.LENGTH_LONG).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(DriverLoginActivity.this,error.toString(),Toast.LENGTH_LONG ).show();
                            }
                        }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> map = new HashMap<String,String>();
                        map.put(KEY_USERNAME,username);
                        map.put(KEY_PASSWORD,password);
                        return map;
                    }
                };

                RequestQueue requestQueue = Volley.newRequestQueue(DriverLoginActivity.this);
                requestQueue.add(stringRequest);

            }
        });

    }

    private void goToNextPage() {
        Intent intent = new Intent(DriverLoginActivity.this, DriverMapsActivity.class);
        startActivity(intent);
    }
}
