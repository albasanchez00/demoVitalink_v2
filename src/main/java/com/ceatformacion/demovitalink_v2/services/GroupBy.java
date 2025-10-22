package com.ceatformacion.demovitalink_v2.services;

public enum GroupBy {
    day, week, month;

    public static GroupBy of(String v){
        try { return GroupBy.valueOf(v==null?"day":v.toLowerCase()); }
        catch(Exception e){ return day; }
    }
}