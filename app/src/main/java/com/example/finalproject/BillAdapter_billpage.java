package com.example.finalproject;

import android.content.Context;
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
    private List<Bill_model_billpage> billList;
    private List<Bill_model_billpage> allBillList;
    private Context context;

    public BillAdapter_billpage(List<Bill_model_billpage> billList , Context context) {
        this.billList = billList;
        this.context = context;
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

        holder.billName.setText(bill.getName());
        holder.billCategory.setText(bill.getCategory());
        holder.billAmount.setText(String.valueOf(bill.getAmount()));
        holder.billDueDate.setText("Due: " + (bill.getDueDate() != null ? bill.getDueDate() : "None"));

        // Set status label with color
        if (bill.isPaid()) {
            holder.statusLabel.setText("Paid");
            holder.statusLabel.setTextColor(ContextCompat.getColor(context, R.color.paidColor));
        } else {
            holder.statusLabel.setText("Unsettled");
            holder.statusLabel.setTextColor(ContextCompat.getColor(context, R.color.unsettledColor));
        }

        // Set up click listener for the card
        holder.itemView.setOnClickListener(v -> {
            // Show a Toast message when an item is clicked
            Toast.makeText(context, "Clicked on: " + bill.getName(), Toast.LENGTH_SHORT).show();
        });
    }
    @Override
    public int getItemCount() {
        return billList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Bill_model_billpage> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(allBillList); // No filter applied
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (Bill_model_billpage bill : allBillList) {
                        if (bill.getName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(bill);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                billList.clear();
                billList.addAll((List) results.values);
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
