package com.pgssoft.testwarez.feature.agenda.filter;

import com.pgssoft.testwarez.ApplicationController;
import com.pgssoft.testwarez.feature.agenda.all.AgendaAdapter;
import com.pgssoft.testwarez.database.model.Category;
import com.pgssoft.testwarez.database.model.Track;
import com.pgssoft.testwarez.util.Utils;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

public class UserFilter implements Func1<AgendaAdapter.ListItem, Boolean> {

        List<Integer> dayList;
        List<Integer> trackFilterList = Utils.getAgendaTrackFilter();

        public UserFilter(CompositeSubscription compositeSubscription, Observable<Category> categoryObservable) {
            List<Integer> filterCategoryIdList = Utils.getAgendaDayFilter();

            compositeSubscription.add(categoryObservable
                    .doOnError(Throwable::printStackTrace)
                    .filter(category -> filterCategoryIdList.contains(category.getId()))
                    .map(category1 -> category1.getStartAt().getDayOfYear())
                    .toList()
                    .subscribe(dayList -> this.dayList = dayList));
        }

        @Override
        public Boolean call(AgendaAdapter.ListItem listItem) {
            boolean dayFilter = true;
            boolean trackFilter = true;


            if (listItem.getEvent() != null) {
                if (dayList != null && !dayList.isEmpty()) {
                    int day = listItem.getEvent().getStartAt().getDayOfYear();
                    dayFilter = dayList.contains(day);
                }

                if (!trackFilterList.isEmpty()) {
                    List<Track> tracks = listItem.getEvent().getTrackList();
                    trackFilter = false;
                    for (Track t : tracks) {
                        if (trackFilterList.contains(new Integer(t.getId()))) {
                            trackFilter = true;
                            break;
                        }
                    }
                }
            } else if (dayList != null && !dayList.isEmpty() && listItem.getCategory() != null) {
                int day = listItem.getCategory().getStartAt().getDayOfYear();
                dayFilter = dayList.contains(day);
            } else if (dayList != null && !dayList.isEmpty() && listItem.getDay() != null) {
                int day = Integer.valueOf(listItem.getDay().split(" ")[0]);
                day = ApplicationController.getActiveConference().getStartAt().getDayOfYear() + day - 1;
                dayFilter = dayList.contains(day);
            }

            return dayFilter & (trackFilter || (listItem.getEvent().isTechnical() && listItem.getEvent().getTracks() == null));
        }
    }