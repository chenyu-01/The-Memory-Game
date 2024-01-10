package com.team01.thememorygame.Utils;
import android.os.SystemClock;

// prevent users from causing problems by frequent clicks
public class DelayAction {
    private long mLastEvt = 0; // Prevent frequent clicks
    private int mTimeInner = 50; // click interval Default 500ms
    public DelayAction() {
    }
    // Set delay interval
    public DelayAction setInner(int ms) {
        mTimeInner = ms;
        return this;
    }
    // Check if the event is valid
    public boolean invalid() {
        return !valid();
    }
    // Check if the event is valid
    public boolean valid() {
        long cur  = SystemClock.uptimeMillis();
        // The system time has been modified so that it is less than
        if (mLastEvt==0||cur>(mLastEvt+mTimeInner)||cur<mLastEvt) {
            mLastEvt = cur;
            return true;
        }
        return false;
    }
    // clear
    public void clear() {
        mLastEvt = 0;
    }
}
