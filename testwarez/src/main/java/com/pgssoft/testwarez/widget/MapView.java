package com.pgssoft.testwarez.widget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.database.model.Conference;

import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by rtulaza on 2015-08-05.
 */
public class MapView extends FrameLayout {

    @Bind(R.id.navigate)
    protected FloatingActionButton navigateFab;

    private SupportMapFragment map;
    private LatLng hotelMarker = new LatLng(0, 0);

    public MapView(Context context) {
        super(context);
        init();
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_map, this, true);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.navigate)
    public void onNavigateFabButtonClick() {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(
                String.format(Locale.US, "http://maps.google.com/maps?daddr=%f,%f", hotelMarker.latitude, hotelMarker.longitude))
        );
        getContext().startActivity(intent);
    }

    public void updateView(ProgressBar progressBar) {
        final Conference conf = ApplicationController.getActiveConference();
        hotelMarker = new LatLng(conf.getMapLatitude(), conf.getMapLongitude());
        map.getMapAsync(googleMap -> {
            progressBar.setVisibility(View.GONE);
            navigateFab.setVisibility(View.VISIBLE);
            googleMap.setInfoWindowAdapter(new CustomWindowInfo(conf));
            googleMap.addMarker(new MarkerOptions().position(hotelMarker)).showInfoWindow();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hotelMarker, 12));
        });

    }

    public void setUp(FragmentManager fragmentManager) {
        map = SupportMapFragment.newInstance(new GoogleMapOptions().mapToolbarEnabled(false));
        fragmentManager.beginTransaction().replace(R.id.map, map).commit();
    }

    private class CustomWindowInfo implements GoogleMap.InfoWindowAdapter {
        private Conference conference;

        public CustomWindowInfo(Conference conference) {
            this.conference = conference;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.map_info_window, null);

            ((TextView) view.findViewById(R.id.title)).setText(Html.fromHtml(conference.getMapDescription().replace("\\n", "\n")));

            return view;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return getInfoWindow(marker);
        }
    }
}
