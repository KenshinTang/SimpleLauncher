package com.kapplication.launcher.widget;

import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.starcor.xul.IXulExternalView;
import com.starcor.xul.Prop.XulAttr;
import com.starcor.xul.Prop.XulStyle;
import com.starcor.xul.Utils.XulPropParser;
import com.starcor.xul.XulLayout;
import com.starcor.xul.XulUtils;
import com.starcor.xul.XulView;


public class XulExt_GSYVideoPlayer extends StandardGSYVideoPlayer implements IXulExternalView {

	public XulExt_GSYVideoPlayer(Context context) {
		super(context);
	}

	@Override
	public void extMoveTo(int x, int y, int width, int height) {
		FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.getLayoutParams();
		layoutParams.leftMargin = x;
		layoutParams.topMargin = y;
		layoutParams.width = width;
		layoutParams.height = height;
		this.requestLayout();
	}

	@Override
	public void extMoveTo(Rect rect) {
		extMoveTo(rect.left, rect.top, rect.width(), rect.height());
	}

	@Override
	public boolean extOnKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_DPAD_DOWN:
			return dispatchKeyEvent(event);
		}
		return false;
	}

	@Override
	public void extOnFocus() {
		this.requestFocus();
	}

	@Override
	public void extOnBlur() {
		this.clearFocus();
	}

	@Override
	public void extShow() {
		this.setVisibility(VISIBLE);
	}

	@Override
	public void extHide() {
		this.setVisibility(GONE);
	}

	@Override
	public void extDestroy() {
	}

	@Override
	public String getAttr(String key, String defVal) {
		return defVal;
	}

	@Override
	public boolean setAttr(String key, String val) {
		return false;
	}

	@Override
	public void extSyncData() {

	}
}
