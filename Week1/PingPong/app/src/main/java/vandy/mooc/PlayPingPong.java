package vandy.mooc;

import java.util.concurrent.CyclicBarrier;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * @class PlayPingPong
 *
 * @brief This class uses elements of the Android HaMeR framework to
 *        create two Threads that alternately print "Ping" and "Pong",
 *        respectively, on the display.
 */
public class PlayPingPong implements Runnable {

    /**
     * Debugging tag
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * Keep track of whether a Thread is printing "ping" or "pong".
     */
    private enum PingPong {
        PING, PONG
    };

    /**
     * Number of iterations to run the ping-pong algorithm.
     */
    private final int mMaxIterations;

    /**
     * The strategy for outputting strings to the display.
     */
    private final OutputStrategy mOutputStrategy;

    /**
     * Define a pair of Handlers used to send/handle Messages via the
     * HandlerThreads.
     */
    private Handler pingHandler;
    private Handler pongHandler;

    /**
     * Define a CyclicBarrier synchronizer that ensures the
     * HandlerThreads are fully initialized before the ping-pong
     * algorithm begins.
     */
    private final CyclicBarrier synchroniser = new CyclicBarrier(2);

    /**
     * Implements the concurrent ping/pong algorithm using a pair of
     * Android Handlers (which are defined as an array field in the
     * enclosing PlayPingPong class so they can be shared by the ping
     * and pong objects).  The class (1) extends the HandlerThread
     * superclass to enable it to run in the background and (2)
     * implements the Handler.Callback interface so its
     * handleMessage() method can be dispatched without requiring
     * additional subclassing.
     */
    class PingPongThread extends HandlerThread implements Handler.Callback {
        /**
         * Keeps track of whether this Thread handles "pings" or
         * "pongs".
         */
        private PingPong mMyType;

        /**
         * Number of iterations completed thus far.
         */
        private int mIterationsCompleted;

        /**
         * Constructor initializes the superclass and type field
         * (which is either PING or PONG).
         */
        public PingPongThread(PingPong myType) {
        	super(myType.toString());
            mMyType = myType;
            mIterationsCompleted = 1;
        }

        /**
         * This hook method is dispatched after the HandlerThread has
         * been started.  It performs ping-pong initialization prior
         * to the HandlerThread running its event loop.
         */
        @Override    
        protected void onLooperPrepared() {
            // Create the Handler that will service this type of
            // Handler, i.e., either PING or PONG.
            if (mMyType == PingPong.PING) {
                pingHandler = new Handler(getLooper(), this);
            }
            else {
                pongHandler = new Handler(getLooper(), this);
            }

            try {
                // Wait for both Threads to initialize their Handlers.
                synchroniser.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Start the PING_THREAD first by (1) creating a Message
            // where the PING Handler is the "target" and the PONG
            // Handler is the "obj" to use for the reply and (2)
            // sending the Message to the PING_THREAD's Handler.
            if (mMyType == PingPong.PING) {
                //Obtain a new message from the "pingHandler" handler
                //The created message will have the "target" of the created message set to "pingHandler"
                Message pingToPongMessage = pingHandler.obtainMessage();
                //Set the "pongHandler" handler as "obj" for the newly created message
                //It will be used for reply
                pingToPongMessage.obj = pongHandler;
                //Send the message to the "PING" thread
                pingHandler.sendMessage(pingToPongMessage);
            }
        }

        /**
         * Hook method called back by HandlerThread to perform the
         * ping-pong protocol concurrently.
         */
        @Override
        public boolean handleMessage(Message reqMsg) {
            // Print the appropriate string if this thread isn't done
            // with all its iterations yet.
            // appropriate code.
            if (mMyType == PingPong.PING) {
                mOutputStrategy.print("PING(" + mIterationsCompleted +")\n");
            }
            else {
                mOutputStrategy.print("PONG(" + mIterationsCompleted +")\n");
            }
            boolean boThreadLooperIsClosed = false;
            if (++mIterationsCompleted <= mMaxIterations) {
            } else {
                // Shutdown the HandlerThread to the main PingPong
                // thread can join with it.
                getLooper().quit();

                // Mark the looper as closed to stop the "ping - pong" of messages
                // between threads
                boThreadLooperIsClosed = true;
            }

            // Create a Message that contains the Handler as the
            // reqMsg "target" and our Handler as the "obj" to use for
            // the reply.
            Handler responseMessageHandler;
            //Initialize the sender handler with the "target" received inside the reqMsg message
            Handler senderHandler = reqMsg.getTarget();
            Message replyMessage = null;

            //Try to cast the received "obj" to a Handler object
            if ((reqMsg.obj != null) && (Handler.class.isInstance(reqMsg.obj))) {

                //Initialize the response handler with the "obj" received inside the reqMsg message
                responseMessageHandler = Handler.class.cast(reqMsg.obj);
                //Obtain a new message from the response handler
                //The created message will have the "target" of the created message set to "responseMessageHandler"
                replyMessage = responseMessageHandler.obtainMessage();

                //Set the "sender" handler as "obj" for the newly created message
                if (boThreadLooperIsClosed) {
                    // If the lopper of current thread is closed,
                    // initialize the response Handler to null to avoid
                    // sending messages to a dead thread
                    replyMessage.obj = null;
                }
                else {
                    // Set the "sender" handler as "obj" for the newly created message
                    // It will be used in the receiver thread for the reply
                    replyMessage.obj = senderHandler;
                }
            }
            // Return control to the Handler in the other
            // HandlerThread, which is the "target" of the msg
            // parameter.
            if (replyMessage != null) {
                try {
                    //The exception can thrown in case the thread to which this message is sent is already dead
                    replyMessage.getTarget().sendMessage(replyMessage);
                }
                catch (RuntimeException e) {
                }
            }
            return true;
        }
    }

    /**
     * Constructor initializes the data members.
     */
    public PlayPingPong(int maxIterations,
                        OutputStrategy outputStrategy) {
        // Number of iterations to perform pings and pongs.
        mMaxIterations = maxIterations;

        // Strategy that controls how output is displayed to the user.
        mOutputStrategy = outputStrategy;
    }

    /**
     * Start running the ping/pong code, which can be called from a
     * main() method in a Java class, an Android Activity, etc.
     */
    public void run() {
        // Let the user know we're starting. 
        mOutputStrategy.print("Ready...Set...Go!\n");
       
        // Create the ping and pong threads.
        PingPongThread pingThread = new PingPongThread(PingPong.PING);
        PingPongThread pongThread = new PingPongThread(PingPong.PONG);

        // Start ping and pong threads, which cause their Looper to
        // loop.
        pingThread.start();
        pongThread.start();

        // Barrier synchronization to wait for all work to be done
        // before exiting play().
        try {
            pingThread.join();
            pongThread.join();
        }
        catch (InterruptedException e) {
        }

        // Let the user know we're done.
        mOutputStrategy.print("Done!");
    }
}
