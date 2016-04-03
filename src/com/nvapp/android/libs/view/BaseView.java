package com.nvapp.android.libs.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

public class BaseView extends View {

    public BaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable p) {
        this.onRestoreInstanceStateStandard(p);
    }

    protected Parcelable onSaveInstanceState() {
        return this.onSaveInstanceStateStandard();
    }

    private void onRestoreInstanceStateStandard(Parcelable state) {
        // If it is not yours doesn't mean it is BaseSavedState
        // You may have a parent in your hierarchy that has their own
        // state derived from BaseSavedState
        // It is like peeling an onion or a Russian doll
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        // it is our state
        SavedState ss = (SavedState) state;
        // Peel it and give the child to the super class
        super.onRestoreInstanceState(ss.getSuperState());
    }

    private Parcelable onSaveInstanceStateStandard() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        return ss;
    }

    public static class SavedState extends BaseSavedState {
        int defRadius;

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(defRadius);
        }

        // Read back the values
        private SavedState(Parcel in) {
            super(in);
            defRadius = in.readInt();
        }

        @Override
        public String toString() {
            return "CircleView defRadius:" + defRadius;
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
