package com.globalLibrary.util;

import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;

public class AnimUtil {

	 public static Animation shakeAnimation(int counts) {
		   Animation translateAnimation = new TranslateAnimation(0, 10, 0, 0);
		   translateAnimation.setInterpolator(new CycleInterpolator(counts));
		   translateAnimation.setDuration(500);
		   return translateAnimation;
	   }
	   public static Animation shakeUpAndDownAnimation(int counts) {
		   Animation translateAnimation = new TranslateAnimation(0, 0, 0, 10);
		   translateAnimation.setInterpolator(new CycleInterpolator(counts));
		   translateAnimation.setDuration(500);
		   return translateAnimation;
	   }
}
