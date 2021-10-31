package com.example.hkeypad;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SimpleIME extends InputMethodService implements KeyboardView.OnKeyboardActionListener
{
    String data="",importantstr="";
    private KeyboardView kv;
    private Keyboard keyboard;
    private String currentString="",str="",res="",each="";
    int count=0,start=0,end=0;
    private boolean caps = false;

    @Override
    public void onKey(int primaryCode, int[] keyCodes)
    {
        InputConnection ic = getCurrentInputConnection();

        playClick(primaryCode);
        switch(primaryCode)
        {
            case Keyboard.KEYCODE_DELETE :
                ic.deleteSurroundingText(1, 0);
                if(!str.isEmpty())
                {
                    str = str.substring(0, str.length() - 1);
                }
                break;

            case Keyboard.KEYCODE_SHIFT:
                caps = !caps;
                keyboard.setShifted(caps);
                kv.invalidateAllKeys();
                break;

            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;

            default:
                char code = (char)primaryCode;
                if(Character.isLetter(code) && caps){
                    code = Character.toUpperCase(code);
                }

                str+=String.valueOf(code);

                /*
                if(str.contains(" ")) {
                    res = wordFilter(str);
                }
                else
                {
                    res=str;
                }
                */

                res = wordFilter(str);


                if(res.equals(str))
                {
                    ic.commitText(String.valueOf(code),1);
                }
                else
                {
                    CharSequence ct=ic.getExtractedText(new ExtractedTextRequest(),0).text;
                    CharSequence bc=ic.getTextBeforeCursor(ct.length(),0);
                    CharSequence ac=ic.getTextBeforeCursor(ct.length(),0);
                    ic.deleteSurroundingText(bc.length(),ac.length());
                    ic.commitText(res,1);
                    str=res;
                    count++;
                }
        }
    }

    @Override
    public void onPress(int primaryCode) {
    }

    @Override
    public void onRelease(int primaryCode) {
    }

    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeUp() {
    }

    @Override
    public View onCreateInputView() {

        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keybroad, null);
        keyboard = new Keyboard(this, R.xml.qwerty);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        return kv;
    }

    private void playClick(int keyCode)
    {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        switch (keyCode) {
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }

    public int StartPosition(String sentence, String word) {
        int startingPosition = sentence.indexOf(word);
        return startingPosition;
    }

    public int EndPosition(String sentence, String word) {
        int startingPosition = sentence.indexOf(word);
        int endingPosition = startingPosition + word.length();
        return endingPosition;
    }

    public String wordFilter(String s)
    {
        String letters="";

        String mainstr1="";
        String mainstr2="";
        List<String>Hatewords=new ArrayList<String>();

        InputStream inputStream = getResources().openRawResource(R.raw.hatewords);
        CSVFile csvFile = new CSVFile(inputStream);
        Hatewords = csvFile.read();

        for(String s1:Hatewords)
        {
          //  if(s.contains("*"))
          //  {
           //     currentString = word_separator(s1, s);
            //}


            mainstr1=s;
            mainstr2=s1;

//            importantstr=mainstr1.substring(StartPosition(s,s1),EndPosition(s,s1));

            if((mainstr1.toLowerCase()).contains(mainstr2.toLowerCase()))
            {
                currentString= s.substring(StartPosition(mainstr1.toLowerCase(),mainstr2.toLowerCase()),EndPosition(mainstr1.toLowerCase(),mainstr2.toLowerCase()));

                Log.d("gettin",mainstr2+"------------------------------------------------");
            }
        }

        letters = s.replaceAll(currentString, "");

        if (s.equals("")) return "";
        return letters;
    }

    public class CSVFile {
        InputStream inputStream;

        public CSVFile(InputStream inputStream){
            this.inputStream = inputStream;
        }

        public List read(){
            List<String>resultList = new ArrayList<String>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null) {
                    String row = String.valueOf(csvLine);//.toString();// .split(",");
                    resultList.add(row);
                }
            }
            catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: "+ex);
            }
            finally {
                try {
                    inputStream.close();
                }
                catch (IOException e) {
                    throw new RuntimeException("Error while closing input stream: "+e);
                }
            }
            return resultList;
        }
    }

    public String word_separator(String str,String pattern)
    {
        String result="";
        String words []= pattern.split(" ");
        for(String w:words)
        {
            if(strmatch(str,w,str.length(),w.length())) {
                result = w;
            }
        }
        return result;
    }


    public boolean strmatch(String str, String pattern, int n, int m)
    {
        if (m == 0)
            return (n == 0);

        boolean[][] lookup = new boolean[n + 1][m + 1];

        for (int i = 0; i < n + 1; i++)
            Arrays.fill(lookup[i], false);

        lookup[0][0] = true;

        for (int j = 1; j <= m; j++)
            if (pattern.charAt(j - 1) == '*')
                lookup[0][j] = lookup[0][j - 1];

        for (int i = 1; i <= n; i++)
        {
            for (int j = 1; j <= m; j++)
            {
                if (pattern.charAt(j - 1) == '*')
                    lookup[i][j] = lookup[i][j - 1]
                            || lookup[i - 1][j];

                else if (pattern.charAt(j - 1) == '?'
                        || str.charAt(i - 1)
                        == pattern.charAt(j - 1))
                    lookup[i][j] = lookup[i - 1][j - 1];

                else
                    lookup[i][j] = false;
            }
        }
        return lookup[n][m];
    }
}