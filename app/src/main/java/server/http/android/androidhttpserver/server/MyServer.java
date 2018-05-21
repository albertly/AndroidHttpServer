package server.http.android.androidhttpserver.server;
import java.io.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;


/**
 * Created by andrei on 7/30/15.
 */
public class MyServer extends NanoHTTPD implements TextToSpeech.OnInitListener {
    public final static int PORT = 8080;
    private TextToSpeech engine = null;
    private double pitch=1.0;
    private double speed=1.0;
    public final static String LOCAL_PATH =  "/ParseYourDictionary/1.0.0.0/";
    public final static String TAG =  "AHS";
    private final String SAY_SENTENCE = "1";
    private final String SAY_WORD = "0";
    private final String GET_SYN = "2";
    private final String WRITE_SYN = "3";
    private Context myContext;

    public MyServer(Context context) throws IOException {
        super(PORT);
        myContext = context;
        start();
        Log.i( TAG, "\nRunning! Point your browers to http://localhost:"  + Integer.toString(PORT) + "/" + System.getProperty("line.separator") );
    }

    @Override
    public void onInit(int status) {
        Log.i(TAG, "Speech OnInit - Status ["+status+"]" + System.getProperty("line.separator"));

        if (status == TextToSpeech.SUCCESS) {
            Log.i(TAG, "SpeechSuccess!" + System.getProperty("line.separator"));
            int result = engine.setLanguage(Locale.US);
            if(result==TextToSpeech.LANG_MISSING_DATA ||
                      result==TextToSpeech.LANG_NOT_SUPPORTED){
                  Log.i(TAG, "This Language is not supported" + System.getProperty("line.separator"));
             }
              else {
                  Log.e(TAG, "setLanguage: " + Integer.toString(result) + System.getProperty("line.separator") );
             }

            engine.setPitch((float) pitch);
            engine.setSpeechRate((float) speed);

            if (Build.VERSION.SDK_INT >= 15) {
                UtteranceProgressListener listener = new UtteranceProgressListener() {

                    @Override
                    public void onStart(String utteranceId) {
                        Log.i(TAG, "UtteranceProgressListener  OnStart: " + utteranceId + System.getProperty("line.separator"));

                    }

                    @Override
                    public void onError(String utteranceId) {
                        Log.i(TAG, "UtteranceProgressListener  OnError: " + utteranceId + System.getProperty("line.separator"));

                    }

                    @Override
                    public void onDone(String utteranceId) {
                        Log.i(TAG, "UtteranceProgressListener  onDone: " + utteranceId + System.getProperty("line.separator"));
                    }
                };
                engine.setOnUtteranceProgressListener(listener);
            } else {
                engine.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {

                    @Override
                    public void onUtteranceCompleted(String arg0) {
                        Log.i(TAG, "UtteranceProgressListener  onDone: " + arg0 + System.getProperty("line.separator"));
                    }

                });

            }
        }
        else if (status == TextToSpeech.ERROR) {
            Log.e(TAG, "TextToSpeech.ERROR: Unable to initialize Text-To-Speech engine" + System.getProperty("line.separator"));
        }

}

    public static int randInt(int min, int max) {

        // NOTE: This will (intentionally) not run as written so that folks
        // copy-pasting have to think about how to initialize their
        // Random instance.  Initialization of the Random instance is outside
        // the main scope of the question, but some decent options are to have
        // a field that is initialized once and then re-used as needed or to
        // use ThreadLocalRandom (if using at least Java 1.7).

        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive

        return rand.nextInt((max - min) + 1) + min;
    }

    private String ReadFileToString(String word) {
        FileInputStream fs=null;
        DataInputStream dis = null;
        BufferedReader br = null;
        StringBuilder  sentence = new StringBuilder("");
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + LOCAL_PATH + "memrise/"  + word.substring(0,2) + "/" + word + ".syn.txt";

        File file = new File(filePath );
        if (file.exists())
            try {
                fs = new FileInputStream(file);
                dis = new DataInputStream(fs);
                br = new BufferedReader(new InputStreamReader(dis));

                String str= br.readLine();
                while(str!=null) {
                    sentence.append(str);
                    str=br.readLine();
                }
            }
            catch (FileNotFoundException  ex01) {}
            catch (IOException ex02) {
                Log.e(TAG, "ReadFileToString" + ex02.toString() + System.getProperty("line.separator") );
            }
            finally {
                try {
                    if (br != null) br.close();
                    if (dis != null) dis.close();
                    if (fs != null) fs.close();
                }
                catch (Exception exc) {}
            }

        return sentence.toString() ;
    }

    private String GetSentence(String word) {
        FileInputStream fs=null;
        DataInputStream dis = null;
        BufferedReader br = null;
        String sentence = null;
        ArrayList<String> myList = new ArrayList<String>();
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + LOCAL_PATH  + word.substring(0,2) + "/" + word + ".bin.txt";

        File file = new File(filePath );
        if (file.exists())
            try {
                fs = new FileInputStream(file);
                dis = new DataInputStream(fs);
                br = new BufferedReader(new InputStreamReader(dis));

                String str= br.readLine();
                while(str!=null) {
                    myList.add(str);
                    str=br.readLine();
                }

                int index = randInt(0,myList.size()-1);
                sentence = myList.get(index);
            }
            catch (FileNotFoundException  ex01) {}
            catch (IOException ex02) {
                Log.i(TAG, "GetSentence" + ex02.toString() + System.getProperty("line.separator") );
            }
            finally {
                try {
                    if (br != null) br.close();
                    if (dis != null) dis.close();
                    if (fs != null) fs.close();
                }
                catch (Exception exc) {}
            }


        if (sentence == null) sentence = word;

        return sentence;
    }
    @Override
    public Response serve(IHTTPSession session) {

        String word = null;
        String mode = null;
        word = session.getParms().get("word");
        mode = session.getParms().get("mode");

        Log.i(TAG, "serve: mode " + mode + " word " + word);

        if (word != null && word.length() > 2) {
            if (mode == null || mode.length() == 0) {
                mode = SAY_WORD;
            }

            switch(mode) {
                case SAY_SENTENCE:
                    speech(GetSentence(word));
                    break;
                case SAY_WORD:
                    speech(word);
                    break;
                case GET_SYN:
                    return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, ReadFileToString(word));
                case WRITE_SYN:
                    // not implemented
                    break;
            }
        }

        return  newFixedLengthResponse("<html><body><p>We serve " + word + " !!</p></body></html>");
    }

    @Override
    public void start() throws IOException {
        super.start();

        engine = new TextToSpeech( myContext, this);
        Log.i(TAG, "After Speech Engine New");
    }

    @Override
    public void stop() {

     //   if (engine != null) {
     //       engine.stop();
     //       engine.shutdown();
          //  engine = null;
     //   }
        super.stop();
        Log.i(TAG, "Server Stop");
    }

    private void speech(String text) {


      //  engine.setLanguage(Locale.US);
      //  if(result==TextToSpeech.LANG_MISSING_DATA ||
      //          result==TextToSpeech.LANG_NOT_SUPPORTED){
      //      Log.i(TAG, "This Language is not supported" + System.getProperty("line.separator"));
      //  }
      //  else {
      //      Log.e(TAG, "setLanguage: " + Integer.toString(result) + System.getProperty("line.separator") );
       // }


        Log.i(TAG, "speech: " + text + System.getProperty("line.separator") );

        HashMap<String, String> params = new HashMap<String, String>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,text);

        int result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, params);
        if (result != TextToSpeech.SUCCESS) {
            Log.e(TAG, "speech Error: " + Integer.toString(result) + System.getProperty("line.separator") );
        }

    }

}
