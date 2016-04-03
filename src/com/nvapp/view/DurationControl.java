//package com.nvapp.view;
//
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.res.TypedArray;
//import android.os.Parcel;
//import android.os.Parcelable;
//import android.support.v4.app.DialogFragment;
//import android.support.v4.app.FragmentManager;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.util.SparseArray;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.Button;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//public class DurationControl extends LinearLayout implements android.view.View.OnClickListener {
//	private static final String tag = "DurationControl";
//	private Calendar fromDate = null;
//	private Calendar toDate = null;
//	// 1: days, 2: weeks
//	private static int ENUM_DAYS = 1;
//	private static int ENUM_WEEKS = 1;
//	private int durationUnits = 1;
//
//	// public interface
//	public long getDuration() {
//		if (validate() == false)
//			return -1;
//		long fromMillis = fromDate.getTimeInMillis();
//		long toMillis = toDate.getTimeInMillis();
//		long diff = toMillis - fromMillis;
//		long day = 24 * 60 * 60 * 1000;
//		long diffInDays = diff / day;
//		long diffInWeeks = diff / (day * 7);
//		if (durationUnits == ENUM_WEEKS) {
//			return diffInDays;
//		}
//		return diffInWeeks;
//	}
//
//	public boolean validate() {
//		if (fromDate == null || toDate == null) {
//			return false;
//		}
//
//		if (toDate.after(fromDate)) {
//			return true;
//		}
//		return false;
//	}
//
//	public DurationControl(Context context) {
//		super(context);
//		initialize(context);
//	}
//
//	public DurationControl(Context context, AttributeSet attrs, int defStyle) {
//		super(context, attrs, defStyle);
//		TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.DurationComponent, 0, 0);
//		durationUnits = t.getInt(R.styleable.DurationComponent_durationUnits, durationUnits);
//		t.recycle();
//		initialize(context);
//	}
//
//	public DurationControl(Context context, AttributeSet attrs) {
//		this(context, attrs, 0);
//	}
//
//	private void initialize(Context context) {
//		LayoutInflater lif = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		lif.inflate(R.layout.duration_view_layout, this);
//		Button b = (Button) this.findViewById(R.id.fromButton);
//		b.setOnClickListener(this);
//		b = (Button) this.findViewById(R.id.toButton);
//		b.setOnClickListener(this);
//		this.setSaveEnabled(true);
//	}
//
//	private FragmentManager getFragmentManager() {
//		Context c = getContext();
//		if (c instanceof Activity) {
//			return ((Activity) c).getFragmentManager();
//		}
//		throw new RuntimeException("Activity context expected instead");
//	}
//
//	public void onClick(View v) {
//		Button b = (Button) v;
//		if (b.getId() == R.id.fromButton) {
//			DialogFragment newFragment = new DatePickerFragment(this, R.id.fromButton);
//			newFragment.show(getFragmentManager(), "com.androidbook.tags.datePicker");
//			return;
//		}
//
//		// Otherwise
//		DialogFragment newFragment = new DatePickerFragment(this, R.id.toButton);
//		newFragment.show(getFragmentManager(), "com.androidbook.tags.datePicker");
//		return;
//	}// eof-onclick
//
//	public void onDateSet(int buttonId, int year, int month, int day) {
//		Calendar c = getDate(year, month, day);
//		if (buttonId == R.id.fromButton) {
//			setFromDate(c);
//			return;
//		}
//		setToDate(c);
//	}
//
//	private void setFromDate(Calendar c) {
//		if (c == null)
//			return;
//		this.fromDate = c;
//		TextView tc = (TextView) findViewById(R.id.fromDate);
//		tc.setText(getDateString(c));
//	}
//
//	private void setToDate(Calendar c) {
//		if (c == null)
//			return;
//		this.toDate = c;
//		TextView tc = (TextView) findViewById(R.id.toDate);
//		tc.setText(getDateString(c));
//	}
//
//	private Calendar getDate(int year, int month, int day) {
//		Calendar c = Calendar.getInstance();
//		c.set(year, month, day);
//		return c;
//	}
//
//	public static String getDateString(Calendar c) {
//		if (c == null)
//			return "null";
//		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
//		df.setLenient(false);
//		String s = df.format(c.getTime());
//		return s;
//	}
//
//	@Override
//	protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
//		// Don't call this so that children won't be explicitly saved
//		// super.dispatchSaveInstanceState(container);
//		// Call your self onsavedinstancestate
//		super.dispatchFreezeSelfOnly(container);
//		Log.d(tag, "in dispatchSaveInstanceState");
//	}
//
//	@Override
//	protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
//		// Don't call this so that children won't be explicitly saved
//		// .super.dispatchRestoreInstanceState(container);
//		super.dispatchThawSelfOnly(container);
//		Log.d(tag, "in dispatchRestoreInstanceState");
//	}
//
//	@Override
//	protected void onRestoreInstanceState(Parcelable state) {
//		Log.d(tag, "in onRestoreInstanceState");
//		if (!(state instanceof SavedState)) {
//			super.onRestoreInstanceState(state);
//			return;
//		}
//		// it is our state
//		SavedState ss = (SavedState) state;
//		// Peel it and give the child to the super class
//		super.onRestoreInstanceState(ss.getSuperState());
//		// this.fromDate = ss.fromDate;
//		// this.toDate= ss.toDate;
//		this.setFromDate(ss.fromDate);
//		this.setToDate(ss.toDate);
//	}
//
//	@Override
//	protected Parcelable onSaveInstanceState() {
//		Log.d(tag, "in onSaveInstanceState");
//		Parcelable superState = super.onSaveInstanceState();
//		SavedState ss = new SavedState(superState);
//		ss.fromDate = this.fromDate;
//		ss.toDate = this.toDate;
//		return ss;
//	}
//
//	/*
//	 * ***************************************************************
//	 * Saved State inner static class
//	 * ***************************************************************
//	 */
//	public static class SavedState extends BaseSavedState {
//		// null values are allowed
//		private Calendar fromDate;
//		private Calendar toDate;
//
//		SavedState(Parcelable superState) {
//			super(superState);
//		}
//
//		@Override
//		public void writeToParcel(Parcel out, int flags) {
//			super.writeToParcel(out, flags);
//			if (fromDate != null) {
//				out.writeLong(fromDate.getTimeInMillis());
//			} else {
//				out.writeLong(-1L);
//			}
//			if (fromDate != null) {
//				out.writeLong(toDate.getTimeInMillis());
//			} else {
//				out.writeLong(-1L);
//			}
//		}
//
//		@Override
//		public String toString() {
//			StringBuffer sb = new StringBuffer("fromDate:" + DurationControl.getDateString(fromDate));
//			sb.append("fromDate:" + DurationControl.getDateString(toDate));
//			return sb.toString();
//		}
//
//		@SuppressWarnings("hiding")
//		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
//			public SavedState createFromParcel(Parcel in) {
//				return new SavedState(in);
//			}
//
//			public SavedState[] newArray(int size) {
//				return new SavedState[size];
//			}
//		};
//
//		// Read back the values
//		private SavedState(Parcel in) {
//			super(in);
//			// Read the from date
//			long lFromDate = in.readLong();
//			if (lFromDate == -1) {
//				fromDate = null;
//			} else {
//				fromDate = Calendar.getInstance();
//				fromDate.setTimeInMillis(lFromDate);
//			}
//
//			long lToDate = in.readLong();
//			if (lFromDate == -1) {
//				toDate = null;
//			} else {
//				toDate = Calendar.getInstance();
//				toDate.setTimeInMillis(lToDate);
//			}
//		}
//	}// eof-state-class
//}// eof-class
