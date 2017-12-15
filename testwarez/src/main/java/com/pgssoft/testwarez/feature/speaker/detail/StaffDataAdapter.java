package com.pgssoft.testwarez.feature.speaker.detail;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobsandgeeks.adapters.Sectionizer;
import com.mobsandgeeks.adapters.SimpleSectionAdapter;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.feature.speaker.list.SpeakerAgendaListAdapter;
import com.pgssoft.testwarez.database.model.Event;
import com.pgssoft.testwarez.database.model.Speaker;
import com.pgssoft.testwarez.database.model.Staff;
import com.pgssoft.testwarez.database.model.StaffDescription;
import com.pgssoft.testwarez.util.ReadMoreLayoutListener;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by brosol on 2016-03-25.
 */
public class StaffDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Staff staff;
    private Context context;
    private Point screenSize;
    private boolean isArchive;

    private CompositeSubscription compositeSubscription;

    public StaffDataAdapter(Context context, Staff staff, Point screenSize, boolean isArchive) {
        this.context = context;
        this.staff = staff;
        this.screenSize = screenSize;
        this.isArchive = isArchive;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v;
        switch (getItemViewType(i)) {
            case 0:
                v = inflater.inflate(R.layout.rv_speaker_company, viewGroup, false);
                return new CompanyViewHolder(v);
            case 1:
                v = inflater.inflate(R.layout.rv_speaker_description, viewGroup, false);
                return new DescriptionViewHolder(v);
            case 2:
                v = inflater.inflate(R.layout.rv_speaker_agenda, viewGroup, false);
                return new AgendaViewHolder(v);
            case 3:
                v = inflater.inflate(R.layout.rv_speaker_contact, viewGroup, false);
                return new ContactViewHolder(v);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder s, int i) {
        switch (getItemViewType(i)) {
            case 0:
                CompanyViewHolder covh = (CompanyViewHolder) s;

                ViewGroup.LayoutParams layoutParams = covh.company.getLayoutParams();
                layoutParams.height = 1;

                StaffDescription staffDescription = staff.getDescription();
                if (staffDescription != null) {
                    boolean isJobNotEmpty = staffDescription.getJob() != null && !staffDescription.getJob().isEmpty();
                    boolean isCompanyNotEmpty = staffDescription.getCompany() != null && !staffDescription.getCompany().isEmpty();

                    if (isJobNotEmpty || isCompanyNotEmpty) {
                        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        covh.company.setLayoutParams(layoutParams);
                    } else {
                        covh.company.setVisibility(View.GONE);
                    }

                    if (isJobNotEmpty) {
                        covh.company.setText(Html.fromHtml(staffDescription.getJob()) + ", ");
                    }
                    if (isCompanyNotEmpty) {
                        covh.company.append(staffDescription.getCompany());
                    }
                } else {
                    covh.company.setLayoutParams(layoutParams);
                    covh.company.setVisibility(View.GONE);
                }

                return;

            case 1:
                DescriptionViewHolder dvh = (DescriptionViewHolder) s;

                ViewGroup.LayoutParams params = dvh.descriptionContainer.getLayoutParams();
                params.height = 1;

                StaffDescription staffDescription1 = staff.getDescription();
                if(staffDescription1 != null && staffDescription1.getBiography() != null) {
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    dvh.description.setText(Html.fromHtml(staffDescription1.getBiography()));
                    dvh.description.setMovementMethod(LinkMovementMethod.getInstance());
                    dvh.description.setLinksClickable(true);

                    dvh.description.getViewTreeObserver().addOnGlobalLayoutListener(new ReadMoreLayoutListener(context, dvh.readMore, dvh.readMoreIcon, dvh.readMoreText, dvh.description, screenSize));

                    dvh.readMoreIcon.setColorFilter(new PorterDuffColorFilter(context.getResources().getColor(R.color.primary), PorterDuff.Mode.MULTIPLY));
                } else {
                    dvh.descriptionContainer.setLayoutParams(params);
                    dvh.descriptionContainer.setVisibility(View.GONE);
                }

                return;

            case 2:
                final AgendaViewHolder avh = (AgendaViewHolder) s;
                // get all speaker's events
                ArrayList<Event> allSpeakersEvents = staff.getActiveEvents();

                if (allSpeakersEvents.size() > 0) {
                    layoutParams = avh.container.getLayoutParams();
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    avh.container.setLayoutParams(layoutParams);

                    ArrayList<ArrayList<Speaker>> speakers = new ArrayList<>();

                    // sort events by date
                    ArrayList<Event> sortedEvents = new ArrayList<>(allSpeakersEvents);
                    Collections.sort(sortedEvents, (ev1, ev2) -> {
                        if (ev1.getStartAt().isAfter(ev2.getStartAt())) {
                            return 1;
                        } else if (ev1.getStartAt().isBefore(ev2.getStartAt())) {
                            return -1;
                        } else {
                            return 0;
                        }
                    });

                    // for each event get its speakers
                    compositeSubscription.add(Observable.from(sortedEvents)
                            .observeOn(Schedulers.newThread())
                            .flatMap(event1 -> {
                                try {
                                    return Observable.just(ApplicationController.getDatabaseHelper().getEventDao().queryForId(event1.getId()));
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }

                                return Observable.empty();
                            }).flatMap(event2 -> Observable.just(event2.getAllSpeakers()))
                            .doOnNext(speakers2 -> speakers.add(speakers2))
                            .toList()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(speakers1 -> {

                                SpeakerAgendaListAdapter adapter = new SpeakerAgendaListAdapter(context, sortedEvents, speakers, false);
                                adapter.setCompositeSubscription(compositeSubscription);
                                SimpleSectionAdapter<Event> ad = new SimpleSectionAdapter<>(context, adapter, R.layout.event_list_simple_day_header, R.id.day, new ListSectionizer());

                                avh.eventsList.removeAllViews();
                                for (int j = 0; j < ad.getCount(); j++) {
                                    avh.eventsList.addView(ad.getView(j, null, avh.eventsList));
                                }
                            }, Throwable::printStackTrace)
                    );

                } else {
                    layoutParams = avh.container.getLayoutParams();
                    layoutParams.height = 1;
                    avh.container.setLayoutParams(layoutParams);
                }

                return;
            case 3:
                final ContactViewHolder cnvh = (ContactViewHolder) s;
                if (staff.getSkype() == null && staff.getEmail() == null && staff.getPhone() == null && staff.getFacebook() == null && staff.getTwitter() == null && staff.getLinkedin() == null) {
                    layoutParams = cnvh.container.getLayoutParams();
                    layoutParams.height = 1;
                    cnvh.container.setLayoutParams(layoutParams);
                } else {
                    layoutParams = cnvh.container.getLayoutParams();
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    cnvh.container.setLayoutParams(layoutParams);

                    cnvh.skypeContainer.setVisibility(staff.getSkype() != null ? View.VISIBLE : View.GONE);
                    cnvh.emailContainer.setVisibility(staff.getEmail() != null ? View.VISIBLE : View.GONE);
                    cnvh.fonContainer.setVisibility(staff.getPhone() != null ? View.VISIBLE : View.GONE);
                    cnvh.facebookContainer.setVisibility(staff.getFacebook() != null ? View.VISIBLE : View.GONE);
                    cnvh.twitterContainer.setVisibility(staff.getTwitter() != null ? View.VISIBLE : View.GONE);
                    cnvh.linkedinContainer.setVisibility(staff.getLinkedin() != null ? View.VISIBLE : View.GONE);

                    cnvh.skype.setText(staff.getSkype());
                    cnvh.email.setText(staff.getEmail());
                    cnvh.fon.setText(staff.getPhone());
                    cnvh.facebook.setText(staff.getFacebook());
                    cnvh.twitter.setText(staff.getTwitter());
                    cnvh.linkedin.setText(staff.getLinkedin());
                }

                cnvh.fonContainer.setOnClickListener(v -> {
                    Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + staff.getPhone()));
                    context.startActivity(callIntent);
                });
                cnvh.emailContainer.setOnClickListener(v -> {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", staff.getEmail(), null));
                    context.startActivity(Intent.createChooser(emailIntent, context.getResources().getString(R.string.chooser_email)));
                });
                cnvh.twitterContainer.setOnClickListener(v -> {
                    try {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + staff.getTwitter())));
                    } catch (Exception e) {
                        Intent i1 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/#!/" + staff.getTwitter()));
                        context.startActivity(Intent.createChooser(i1, context.getResources().getString(R.string.chooser_twitter)));
                    }
                });
                cnvh.facebookContainer.setOnClickListener(v -> {
                    Intent i1 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/" + staff.getFacebook()));
                    context.startActivity(Intent.createChooser(i1, context.getResources().getString(R.string.chooser_facebook)));
                });
                cnvh.linkedinContainer.setOnClickListener(v -> {
                    Intent i1 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/" + staff.getLinkedin()));
                    context.startActivity(Intent.createChooser(i1, context.getResources().getString(R.string.chooser_linkedin)));
                });
                cnvh.skypeContainer.setOnClickListener(v -> {
                    try {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("skype:" + staff.getSkype())));
                    } catch (Exception e) {
                        Toast.makeText(context, R.string.chooser_skype_error, Toast.LENGTH_SHORT).show();
                    }
                });

                break;
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        compositeSubscription.clear();
        compositeSubscription.unsubscribe();
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 2 && isArchive) {
            return 3;
        }
        return position;
    }

    @Override
    public int getItemCount() {
        if(isArchive) {
            return 3;
        }
        return 4;
    }

    public void setCompositeSubscription(CompositeSubscription compositeSubscription) {
        this.compositeSubscription = compositeSubscription;
    }

    public class CompanyViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.company)
        public TextView company;

        public CompanyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public class DescriptionViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.description_container)
        protected RelativeLayout descriptionContainer;
        @Bind(R.id.description)
        protected TextView description;
        @Bind(R.id.read_more)
        protected View readMore;
        @Bind(R.id.read_more_icon)
        protected ImageView readMoreIcon;
        @Bind(R.id.read_more_text)
        protected TextView readMoreText;

        public DescriptionViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.findViewById(R.id.speakerDescriptionHelpView).setOnClickListener(v -> {});
        }
    }

    public class AgendaViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.agenda_container)
        protected View container;
        @Bind(R.id.events_list)
        protected LinearLayout eventsList;

        public AgendaViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.contact_container)
        protected View container;
        @Bind(R.id.skype_container)
        protected View skypeContainer;
        @Bind(R.id.email_container)
        protected View emailContainer;
        @Bind(R.id.fon_container)
        protected View fonContainer;
        @Bind(R.id.facebook_container)
        protected View facebookContainer;
        @Bind(R.id.twitter_container)
        protected View twitterContainer;
        @Bind(R.id.linkedin_container)
        protected View linkedinContainer;
        @Bind(R.id.skype)
        protected TextView skype;
        @Bind(R.id.email)
        protected TextView email;
        @Bind(R.id.fon)
        protected TextView fon;
        @Bind(R.id.facebook)
        protected TextView facebook;
        @Bind(R.id.twitter)
        protected TextView twitter;
        @Bind(R.id.linkedin)
        protected TextView linkedin;

        public ContactViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    class ListSectionizer implements Sectionizer<Event> {
        @Override
        public String getSectionTitleForItem(Event item) {
            return item.getStartAt().toString("EEEE d MMMM", context.getResources().getConfiguration().locale).toUpperCase();
        }
    }
}
