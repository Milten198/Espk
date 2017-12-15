package com.pgssoft.testwarez.feature.messages.filter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.pgssoft.testwarez.R;
import com.pgssoft.testwarez.core.BaseFilterAdapter;
import com.pgssoft.testwarez.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dpodolak on 08.04.16.
 */
public class MessageFilterAdapter extends BaseFilterAdapter {

    private List<Object> socialList = new ArrayList<>();
    private List<Integer> selectedList = new ArrayList<>();
    private Context context;

    public static final int FACEBOOK_ID = 0;
    public static final int TWITTER_ID = 1;
    public static final int ORGANIZER_ID = 2;

    public MessageFilterAdapter(Context context) {
        super(context);
        this.context = context;
        socialList.add(new Title("Media", Type.SOCIAL));

        loadSocial();


    }

    private void loadSocial() {
        Filter facebook = new Filter("Facebook", Type.SOCIAL, FACEBOOK_ID);
        facebook.medium = "F";
        socialList.add(facebook);

        Filter twitter = new Filter("Twitter", Type.SOCIAL, TWITTER_ID);
        twitter.medium = "T";
        socialList.add(twitter);

        Filter organizer = new Filter("Organizator", Type.SOCIAL, ORGANIZER_ID);
        organizer.medium = "O";
        socialList.add(organizer);


        selectedList = Utils.getSocialFilter();

        if (!selectedList.isEmpty()) {
            for (Filter f : getSocialFilters()) {
                if (selectedList.contains(f.id)) {
                    f.isSelected = true;
                } else {
                    f.isSelected = false;
                }
            }
        }
        checkAllItems();

    }

    @Override
    public void removeFilter() {
        selectAllItems();
    }

    @Override
    public boolean isFilterOn() {
        return false;
    }

    @Override
    public void saveFilter() {
        if (selectedList.isEmpty() || selectedList.size() == (socialList.size()-1)){
            Utils.clearSocialFilter();
        }else {
            Utils.saveSocialFilter((ArrayList<Integer>) selectedList);
        }
    }

    @Override
    protected Object getItem(int position) {
        return socialList.get(position);
    }

    @Override
    public void afterBind(RecyclerView.ViewHolder holder, Object item, int position) {
        if (holder instanceof TitleViewHolder){
            Title title = (Title) item;
            holder.itemView.setOnClickListener(v -> {
                if (!title.isSelected) {
                    title.isSelected = !title.isSelected;
                    ((TitleViewHolder) holder).marker.setVisibility(title.isSelected ? View.VISIBLE : View.GONE);
                    selectAllItems();
                }
            });
        }else if(holder instanceof FilterSocialHolder){
            Filter filter = (Filter) item;
            FilterSocialHolder fsh = (FilterSocialHolder) holder;
            holder.itemView.setOnClickListener(v -> {
                if (selectedList.size() >1 || !filter.isSelected) {
                    filter.isSelected = !filter.isSelected;
                    fsh.setChecked(filter.isSelected);
                    checkAllItems();
                }else{
                    Toast.makeText(context, R.string.atleast_one_item_should_be_selected, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void selectAllItems() {
        for (Object o: socialList){
            if (o instanceof Filter){
                ((Filter) o).isSelected = true;
            }else{
                ((Title) o).isSelected = true;
            }
        }

        checkAllItems();
        notifyDataSetChanged();
    }

    private List<Filter> getSocialFilters(){
        List<Filter> filters = new ArrayList<>();

        for (Object o: socialList){
            if (o  instanceof  Filter){
                filters.add((Filter) o);
            }
        }

        return filters;
    }

    private void checkAllItems(){
        boolean allSelected = true;

        selectedList.clear();
        for (Filter f: getSocialFilters()){
            if (f.isSelected) {
                selectedList.add(f.id);
            }else{
                allSelected = false;
            }
        }

        ((Title) getItem(0)).isSelected = allSelected;

        notifyDataSetChanged();

    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) instanceof Title){
            return TITLE_HOLDER;
        }else{
            return FILTER_SOCIAL_HOLDER;
        }
    }

    @Override
    public int getItemCount() {
        return socialList.size();
    }
}
