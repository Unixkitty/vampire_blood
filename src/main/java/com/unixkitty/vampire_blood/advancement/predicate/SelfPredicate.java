package com.unixkitty.vampire_blood.advancement.predicate;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

public class SelfPredicate
{
    public static final SelfPredicate ANY = new SelfPredicate(false);

    private static final String SELF_KEY = "self";

    private final boolean self;

    private SelfPredicate(boolean self)
    {
        this.self = self;
    }

    public boolean matches(boolean self)
    {
        return this == ANY || this.self == self;
    }

    public JsonObject serializeToJson()
    {
        if (this == ANY)
        {
            return new JsonObject();
        }
        else
        {
            JsonObject json = new JsonObject();

//            json.add(SELF_KEY, new JsonPrimitive(this.self));
            json.addProperty(SELF_KEY, this.self);

            return json;
        }
    }

    public static SelfPredicate self(boolean self)
    {
        return new SelfPredicate(self);
    }

    //    public static SelfPredicate fromJson(JsonElement jsonElement)
//    {
//        return jsonElement != null ? new SelfPredicate(GsonHelper.convertToBoolean(jsonElement, SELF_KEY)) : ANY;
//    }
    public static SelfPredicate fromJson(JsonObject json)
    {
        return new SelfPredicate(json.has(SELF_KEY) && GsonHelper.getAsBoolean(json, SELF_KEY));
    }
}
