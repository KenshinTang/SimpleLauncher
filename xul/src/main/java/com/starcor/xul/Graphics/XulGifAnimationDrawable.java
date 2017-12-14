package com.starcor.xul.Graphics;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import com.starcor.xul.XulUtils;
import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by hy on 2015/6/11.
 */
public class XulGifAnimationDrawable extends XulAnimationDrawable {
	GifDrawable _drawable;
	AnimationDrawingContext _ctx;
	int _width;
	int _height;


	public static XulGifAnimationDrawable buildAnimation(GifDrawable drawable, int width, int height) {
		XulGifAnimationDrawable xulGif = new XulGifAnimationDrawable();
		xulGif._drawable = drawable;
		if (width <= 0) {
			if (height <= 0) {
				xulGif._width = xulGif._drawable.getIntrinsicWidth();
				xulGif._height = xulGif._drawable.getIntrinsicHeight();
			} else {
				xulGif._width = height * xulGif._drawable.getIntrinsicWidth() / xulGif._drawable.getIntrinsicHeight();
				xulGif._height = height;
			}
		} else if (height <= 0) {
			xulGif._width = width;
			xulGif._height = width * xulGif._drawable.getIntrinsicHeight() / xulGif._drawable.getIntrinsicWidth();
		} else {
			xulGif._width = width;
			xulGif._height = height;
		}
		return xulGif;
	}

	@Override
	public boolean drawAnimation(AnimationDrawingContext ctx, XulDC dc, Rect dst, Paint paint) {
		_drawable.setBounds(dst);
		_drawable.setAlpha(paint.getAlpha());
		_drawable.draw(dc.getCanvas());
		return false;
	}

	@Override
	public boolean drawAnimation(AnimationDrawingContext ctx, XulDC dc, RectF dst, Paint paint) {
		_drawable.setBounds(XulUtils.roundToInt(dst.left), XulUtils.roundToInt(dst.top), XulUtils.roundToInt(dst.right), XulUtils.roundToInt(dst.bottom));
		_drawable.setAlpha(paint.getAlpha());
		_drawable.draw(dc.getCanvas());
		return false;
	}

	@Override
	public AnimationDrawingContext createDrawingCtx() {
		if (_ctx != null) {
			return _ctx;
		}
		_ctx = new AnimationDrawingContext() {
			int _lastFrameIndex = -1;

			@Override
			public boolean updateAnimation(long timestamp) {
				int currentFrameIndex = _drawable.getCurrentFrameIndex();
				if (_lastFrameIndex != currentFrameIndex) {
					return true;
				}
				return false;
			}

			@Override
			public boolean isAnimationFinished() {
				return _drawable.isAnimationCompleted();
			}

			@Override
			public void reset() {
				_drawable.reset();
			}
		};
		return _ctx;
	}

	@Override
	public boolean draw(Canvas canvas, Rect rc, Rect dst, Paint paint) {
		_drawable.setBounds(dst);
		_drawable.setAlpha(paint.getAlpha());
		_drawable.draw(canvas);
		return true;
	}

	@Override
	public boolean draw(Canvas canvas, Rect rc, RectF dst, Paint paint) {
		_drawable.setBounds((int) dst.left, (int) dst.top, (int) dst.right, (int) dst.bottom);
		_drawable.setAlpha(paint.getAlpha());
		_drawable.draw(canvas);
		return true;

	}

	@Override
	public int getHeight() {
		return _height;
	}

	@Override
	public int getWidth() {
		return _width;
	}
}
