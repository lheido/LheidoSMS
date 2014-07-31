package com.lheidosms.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;

public class AutoCompleteAdapter extends ArrayAdapter<LheidoContact> {
    private Context context;
    private int ressource;
    private ArrayList<LheidoContact> items;
    private ArrayList<LheidoContact> suggestions;

    public AutoCompleteAdapter(Context context, int ressource, ArrayList<LheidoContact> contactsList) {
        super(context, ressource, contactsList);
        this.context = context;
        this.ressource = ressource;
        this.items = contactsList;
        suggestions = new ArrayList<LheidoContact>();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(ressource, null);
        }
        LheidoContact contact = items.get(position);
        if (contact != null) {
            TextView contactNameLabel = (TextView) v.findViewById(R.id.contact_name);
            TextView contactPhoneLabel = (TextView) v.findViewById(R.id.contact_phone);
            if (contactNameLabel != null) {
//              Log.i(MY_DEBUG_TAG, "getView Customer Name:"+customer.getName());
                contactNameLabel.setText(contact.getName());
            }
            if (contactPhoneLabel != null) {
//              Log.i(MY_DEBUG_TAG, "getView Customer Name:"+customer.getName());
                contactPhoneLabel.setText(contact.getPhone());
            }
        }
        return v;
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    Filter nameFilter = new Filter() {
        @Override
        public String convertResultToString(Object resultValue) {
            String str = ((LheidoContact)(resultValue)).getName();
            return str;
        }
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if(constraint != null) {
                suggestions.clear();
                for (LheidoContact contact : items) {
                    if(contact.getName().toLowerCase().contains(constraint.toString().toLowerCase()) ||
                       contact.getPhone().toLowerCase().contains(constraint.toString().toLowerCase())){
                        suggestions.add(contact);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }
        @Override
        protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
            ArrayList<LheidoContact> filteredList = (ArrayList<LheidoContact>) results.values;
            ArrayList<LheidoContact> cList=new ArrayList<LheidoContact>();
            if (results != null && results.count > 0) {
                clear();
                for (LheidoContact c : filteredList) {
                    cList.add(c);
                }
                for (LheidoContact customerIterator : cList) {
                    add(customerIterator);
                }
                notifyDataSetChanged();
            }

        }
    };
}
