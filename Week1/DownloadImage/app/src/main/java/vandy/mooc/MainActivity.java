package vandy.mooc;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

/**
 * A main Activity that prompts the user for a URL to an image and
 * then uses Intents and other Activities to download the image and
 * view it.
 */
public class MainActivity extends LifecycleLoggingActivity {
    /**
     * Debugging tag used by the Android logger.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * A value that uniquely identifies the request to download an
     * image.
     */
    private static final int DOWNLOAD_IMAGE_REQUEST = 1;

    /**
     * EditText field for entering the desired URL to an image.
     */
    private EditText mUrlEditText;

    /**
     * URL for the image that's downloaded by default if the user
     * doesn't specify otherwise.
     */
    private Uri mDefaultUrl =
        Uri.parse("http://www.dre.vanderbilt.edu/~schmidt/robot.png");

    /**
     * Hook method called when a new instance of Activity is created.
     * One time initialization code goes here, e.g., UI layout and
     * some class scope variable initialization.
     *
     * @param savedInstanceState object that contains saved state information.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Always call super class for necessary
        // initialization/implementation.
        super.onCreate(savedInstanceState);

        // Set the default layout.
        setContentView(R.layout.main_activity);

        // Set the text view to always display the default URL
        final TextView defaultUrlView = (TextView)findViewById(R.id.textView1);
        defaultUrlView.setText(mDefaultUrl.toString());

        // Cache the EditText that holds the urls entered by the user
        // (if any).
        mUrlEditText = (EditText)findViewById(R.id.url);
    }

    /**
     * Called by the Android Activity framework when the user clicks
     * the "Find Address" button.
     *
     * @param view The view.
     */
    public void downloadImage(View view) {
        try {
            // Hide the keyboard.
            hideKeyboard(this,
                         mUrlEditText.getWindowToken());

            // Call the makeDownloadImageIntent() factory method to
            // create a new Intent to an Activity that can download an
            // image from the URL given by the user.  In this case
            // it's an Intent that's implemented by the
            // DownloadImageActivity.
            Uri downloadLocationUrl = getUrl();
            Intent downloadIntent = null;

            if (downloadLocationUrl != null) {
                // Create the intent to implicitly start the activity for result
                // The activity that will be started is configured in AndroidManifest.xml
                downloadIntent = makeDownloadImageIntent(downloadLocationUrl);
            }

            // Start the Activity associated with the Intent, which
            // will download the image and then return the Uri for the
            // downloaded image file via the onActivityResult() hook
            // method.
            if (downloadIntent != null)
            {
                // Start the activity that will download the image
                startActivityForResult(downloadIntent, DOWNLOAD_IMAGE_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Hook method called back by the Android Activity framework when
     * an Activity that's been launched exits, giving the requestCode
     * it was started with, the resultCode it returned, and any
     * additional data from it.
     */
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {

        Log.i(TAG, "onActivityResult()");
        // Check if the started Activity completed successfully.
        // code.
        if (resultCode == RESULT_OK) {
            // right code.
            if (requestCode == DOWNLOAD_IMAGE_REQUEST) {
                // Call the makeGalleryIntent() factory method to
                // create an Intent that will launch the "Gallery" app
                // by passing in the path to the downloaded image
                // file.
                String imagePath = data.getStringExtra("image");
                if (imagePath != null) {

                    Log.i(TAG, "File downloaded at " + imagePath);

                    try {
                        Intent viewBitmapIntent = makeGalleryIntent(data.getStringExtra("image"));

                        // Start the Gallery Activity.
                        startActivity(viewBitmapIntent);
                    }
                    catch (ActivityNotFoundException e) {
                        Log.i(TAG, "No activity started due to ActivityNotFoundException");
                    }
                }
            }
        }
        // Check if the started Activity did not complete successfully
        // and inform the user a problem occurred when trying to
        // download contents at the given URL.
        // code.
        else if (requestCode == DOWNLOAD_IMAGE_REQUEST) {
            Toast.makeText(this,
                    "Error while loading image",
                    Toast.LENGTH_SHORT).show();
        }
    }    

    /**
     * Factory method that returns an Intent for viewing the
     * downloaded image in the Gallery app.
     */
    private Intent makeGalleryIntent(String pathToImageFile) {
        // Create an intent that will start the Gallery app to view
        // the image.
    	// code.
        // Create a new file based on the absolute path on which the data is aved
        File imageFile = new File(pathToImageFile);
        // Create a new intent to implicitly start the Gallery activity to view the downloaded image
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_VIEW);
        galleryIntent.setDataAndType(Uri.fromFile(imageFile), "image/*");
        return galleryIntent;
    }

    /**
     * Factory method that returns an Intent for downloading an image.
     */
    private Intent makeDownloadImageIntent(Uri url) {
        // Create an intent that will download the image from the web.
    	// code.
    	return new Intent(Intent.ACTION_WEB_SEARCH, url);
    }

    /**
     * Get the URL to download based on user input.
     */
    protected Uri getUrl() {
        Uri url;

        // Get the text the user typed in the edit text (if anything).
        url = Uri.parse(mUrlEditText.getText().toString());

        // If the user didn't provide a URL then use the default.
        String uri = url.toString();
        if (uri == null || uri.equals(""))
            url = mDefaultUrl;

        // Do a sanity check to ensure the URL is valid, popping up a
        // toast if the URL is invalid.
        // proper code.
        // The URLUtil class is used to validate the url
        if (URLUtil.isValidUrl(url.toString()))
            return url;
        else {
            Toast.makeText(this,
                           "Invalid URL",
                           Toast.LENGTH_SHORT).show();
            return null;
        } 
    }

    /**
     * This method is used to hide a keyboard after a user has
     * finished typing the url.
     */
    public void hideKeyboard(Activity activity,
                             IBinder windowToken) {
        InputMethodManager mgr =
            (InputMethodManager) activity.getSystemService
            (Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken,
                                    0);
    }
}
