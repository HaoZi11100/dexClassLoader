package com.connetly.puzzle.game;
import android.content.Intent;
import android.net.Uri;
import android.content.Context;
public class Evaluate  {	
    public static void evaluate(Context ctx) {
		String mAddress = "market://details?id=" + ctx.getPackageName();
		Intent marketIntent = new Intent("android.intent.action.VIEW");
		marketIntent.setData(Uri.parse(mAddress));
		ctx.startActivity(marketIntent);
    }
}