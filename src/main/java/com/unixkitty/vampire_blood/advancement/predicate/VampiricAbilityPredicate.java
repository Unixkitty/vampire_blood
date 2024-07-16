package com.unixkitty.vampire_blood.advancement.predicate;

import com.google.gson.JsonObject;
import com.unixkitty.vampire_blood.capability.player.VampireActiveAbility;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VampiricAbilityPredicate
{
    public static final VampiricAbilityPredicate ANY = new VampiricAbilityPredicate();

    public static final String ABILITY_KEY = VampireActiveAbility.class.getSimpleName().toLowerCase();

    @Nullable
    private final VampireActiveAbility ability;

    private VampiricAbilityPredicate()
    {
        this.ability = null;
    }

    private VampiricAbilityPredicate(@Nonnull VampireActiveAbility ability)
    {
        this.ability = ability;
    }

    public boolean matches(@Nonnull VampireActiveAbility ability)
    {
        return this == ANY || this.ability != null && this.ability.equals(ability);
    }

    public JsonObject serializeToJson()
    {
        if (this == ANY || this.ability == null)
        {
            return new JsonObject();
        }
        else
        {
            JsonObject json = new JsonObject();

            json.addProperty(ABILITY_KEY, this.ability.ordinal());

            return json;
        }
    }

    public static VampiricAbilityPredicate ability(@Nonnull VampireActiveAbility ability)
    {
        return new VampiricAbilityPredicate(ability);
    }

    public static VampiricAbilityPredicate fromJson(JsonObject json)
    {
        if (json.has(ABILITY_KEY))
        {
            VampireActiveAbility ability = VampireActiveAbility.fromOrdinal(GsonHelper.getAsInt(json, ABILITY_KEY));

            if (ability != null)
            {
                return new VampiricAbilityPredicate(ability);
            }
        }

        return VampiricAbilityPredicate.ANY;
    }
}
