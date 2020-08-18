package com.neo.firebaseuserandemailverification.models.fcm;


/**
 * class rep the total body of post request to sent including the data obj to
 */
public class FirebaseCloudMessage {

    private String to;                              // token to send message to
    private Data data;                              // data to send to token owner or user

    public FirebaseCloudMessage(String to, Data data) {
        this.to = to;
        this.data = data;
    }

    public FirebaseCloudMessage() {

    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }


    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "FirebaseCloudMessage{" +
                "to='" + to + '\'' +
                ", data=" + data +
                '}';
    }
}
