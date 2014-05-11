package com.heyzap.sdk;

import com.heyzap.sdk.HeyzapLib;

public class Rzap {

	  public static ResourceGetter freResourceGetter;
    public static interface ResourceGetter {
      public int getResourceId(String s);
    }

    public static int attr(String id){ return getIdentifier("attr", id); }
    public static int drawable(String id){ return getIdentifier("drawable", "hz_" + id); }
    public static int layout(String id){ return getIdentifier("layout", "hz_" + id); }
    public static int anim(String id){ return getIdentifier("anim", "hz_" + id);}
    public static int styleable(String id){ return getIdentifier("styleable", id);}
    public static int id(String id){ return getIdentifier("id", id);}
    public static int getIdentifier(String type, String id){
    	if (freResourceGetter != null) {
    		String resourceId = String.format("%s.%s", type, id);
    		return freResourceGetter.getResourceId(resourceId);
    	} else {
    		return HeyzapLib.applicationContext.getResources().getIdentifier(id, type, HeyzapLib.applicationContext.getPackageName());
    	}
    }
}

// sed -i 's/\([^.]\)R\.\([a-z]*\?\)\.\([a-zA-Z_]*\?\)/\1Rzap.\2("\3")/' src/**/*.java
