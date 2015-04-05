package vandy.mooc;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.os.Handler;
import android.view.KeyEvent;

/**
 * An Activity that downloads an image, stores it in a local file on
 * the local device, and returns a Uri to the image file.
 */
public class DownloadImageActivity extends Activity {
    /**
     * Debugging tag used by the Android logger.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * Hook method called when a new instance of Activity is created.
     * One time initialization code goes here, e.g., UI layout and
     * some class scope variable initialization.
     *
     * @param savedInstanceState object that contains saved state information.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Always call super class for necessary
        // initialization/implementation.
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");

        // Get the URL associated with the Intent data.
        final Uri downloadUrl = getIntent().getData();
        Log.i(TAG, "Download address is " + downloadUrl.toString());

        // Download the image in the background, create an Intent that
        // contains the path to the image file, and set this as the
        // result of the Activity.

        // A helper class DownloadImageAsync was created
        // This class extends the Android AsyncTask in order to cope with the 2 solutions I propose here
        final DownloadImageAsync downloadTask = new DownloadImageAsync();

        // Variant 1: Download data using the Android Async task
        // The AsyncTask is implemented using handlers, but the handlers are hidden to the developer
//        new DownloadImageAsync().execute(downloadUrl);


        // Variant 2: Download data using the Android "HaMeR"
        // concurrency framework.  Note that the finish() method
        // should be called in the UI thread, whereas the other
        // methods should be called in the background thread.

        // Create a new handler that is used to post a runnable in the Looper of the main (UI) thread
        // By default the handler will belong to the thread in which is created
        final Handler mainThreadHandler = new Handler();

        // Create a new thread that will download the image in the background
        Thread downloadBackgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Download the image in the background
                final Uri result = downloadTask.doInBackground(downloadUrl);
                // When the download is complete, post a Runnable to the main (UI) thread
                // This Runnable is responsible to process the result, send it back to the MainActivity and close this activity
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        downloadTask.onPostExecute(result);
                    }
                });
            }
        });
        // Start the background thread
        downloadBackgroundThread.start();
    }

    class DownloadImageAsync extends AsyncTask<Uri, Integer, Uri> {

        // Inherited
        protected void onPreExecute() {

        }
        // Inherited
        protected void onPostExecute(Uri result) {

            // Create a new intent
            Intent returnIntent = new Intent();
            if (result != null) {
                Log.i(TAG, "Background processing successful");
                // Save the Uri result as an string extra
                returnIntent.putExtra("image", result.toString());
                // Set Activity's result with result code RESULT_OK
                setResult(RESULT_OK, returnIntent);
            }
            else {
                Log.i(TAG, "Background processing error");
                // Set Activity's result with result code RESULT_CANCELED - a better result code can be defied here
                setResult(RESULT_CANCELED, returnIntent);
            }

            // Finish the Activity
            finish();
        }

        protected Uri doInBackground(Uri... adressUrl) {
            Log.i(TAG, "Background processing on URL " + adressUrl[0].toString());
            return DownloadUtils.downloadImage(getApplicationContext(), adressUrl[0]);
        }
    }
}
