package models;

import play.libs.F;

public class StatefulModel {
    
    // singleton
    public static StatefulModel instance = new StatefulModel();
    private StatefulModel() { }

    // communication by events
    public final F.EventStream event = new F.EventStream();


}
