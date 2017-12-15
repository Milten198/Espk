package com.pgssoft.testwarez.feature.event;

import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.f2prateek.rx.android.schedulers.AndroidSchedulers;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.BaseActivity;
import com.pgssoft.testwarez.database.model.Event;
import com.pgssoft.testwarez.database.model.Rate;
import com.pgssoft.testwarez.util.FinishWithoutAnim;
import com.pgssoft.testwarez.util.Observables;
import com.pgssoft.testwarez.util.Utils;
import com.pgssoft.testwarez.widget.StarRatingBar;

import java.sql.SQLException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by brosol on 2017-07-05.
 */

public class EventRateActivity extends BaseActivity implements FinishWithoutAnim {

    private static final long SUCCESS_LAYOUT_DURATION_TIME = 2500;

    @Bind(R.id.toolbar)
    protected Toolbar toolbar;
    @Bind(R.id.event_rate_container)
    protected FrameLayout eventRateContainer;
    @Bind(R.id.rating_bar)
    protected StarRatingBar ratingBar;
    @Bind(R.id.rate_none_icon)
    protected ImageView rateNoneIcon;
    @Bind(R.id.rate_star_message_container)
    protected RelativeLayout rateStarMessageContainer;
    @Bind(R.id.rate_star_icon)
    protected ImageView rateStarIcon;
    @Bind(R.id.rate_star_message)
    protected TextView rateStarMessage;
    @Bind(R.id.message_edit_text)
    protected EditText messageEditText;
    @Bind(R.id.rate_successful_layout)
    protected RelativeLayout rateSuccessfulLayout;
    @Bind(R.id.button_rate_send)
    protected Button rateButton;
    @Bind(R.id.sending_rate_text)
    protected TextView sendingRateText;

    private long successLayoutTimestamp;
    private int eventId;
    private boolean isRatingInProgress;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private Runnable successRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_rate);
        ButterKnife.bind(this);

        eventId = getIntent().getIntExtra("eventId", -1);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setRatingBarStarsColors();

        ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {
            switch ((int) rating) {
                case 0:
                    rateNoneIcon.setVisibility(View.VISIBLE);
                    rateStarMessageContainer.setVisibility(View.GONE);
                    break;
                case 1:
                    rateNoneIcon.setVisibility(View.GONE);
                    rateStarIcon.setImageResource(R.drawable.ic_rate_1_icon);
                    rateStarMessage.setText(getString(R.string.rate_1));
                    rateStarMessageContainer.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    rateNoneIcon.setVisibility(View.GONE);
                    rateStarIcon.setImageResource(R.drawable.ic_rate_2_icon);
                    rateStarMessage.setText(getString(R.string.rate_2));
                    rateStarMessageContainer.setVisibility(View.VISIBLE);
                    break;
                case 3:
                    rateNoneIcon.setVisibility(View.GONE);
                    rateStarIcon.setImageResource(R.drawable.ic_rate_3_icon);
                    rateStarMessage.setText(getString(R.string.rate_3));
                    rateStarMessageContainer.setVisibility(View.VISIBLE);
                    break;
                case 4:
                    rateNoneIcon.setVisibility(View.GONE);
                    rateStarIcon.setImageResource(R.drawable.ic_rate_4_icon);
                    rateStarMessage.setText(getString(R.string.rate_4));
                    rateStarMessageContainer.setVisibility(View.VISIBLE);
                    break;
                case 5:
                    rateNoneIcon.setVisibility(View.GONE);
                    rateStarIcon.setImageResource(R.drawable.ic_rate_5_icon);
                    rateStarMessage.setText(getString(R.string.rate_5));
                    rateStarMessageContainer.setVisibility(View.VISIBLE);
                    break;
            }
        });

        successRunnable = () -> {
            setResult(RESULT_OK);
            finishWithoutAnimation();
        };
    }

    private void setRatingBarStarsColors() {
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();

        stars.getDrawable(0).setColorFilter(ContextCompat.getColor(this, R.color.star_color_empty), PorterDuff.Mode.MULTIPLY);
        stars.getDrawable(1).setColorFilter(ContextCompat.getColor(this, R.color.star_color_yellow), PorterDuff.Mode.SRC_IN);
        stars.getDrawable(2).setColorFilter(ContextCompat.getColor(this, R.color.star_color_yellow), PorterDuff.Mode.SRC_IN);
    }

    @OnClick(R.id.button_rate_send)
    protected void onRateButtonClick() {
        if (ratingBar.getRating() < 1.0f && eventId != -1) {
            return;
        }

        if (!Utils.isInternetConnection(this)) {
            showNoInternetConnectionSnackbar(findViewById(R.id.event_rate_container), true);
            return;
        }

        isRatingInProgress = true;

        rateButton.setVisibility(View.GONE);
        sendingRateText.setVisibility(View.VISIBLE);

        compositeSubscription.add(ApplicationController.getNetworkInterface().rateEvent(eventId, new Rate((int) ratingBar.getRating(), messageEditText.getText().toString()))
                .doOnError(Throwable::printStackTrace)
                .doOnNext(eventRateResponse -> {
                    try {
                        Event event = ApplicationController.getDatabaseHelper().getEventDao().queryForId(eventId);
                        event.setAverageRating(eventRateResponse.getAverageRate());
                        event.setRated(true);
                        ApplicationController.getDatabaseHelper().getEventDao().update(event);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(eventRateResponse -> showSuccessLayout(), e -> {
                    isRatingInProgress = false;
                    rateButton.setVisibility(View.VISIBLE);
                    sendingRateText.setVisibility(View.GONE);
                    Snackbar.make(eventRateContainer, getString(R.string.rate_fail), Snackbar.LENGTH_LONG).show();
                    Timber.e(e);
                }));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void showSuccessLayout() {
        successLayoutTimestamp = System.currentTimeMillis();

        rateSuccessfulLayout.setVisibility(View.VISIBLE);
        Observables.getInstance().getEventObservables().refreshEventsObservable()
                .doOnError(Throwable::printStackTrace)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    isRatingInProgress = false;

                    long timeLeft;
                    long timeEnd = successLayoutTimestamp + SUCCESS_LAYOUT_DURATION_TIME;

                    if (System.currentTimeMillis() - timeEnd >= 0) {
                        timeLeft = 0;
                    } else {
                        timeLeft = timeEnd - System.currentTimeMillis();
                    }

                    new Handler().postDelayed(successRunnable, timeLeft);

                    rateSuccessfulLayout.setOnClickListener(v -> {
                        setResult(RESULT_OK);
                        finishWithoutAnimation();
                    });
                });
    }

    @Override
    public void onBackPressed() {
        if (isRatingInProgress) {
            return;
        }

        setResult(RESULT_OK);
        finishWithoutAnimation();
        super.onBackPressed();
    }

    @Override
    public void finishWithoutAnimation() {
        super.finish();
    }

    @Override
    protected void onDestroy() {
        compositeSubscription.clear();
        super.onDestroy();
    }
}
