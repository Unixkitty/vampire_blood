package com.unixkitty.vampire_blood.client.cache;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModDebugVarsCache
{
    public boolean catchingUV = false;
    public float armourUVCoverage;
    public int ticksInSun;
    public long age;
    public int thirstExhaustionIncrement;
    public int thirstTickTimer;
    public int noRegenTicks;
    public final int[] diet = new int[20];

    public float thirstExhaustionIncrementRate;
    public int highestThirstExhaustionIncrement;

    private int previousThirstExhaustionIncrement;
    private int totalIncrement;

    public void updateThirstExhaustionIncrementRate(int tickCounter)
    {
        int difference = this.thirstExhaustionIncrement - this.previousThirstExhaustionIncrement;

        if (difference > 0)
        {
            this.totalIncrement += difference;
        }

        if (tickCounter % 20 == 0)
        {
            this.thirstExhaustionIncrementRate = (float) this.totalIncrement / 20;

            this.totalIncrement = 0;
        }

        this.previousThirstExhaustionIncrement = this.thirstExhaustionIncrement;

        if (this.thirstExhaustionIncrement > this.highestThirstExhaustionIncrement)
        {
            this.highestThirstExhaustionIncrement = this.thirstExhaustionIncrement;
        }
    }
}
