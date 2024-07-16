package com.unixkitty.vampire_blood.advancement.predicate;

import com.google.gson.JsonObject;
import com.unixkitty.vampire_blood.capability.blood.BloodType;
import com.unixkitty.vampire_blood.capability.player.VampirePlayerData;
import com.unixkitty.vampire_blood.capability.player.VampirismTier;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BloodPredicate
{
    public static final BloodPredicate ANY = new BloodPredicate();

    public static final String TIER_KEY = VampirismTier.getName(BloodType.class);

    @Nullable
    private final BloodType bloodType;
    private final MinMaxBounds.Doubles purity;

    private BloodPredicate()
    {
        this.bloodType = null;
        this.purity = MinMaxBounds.Doubles.ANY;
    }

    private BloodPredicate(@Nonnull BloodType bloodType, MinMaxBounds.Doubles purity)
    {
        this.bloodType = bloodType;
        this.purity = purity;
    }

    public boolean matches(@Nonnull BloodType bloodType, float purity)
    {
        return this == ANY || this.bloodType != null && this.bloodType.equals(bloodType) && this.purity.matches(purity);
    }

    public JsonObject serializeToJson()
    {
        if (this == ANY || this.bloodType == null)
        {
            return new JsonObject();
        }
        else
        {
            JsonObject json = new JsonObject();

            json.addProperty(TIER_KEY, this.bloodType.getId());
            json.add(VampirePlayerData.BLOOD_PURITY_NBT_NAME, this.purity.serializeToJson());

            return json;
        }
    }

    public static BloodPredicate blood(@Nonnull BloodType bloodType)
    {
        return new BloodPredicate(bloodType, MinMaxBounds.Doubles.ANY);
    }

    public static BloodPredicate blood(@Nonnull BloodType bloodType, float purity)
    {
        return new BloodPredicate(bloodType, MinMaxBounds.Doubles.exactly(purity));
    }

    public static BloodPredicate fromJson(JsonObject json)
    {
        if (json.has(TIER_KEY))
        {
            BloodType level = VampirismTier.fromId(BloodType.class, GsonHelper.getAsInt(json, TIER_KEY));

            if (level != null)
            {
                return new BloodPredicate(level, json.has(VampirePlayerData.BLOOD_PURITY_NBT_NAME) ? MinMaxBounds.Doubles.fromJson(json.get(VampirePlayerData.BLOOD_PURITY_NBT_NAME)) : MinMaxBounds.Doubles.ANY);
            }
        }

        return BloodPredicate.ANY;
    }
}
