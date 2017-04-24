package ar.unsta.robotteam.network.video;

import android.graphics.Bitmap;

public interface VideoEventListener {

	public void onImageReceived(Bitmap p_image);

}