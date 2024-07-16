package com.unixkitty.vampire_blood.advancement.predicate;

import com.google.gson.JsonObject;
import com.unixkitty.vampire_blood.capability.player.VampirismLevel;
import com.unixkitty.vampire_blood.capability.player.VampirismTier;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VampirismLevelPredicate
{
    public static final VampirismLevelPredicate ANY = new VampirismLevelPredicate();

    public static final String TIER_KEY = VampirismTier.getName(VampirismLevel.class);

    @Nullable
    private final VampirismLevel vampirismLevel;

    private VampirismLevelPredicate()
    {
        this.vampirismLevel = null;
    }

    private VampirismLevelPredicate(@Nonnull VampirismLevel vampirismLevel)
    {
        this.vampirismLevel = vampirismLevel;
    }

    public boolean matches(@Nonnull VampirismLevel vampirismLevel)
    {
        return this == ANY || this.vampirismLevel != null && this.vampirismLevel.equals(vampirismLevel);
    }

    public JsonObject serializeToJson()
    {
        if (this == ANY || this.vampirismLevel == null)
        {
            return new JsonObject();
        }
        else
        {
            JsonObject json = new JsonObject();

            json.addProperty(TIER_KEY, this.vampirismLevel.getId());

            return json;
        }
    }

    public static VampirismLevelPredicate tier(@Nonnull VampirismLevel vampirismLevel)
    {
        return new VampirismLevelPredicate(vampirismLevel);
    }

    public static VampirismLevelPredicate fromJson(JsonObject json)
    {
        if (json.has(TIER_KEY))
        {
            VampirismLevel level = VampirismTier.fromId(VampirismLevel.class, GsonHelper.getAsInt(json, TIER_KEY));

            if (level != null)
            {
                return new VampirismLevelPredicate(level);
            }
        }

        return VampirismLevelPredicate.ANY;
    }
}
