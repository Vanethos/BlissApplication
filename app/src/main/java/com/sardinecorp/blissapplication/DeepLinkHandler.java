package com.sardinecorp.blissapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.airbnb.deeplinkdispatch.DeepLink;
import com.sardinecorp.blissapplication.ui.MainActivity;

/*
 * This class is not used on the project
 * It's main objective is to fetch the URI and start the main activity with the intended action
 */

//@DeepLink("blissrecruitment://questions")
public class DeepLinkHandler extends AppCompatActivity {

    public static final String FRAGMENT_TO_GO = "FRAGMENT_TO_GO";
    public static final String FILTER = "FILTER";
    public static final String QUESTION_ID = "QUESTION_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_link_handler);
        // Set a bundle for the Main Activity
        Bundle bundle = new Bundle();
        Intent intent = getIntent();
        Log.d("DEEPLINK", "I AM HERE BY A DEEP LNK");
        if (intent.getBooleanExtra(DeepLink.IS_DEEP_LINK, false)) {
            Log.d("URI", "I WAS HERE! WITH NON NULL PARAMETERS");
            Bundle parameters = intent.getExtras();
            if (parameters != null) {
                String questionFilter = parameters.getString("question_filter");
                String questionID = parameters.getString("question_id");
                // we have the /question_filter with no value in it
                if (questionFilter != null && questionFilter.trim().length() == 0 && questionID == null) {
                    bundle.putString(FRAGMENT_TO_GO, MainActivity.FRAGMENT_LIST_WITHOUT_FILTER_TAG);
                } else if (questionFilter != null && questionID == null) {
                    bundle.putString(FRAGMENT_TO_GO, MainActivity.FRAGMENT_LIST_WITH_FILTER_TAG);
                    bundle.putString(FILTER, questionFilter);
                } else if (questionID != null) {
                    try {
                        // try parsing the string to an int and check if it's a valid number
                        bundle.putString(FRAGMENT_TO_GO, MainActivity.FRAGMENT_QUESTION_TAG);
                        bundle.putInt(QUESTION_ID, Integer.parseInt(questionID));
                    } catch (NumberFormatException nfe) {
                        // if it isn't, then we are going to be transfered to the list of questions
                        nfe.printStackTrace();
                        Toast.makeText(this, "Invalid Question ID", Toast.LENGTH_SHORT).show();
                        bundle.putString(FRAGMENT_TO_GO, MainActivity.FRAGMENT_LIST_TAG);
                    }
                }
            }
            // goto main activity
            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.putExtras(bundle);
            startActivity(mainIntent);
        }
    }



}
