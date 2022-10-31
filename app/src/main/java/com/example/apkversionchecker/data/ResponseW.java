package com.example.apkversionchecker.data;

import java.io.Serializable;

public class ResponseW<DATA> implements Serializable {
    public int code;
    public String msg;
    public DATA data;
}
