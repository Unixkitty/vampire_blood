package com.unixkitty.vampire_blood.capability.provider;

import com.unixkitty.vampire_blood.capability.blood.BloodStorage;
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

public class BloodProvider implements ICapabilityProvider, INBTSerializable<CompoundTag>
{
    public static Capability<BloodStorage> BLOOD_STORAGE = CapabilityManager.get(new CapabilityToken<>() {});

    private BloodStorage blood_storage = null;
    private final LazyOptional<BloodStorage> optional = LazyOptional.of(this::createCap);

    private BloodStorage createCap()
    {
        if (this.blood_storage == null)
        {
            this.blood_storage = new BloodStorage();
        }

        return this.blood_storage;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        return cap == BLOOD_STORAGE ? optional.cast() : LazyOptional.empty();
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
