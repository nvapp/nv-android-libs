package com.nvapp.android.libs.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

public class BaseView extends View {

    public BaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable p) {
        this.onRestoreInstanceStateSimple(p);
    }

    protected Parcelable onSaveInstanceState() {
        return this.onSaveInstanceStateSimple();
    }

    private void onRestoreInstanceStateSimple(Parcelable p) {
        if (!(p instanceof Bundle)) {
            throw new RuntimeException("unexpected bundle");
        }
        Bundle b = (Bundle) p;
        Parcelable sp = b.getParcelable("super");
        super.onRestoreInstanceState(sp);
    }

    private Parcelable onSaveInstanceStateSimple() {
        Parcelable p = super.onSaveInstanceState();
        Bundle b = new Bundle();
        b.putParcelable("super", p);
        return b;
    }
}
