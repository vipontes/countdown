package br.net.easify.countdown;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import static android.os.VibrationEffect.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private long timeCountInMilliSeconds = 60000;
    private long alertInSeconds = 0;
    private boolean alertSent = false;

    private enum TimerStatus {
        STARTED,
        STOPPED
    }

    private TimerStatus timerStatus = TimerStatus.STOPPED;

    private ProgressBar progressBarCircle;
    private EditText editTextMinute;
    private EditText editTextAlert;
    private TextView textViewTime;
    private ImageView imageViewReset;
    private ImageView imageViewStartStop;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initViews();
        initListeners();
    }

    private void initViews() {
        progressBarCircle = findViewById(R.id.progressBarCircle);
        editTextMinute = findViewById(R.id.editTextMinute);
        editTextAlert = findViewById(R.id.editTextAlert);
        textViewTime = findViewById(R.id.textViewTime);
        imageViewReset = findViewById(R.id.imageViewReset);
        imageViewStartStop = findViewById(R.id.imageViewStartStop);
    }

    private void initListeners() {
        imageViewReset.setOnClickListener(this);
        imageViewStartStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageViewReset:
                reset();
                break;
            case R.id.imageViewStartStop:
                startStop();
                break;
        }
    }

    private void reset() {
        stopCountDownTimer();
        startCountDownTimer();
    }

    private void startStop() {
        if (timerStatus == TimerStatus.STOPPED) {
            setTimerValues();
            setProgressBarValues();
            imageViewReset.setVisibility(View.VISIBLE);
            editTextAlert.setVisibility(View.GONE);
            imageViewStartStop.setImageResource(R.drawable.icon_stop);
            editTextMinute.setEnabled(false);
            timerStatus = TimerStatus.STARTED;
            startCountDownTimer();
        } else {
            imageViewReset.setVisibility(View.GONE);
            editTextAlert.setVisibility(View.VISIBLE);
            imageViewStartStop.setImageResource(R.drawable.icon_start);
            editTextMinute.setEnabled(true);
            timerStatus = TimerStatus.STOPPED;
            stopCountDownTimer();
        }

    }

    private void setTimerValues() {
        int time = 0;
        alertSent = false;
        if (!editTextMinute.getText().toString().isEmpty()) {
            time = Integer.parseInt(editTextMinute.getText().toString().trim());
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.message_minutes), Toast.LENGTH_LONG).show();
        }

        if (!editTextAlert.getText().toString().isEmpty()) {
            alertInSeconds = Integer.parseInt(editTextAlert.getText().toString().trim());
            alertInSeconds *= 60;
        } else {
            alertInSeconds = 0;
        }

        timeCountInMilliSeconds = time * 60 * 1000;
    }

    private void startCountDownTimer() {

        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textViewTime.setText(hmsTimeFormatter(millisUntilFinished));
                progressBarCircle.setProgress((int) (millisUntilFinished / 1000));

                if ( alertInSeconds > 0 ) {
                    int timeToFinish = (int) millisUntilFinished / 1000;
                    if (timeToFinish == alertInSeconds && !alertSent) {
                        alertSent = true;
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            v.vibrate(createOneShot(500, DEFAULT_AMPLITUDE));
                        } else {
                            v.vibrate(500);
                        }
                    }
                }
            }

            @Override
            public void onFinish() {
                try {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                textViewTime.setText(hmsTimeFormatter(timeCountInMilliSeconds));
                setProgressBarValues();
                imageViewReset.setVisibility(View.GONE);
                imageViewStartStop.setImageResource(R.drawable.icon_start);
                editTextMinute.setEnabled(true);
                timerStatus = TimerStatus.STOPPED;
            }

        }.start();
        countDownTimer.start();
    }

    private void stopCountDownTimer() {
        countDownTimer.cancel();
    }

    private void setProgressBarValues() {

        progressBarCircle.setMax((int) timeCountInMilliSeconds / 1000);
        progressBarCircle.setProgress((int) timeCountInMilliSeconds / 1000);
    }

    private String hmsTimeFormatter(long milliSeconds) {

        @SuppressLint("DefaultLocale")
        String hms = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(milliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));

        return hms;
    }
}
