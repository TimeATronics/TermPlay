package timeatronics.termplay;

import android.os.Bundle;
import android.os.AsyncTask;
import android.os.Handler;
import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import java.net.URL;
import java.net.URI;
import java.net.URLDecoder;
import java.io.IOException;

public class MainActivity extends Activity implements View.OnClickListener,
	View.OnTouchListener, MediaPlayer.OnCompletionListener,
	MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnInfoListener  {

	Button play ,pause;
	SeekBar seekBar;
	
	Player player;
	int flag;
	ProgressDialog progressDialog;
	
	private MediaPlayer mediaPlayer;
	private int lengthOfAudio;
	private final Handler handler = new Handler();
	private final Runnable r = new Runnable() {
		@Override
		public void run() {
			updateSeekProgress();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		progressDialog=new ProgressDialog(this);

		flag=0;

		progressDialog.setMessage("Loading...");
		progressDialog.setCancelable(false);

		mediaPlayer=new MediaPlayer();

		play=(Button)findViewById(R.id.play);
		pause=(Button)findViewById(R.id.pause);
		seekBar=(SeekBar)findViewById(R.id.seekbar);

		play.setOnClickListener(this);
		pause.setOnClickListener(this);
		seekBar.setOnTouchListener(this);

		mediaPlayer.setOnBufferingUpdateListener(this);
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnInfoListener(this);
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
		seekBar.setSecondaryProgress(i);
		System.out.println(i);
	}

	@Override
	public void onCompletion(MediaPlayer mediaPlayer) {
		seekBar.setProgress(0);
		Toast.makeText(MainActivity.this, "Playback ended...", Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {

		switch (what) {

			case MediaPlayer.MEDIA_INFO_BUFFERING_START:
				progressDialog.show();
				break;

			case MediaPlayer.MEDIA_INFO_BUFFERING_END:
				progressDialog.dismiss();
				break;
		} return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

			case R.id.play:
				if (flag==0) {

					EditText text = (EditText)findViewById(R.id.input);
					String urltext = text.getText().toString();

					try {
						String urlString2Decode = urltext;
						String decodedURL = URLDecoder.decode(urlString2Decode, "UTF-8");

						URL url1 = new URL(decodedURL);
						URI uri = new URI(url1.getProtocol(), url1.getUserInfo(), url1.getHost(), url1.getPort(), url1.getPath(), url1.getQuery(), url1.getRef());
						String url = uri.toASCIIString();

						player=new Player();
						player.execute(url);

					} catch (Exception e) {
						;
					}
				} else {
					if (mediaPlayer!=null){
						playAudio();
					}
				} break;

			case R.id.pause:
				pauseAudio();
				break;

			default:
				break;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent motionEvent) {

		if (mediaPlayer.isPlaying()) {
			SeekBar tmpSeekBar = (SeekBar)v;
			mediaPlayer.seekTo((lengthOfAudio / 100) * tmpSeekBar.getProgress() );
		} else {
			SeekBar tmpSeekBar = (SeekBar)v;
			mediaPlayer.seekTo((lengthOfAudio / 100) * tmpSeekBar.getProgress() );
		} return false;
	}

	private void updateSeekProgress() {
		if (mediaPlayer != null) {
			if (mediaPlayer.isPlaying()) {
				seekBar.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / lengthOfAudio) * 100));
				handler.postDelayed(r, 1000);
			}
		}
	}

	class Player extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			Boolean prepared;
			try {
				mediaPlayer.setDataSource(params[0]);
				mediaPlayer.prepare();
				lengthOfAudio = mediaPlayer.getDuration();
				prepared = true;
			} catch (IllegalArgumentException e) {
				Log.d("IllegalArgument", e.getMessage());
				prepared = false;
				e.printStackTrace();
			} catch (SecurityException e) {
				prepared = false;
				e.printStackTrace();
			} catch (IllegalStateException e) {
				prepared = false;
				e.printStackTrace();
			} catch (IOException e) {
				prepared = false;
				e.printStackTrace();
			} return prepared;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}

		@Override
		protected void onPostExecute(Boolean aBoolean) {
			super.onPostExecute(aBoolean);
			progressDialog.dismiss();

			if (aBoolean) {
				flag=1;
			} else {
				flag=0;
			} playAudio();
		}
	}

	private void playAudio() {
		if(mediaPlayer!=null) {
			mediaPlayer.start();
			updateSeekProgress();
		}
	}

	private void pauseAudio() {
		if(mediaPlayer!=null) {
			mediaPlayer.pause();
		}
	}
}