package moloi.tk.kovsiecash;

import android.os.Parcel;
import android.os.Parcelable;

public class Transaction implements Parcelable {
    private int transactionId;
    private String reference;
    private String dateTime;
    private double amount;
    private String type;
    private double balance;
    private String accountNumber;

    // Constructor
    public Transaction(int transactionId, String reference, String dateTime, double amount, String type, double balance, String accountNumber) {
        this.transactionId = transactionId;
        this.reference = reference;
        this.dateTime = dateTime;
        this.amount = amount;
        this.type = type;
        this.balance = balance;
        this.accountNumber = accountNumber;
    }

    // Getters and Setters
    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    // Parcelable implementation
    protected Transaction(Parcel in) {
        // Read data from the Parcel
        dateTime = in.readString();
        reference = in.readString();
        amount = in.readDouble();
        balance = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write data to the Parcel
        dest.writeString(dateTime);
        dest.writeString(reference);
        dest.writeDouble(amount);
        dest.writeDouble(balance);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };
}
