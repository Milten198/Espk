package com.pgssoft.testwarez.feature.about;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.mobsandgeeks.adapters.Sectionizer;
import com.mobsandgeeks.adapters.SimpleSectionAdapter;
import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.database.model.companies.CompanyEntity;
import com.pgssoft.testwarez.database.model.companies.CompanyType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.observables.GroupedObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


public class PhotoListFragment extends Fragment {
    private static final String TYPE_EXTRA = "type_extra";

    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private PhotoListAdapter adapter;

    private AboutFragmentPagerAdapter.TAB_TITLE tabType;
    private ListView companyListView;

    public static PhotoListFragment newInstance(AboutFragmentPagerAdapter.TAB_TITLE type) {
        PhotoListFragment fragment = new PhotoListFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE_EXTRA, type.ordinal());
        fragment.setArguments(args);
        return fragment;
    }

    public PhotoListFragment() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeSubscription.clear();
        compositeSubscription.unsubscribe();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            int val = getArguments().getInt(TYPE_EXTRA, -1);
            if (val == -1) {
                tabType = null;
            } else {
                tabType = AboutFragmentPagerAdapter.TAB_TITLE.values()[val];
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout fl = (FrameLayout) inflater.inflate(R.layout.fragment_photo_list, container, false);

        companyListView = ButterKnife.findById(fl, R.id.lvFragmentPhotoList);

        if (tabType == null) {
            return companyListView;
        }

        adapter = new PhotoListAdapter(getActivity());
        updateView();

        return fl;
    }

    boolean allCompaniesHasType = true;


    public void updateView() {
        compositeSubscription.add(
                Observable.defer(() -> Observable.just(ApplicationController.getActiveConference()))
                        .flatMap(conf -> {
                            switch (tabType) {
                                case ORGANIZERS:
                                    return Observable.from(new ArrayList<CompanyEntity>(conf.getAllOrganizers()));
                                case PARTNERS:
                                    return Observable.from(new ArrayList<CompanyEntity>(conf.getAllPartners()));
                                case SPONSORS:
                                    return Observable.from(new ArrayList<CompanyEntity>(conf.getAllSponsors()));
                            }
                            return Observable.empty();
                        }).toSortedList((companyEntity, companyEntity2) -> new Integer(companyEntity.getPosition()).compareTo(companyEntity2.getPosition()))
                        .flatMap(companyEntities1 -> Observable.from(companyEntities1).filter(companyEntity -> companyEntity.getCompanyType() != null)
                                .count()
                                .flatMap(integer -> Observable.just(integer == companyEntities1.size())) //if true, all companies has type
                                .flatMap(companiesHasType -> {
                                    allCompaniesHasType = companiesHasType;
                                    if (companiesHasType) {
                                        return getObservableForCompaniesType(companyEntities1);
                                    } else {
                                        return Observable.just(companyEntities1);
                                    }
                                }))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(companyEntities -> {
                            adapter.setCompanyList(companyEntities);

                            if (allCompaniesHasType) {
                                SimpleSectionAdapter<CompanyEntity> sectionAdaper = new SimpleSectionAdapter<>(getContext(), adapter, R.layout.photo_list_header, R.id.tvFragmentPhotoListItemTitle, new ListSectionizer());
                                companyListView.setAdapter(sectionAdaper);
                            } else {
                                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) companyListView.getLayoutParams();
                                params.setMargins(0, (int) getContext().getResources().getDimension(R.dimen.medium_offset) * 2, 0, 0);
                                companyListView.setLayoutParams(params);
                                companyListView.setAdapter(adapter);
                            }

                        }, Throwable::printStackTrace));
    }

    @NonNull
    private Observable<? extends List<CompanyEntity>> getObservableForCompaniesType(List<CompanyEntity> companyEntities1) {
        Map<CompanyType, List<CompanyEntity>> groupedValues = new HashMap<>();
        return Observable.from(companyEntities1)
                .groupBy(CompanyEntity::getCompanyType)
                .flatMap(companyTypeCompanyEntityGroupedObservable2 -> GroupedObservable.from(companyTypeCompanyEntityGroupedObservable2.getKey(), companyTypeCompanyEntityGroupedObservable2)
                        .toList()
                        .doOnNext(companyEntities2 -> groupedValues.put((CompanyType) companyTypeCompanyEntityGroupedObservable2.getKey(), companyEntities2)))
                .toList()
                .flatMap(lists -> Observable.from(groupedValues.keySet().toArray(new CompanyType[groupedValues.size()]))
                        .toSortedList((companyType, companyType2) -> new Integer(companyType.getPosition()).compareTo(companyType2.getPosition()))
                        .flatMap(Observable::from)
                        .flatMap(companyType1 -> Observable.from(groupedValues.get(companyType1))))
                .toList();
    }

    class ListSectionizer implements Sectionizer<CompanyEntity> {
        @Override
        public String getSectionTitleForItem(CompanyEntity item) {
            return item.getCompanyType().getTitle();
        }
    }
}
