package com.unixkitty.vampire_blood.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VampirePlayerProvider implements ICapabilityProvider, INBTSerializable<CompoundTag>
{
    public static Capability<VampirePlayerData> VAMPIRE_PLAYER = CapabilityManager.get(new CapabilityToken<>() {});

    private VampirePlayerData vampirePlayerData = null;
    private final LazyOptional<VampirePlayerData> optional = LazyOptional.of(this::createCap);

    private VampirePlayerData createCap()
    {
        if (this.vampirePlayerData == null)
        {
            this.vampirePlayerData = new VampirePlayerData();
        }

        return this.vampirePlayerData;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        return cap == VAMPIRE_PLAYER ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag tag = new CompoundTag();

        createCap().saveNBTData(tag);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag)
    {
        createCap().loadNBTData(tag);
    }
}
