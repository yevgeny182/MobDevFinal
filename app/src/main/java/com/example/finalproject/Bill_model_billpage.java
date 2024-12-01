package com.example.finalproject;

public class Bill_model_billpage {
    private String id;
    private String Billname;
    private String Category;
    private double Amount;
    private String DueDate;
    private String status;

    public Bill_model_billpage(String id, String name, String category, double amount, String dueDate, String status) {
        this.id = id;
        this.Billname = name;
        this.Category = category;
        this.Amount = amount;
        this.DueDate = dueDate;
        this.status = status;
    }

    // Corrected getter for Billname
    public String getBillName() {
        return Billname;
    }

    public String getCategory() {
        return Category;
    }

    public double getAmount() {
        return Amount;
    }

    public String getDueDate() {
        return DueDate;
    }

    public String getStatus() {
        return status;
    }
}