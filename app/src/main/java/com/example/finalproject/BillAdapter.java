package com.example.finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.BillViewHolder> {

    private List<Bill_model_homepage> billList;

    public BillAdapter(List<Bill_model_homepage> billList) {
        this.billList = billList;
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_dashboard, parent, false);
        return new BillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        Bill_model_homepage bill = billList.get(position);
        holder.amountTextView.setText(bill.getAmount());
        holder.categoryTextView.setText(bill.getCategory());
        holder.dueDateTextView.setText(bill.getDueDate());
    }

    @Override
    public int getItemCount() {
        return billList.size();
    }

    static class BillViewHolder extends RecyclerView.ViewHolder {
        TextView amountTextView, categoryTextView, dueDateTextView;

        public BillViewHolder(@NonNull View itemView) {
            super(itemView);
            amountTextView = itemView.findViewById(R.id.tvHeaderAmountDataRow);
            categoryTextView = itemView.findViewById(R.id.tvHeaderCategoryDataRow);
            dueDateTextView = itemView.findViewById(R.id.tvHeaderDueDateDataRow);
        }
    }
}