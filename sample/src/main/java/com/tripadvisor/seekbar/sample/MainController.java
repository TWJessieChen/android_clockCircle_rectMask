package com.tripadvisor.seekbar.sample;

import android.os.Handler;
import android.os.Message;

import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created by Jessie.PO.Chen on 2015/4/28.
 */
public class MainController extends Handler {
    private static final String TAG = MainController.class.getSimpleName();

    public static final int MSG_STATUS_CHANGED_RESULT_TO_UI = 0xAFFF;

    private static MainController sMainController = null;

    public static final int MSG_START_TIME = 1;

    public static final int MSG_END_TIME = 2;


    private CopyOnWriteArrayList<Callback> mListenerList = new CopyOnWriteArrayList<>();

    public static synchronized MainController getInstance() {
        if (null == sMainController) {
            synchronized (MainController.class) {
                if (sMainController == null) {
                    sMainController = new MainController();
                }
            }
        }
        return sMainController;
    }

    @Override
    public void handleMessage(final Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            default:
                notifyAllListeners(msg);
                break;
        }
    }

    public void registerUiListener(Callback aCallback) {
        if (!mListenerList.contains(aCallback)) {
            mListenerList.add(aCallback);
        }
    }

    public boolean deregisterUiListener(Handler.Callback aCallback) {
        return mListenerList.remove(aCallback);
    }

    private void notifyAllListeners(Message aMessage) {
        for (Callback callback : mListenerList) {
            callback.handleMessage(aMessage);
        }
    }


}
