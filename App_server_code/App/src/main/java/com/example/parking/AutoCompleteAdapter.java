package com.example.parking;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteAdapter  extends ArrayAdapter<SearchActivity.searchItem> {

    //데이터를 넣을 리스트
    private List<SearchActivity.searchItem> searchListFull;

    public AutoCompleteAdapter(@NonNull Context context, @NonNull List<SearchActivity.searchItem> searchList) {
        super(context, 0, searchList);
        searchListFull = new ArrayList<>(searchList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.country_autocomplete_row, parent, false
            );
        }

        TextView textView = convertView.findViewById(R.id.text_view_name);
        ImageView imageView = convertView.findViewById(R.id.image_view_flag);

        //getItem(position) 코드로 자동완성 될 아이템을 가져온다
        SearchActivity.searchItem searchItem = getItem(position);

        if (searchItem != null) {
            textView.setText(searchItem.getname());
            imageView.setImageResource(searchItem.getimage());
        }
        return convertView;
    }

    //-------------------------- 이 아래는 자동완성을 위한 검색어를 찾아주는 코드이다 --------------------------
    @NonNull
    @Override
    public Filter getFilter() {
        return countryFilter;
    }

    private Filter countryFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();
            List<SearchActivity.searchItem> suggestions = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                suggestions.addAll(searchListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (SearchActivity.searchItem item : searchListFull) {
                    if (item.getname().toLowerCase().contains(filterPattern)) {
                        suggestions.add(item);
                    }
                }
            }
            results.values = suggestions;
            results.count = suggestions.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((List) results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((SearchActivity.searchItem) resultValue).getname();
        }
    };
}
