package com.example.finalproject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BillAdapter_billpage extends RecyclerView.Adapter<BillAdapter_billpage.BillViewHolder> implements Filterable {

    // Define an interface for click listener
    public interface OnItemClickListener {
        void onItemClick(Bill_model_billpage bill);
    }

    private List<Bill_model_billpage> billList;
    private List<Bill_model_billpage> allBillList;
    private Context context;
    private OnItemClickListener listener; // Add listener variable

    // Add listener parameter to the constructor
    public BillAdapter_billpage(List<Bill_model_billpage> billList, Context context, OnItemClickListener listener) {
        this.billList = billList;
        this.context = context;
        this.listener = listener; // Assign the listener
        this.allBillList = new ArrayList<>(billList);
    }

    @NonNull
    @Override
    public BillAdapter_billpage.BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bill_pagelist, parent, false);
        return new BillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        Bill_model_billpage bill = billList.get(position);

        holder.billName.setText(bill.getBillName());
        holder.billCategory.setText(bill.getCategory());
        holder.billAmount.setText(String.valueOf(bill.getAmount()));
        holder.billDueDate.setText("Due: " + (bill.getDueDate() != null ? bill.getDueDate() : "None"));

        // Set status label with color
        if (bill.getStatus().equalsIgnoreCase("paid")) {
            holder.statusLabel.setText("Paid");
            holder.statusLabel.setTextColor(ContextCompat.getColor(context, R.color.paidColor));
        } else if (bill.getStatus().equalsIgnoreCase("unsettled")) {
            holder.statusLabel.setText("Unsettled");
            holder.statusLabel.setTextColor(ContextCompat.getColor(context, R.color.unsettledColor));
        } else {
            holder.statusLabel.setText("Unpaid");
            holder.statusLabel.setTextColor(ContextCompat.getColor(context, R.color.unpaid));
        }

        // Set up the click listener for the card
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(bill); // Pass the clicked bill to the listener
            }
        });
    }

    @Override
    public int getItemCount() {
        return billList.size();
    }

    public List<Bill_model_billpage> getAllBillList() {
        return allBillList;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Bill_model_billpage> filteredList = new ArrayList<>();

                // Debug: Log the size of the full list before filtering
                Log.d("FilterDebug", "AllBillList Size: " + (allBillList != null ? allBillList.size() : "null"));

                if (constraint == null || constraint.length() == 0) {
                    // If the query is empty, return the full list
                    if (allBillList != null) {
                        filteredList.addAll(allBillList);
                    }
                } else {
                    // Convert the constraint to lower case once for efficiency
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    // Iterate through the full list to find matches
                    for (Bill_model_billpage item : allBillList) {
                        if (item.getBillName() != null && item.getBillName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(item); // Add matching items to the filtered list
                            Log.d("FilterDebug", "Match Found: " + item.getBillName());
                        }
                    }
                }

                // Prepare and return filter results
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                // Update the main list and notify the adapter
                billList.clear();
                if (results.values != null) {
                    billList.addAll((List<Bill_model_billpage>) results.values);
                    Log.d("FilterDebug", "Filtered List Size: " + billList.size());
                }
                notifyDataSetChanged();
            }
        };
    }

    static class BillViewHolder extends RecyclerView.ViewHolder {

        TextView billName, billCategory, billAmount, billDueDate, statusLabel;

        public BillViewHolder(@NonNull View itemView) {
            super(itemView);
            billName = itemView.findViewById(R.id.bill_name_billpage);
            billCategory = itemView.findViewById(R.id.bill_category_billpage);
            billAmount = itemView.findViewById(R.id.bill_amount_billpage);
            billDueDate = itemView.findViewById(R.id.bill_due_date_billpage);
            statusLabel = itemView.findViewById(R.id.status_label_billpage);
        }
    }
}

