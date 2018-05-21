package server.http.android.androidhttpserver;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import server.http.android.androidhttpserver.server.MyServer;


public class MainActivity extends AppCompatActivity {

    private static MyServer server;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.editText);
        editText.setText( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + MyServer.LOCAL_PATH);
        Log.i(MyServer.TAG, "onCreate" + System.getProperty("line.separator") );
        try {
            server = new MyServer(this);
            //server.start();
        } catch (IOException e) {
            Log.e(MyServer.TAG, "onCreate ER :" + e.getMessage() + System.getProperty("line.separator"));
        }

    }

//    @Override
    protected void onStop() {
       super.onStop();
        Log.i(MyServer.TAG, "onStop" + System.getProperty("line.separator") );
       if(server != null) {
        //        server.stop();
       }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            try {
                Process process = Runtime.getRuntime().exec("logcat -d AHS:I *:S");
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));

                String versionName="";

                try {
                 PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                 versionName = packageInfo.versionName;
                 //versionCode = packageInfo.versionCode;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                StringBuilder log=new StringBuilder("Version:" + versionName + System.getProperty("line.separator"));
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    log.insert (0,line + System.getProperty("line.separator") );
                }

                TextView tv = (TextView)findViewById(R.id.textView2);
                tv.setText(log.toString());
            }
            catch (IOException e) {}
            return true;
        }
        else if (id == R.id.action_say) {

            //Body of your click handler
            Thread thread = new Thread(new Runnable(){
                @Override
                public void run(){
                    HttpURLConnection urlConnection = null;
                    try {
                        URL url = new URL("http://localhost:"  + Integer.toString(MyServer.PORT) + "/?word=00" + Integer.toString(MyServer.randInt(1,5)) +  "&mode=1");
                        urlConnection = (HttpURLConnection) url.openConnection();

                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        in.read();
                        in.close();
                    }
                    catch (Exception e ) {
                        Log.e(MyServer.TAG, "Say :" + e.getMessage() + System.getProperty("line.separator"));
                    }
                    finally{
                        if (urlConnection != null ) urlConnection.disconnect();
                    }

                }
            });
            thread.start();

        }
        else if (id == R.id.item1) {
            item.setChecked(!item.isChecked());
        }
        else if (id == R.id.item2) {
            item.setChecked(!item.isChecked());
        }
        else if ( id == R.id.action_close) {
            server.stop();
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(MyServer.TAG, "onResume" + System.getProperty("line.separator") );
        try {
            if (server == null) {
                Log.i(MyServer.TAG, "onResume : isNull" + System.getProperty("line.separator"));
                server = new MyServer(this);
            }

            if (!server.isAlive()) {
                Log.i(MyServer.TAG, "onResume : isAlive" + System.getProperty("line.separator"));
                server.start();
            }
        } catch (IOException e) {
            Log.e(MyServer.TAG, "onResume ER :" + e.getMessage() + System.getProperty("line.separator"));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(MyServer.TAG, "onPause" + System.getProperty("line.separator") );
    }

}
