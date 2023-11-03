package DTO;

import java.util.Date;

public class Connection {
    private String address;
    private String request;
    private Date requestTime;

    public Connection(String address, String request) {
        this.address = address;
        this.request = request;
        this.requestTime = new Date();
    }

    public Connection(){
        this.address = null;
        this.request = null;
        this.requestTime = new Date();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }

    @Override
    public String toString() {
        return "Connection{" + "address=" + address + ", request=" + request + ", requestTime=" + requestTime + '}';
    }


}
