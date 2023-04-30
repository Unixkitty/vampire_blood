package com.unixkitty.vampire_blood.util;

import net.minecraft.ChatFormatting;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.StringBuilders;

public class StringCrafter
{
    private final StringBuilder buffer = new StringBuilder();
    private String longestLine;

    public StringCrafter add(Object obj)
    {
        StringBuilders.appendValue(this.buffer, obj);
        return this;
    }

    public StringCrafter addWithQuotes(Object obj)
    {
        StringBuilders.appendDqValue(this.buffer, obj);
        return this;
    }

    public Pair<String, Integer> addLine(ChatFormatting chatFormatting, Object... objects)
    {
        for (Object textObject : objects)
        {
            add(textObject);
        }
        return endLine(chatFormatting);
    }

    public Pair<String, Integer> endLine(ChatFormatting formatting)
    {
        String line = this.buffer.toString();

        if (line.length() > longestLine.length())
        {
            this.longestLine = line;
        }

        buffer.setLength(0);

        return new ImmutablePair<>(line, formatting.getColor());
    }

    public String getLongestLine()
    {
        return this.longestLine;
    }

    public void clear()
    {
        this.buffer.setLength(0);
        this.longestLine = "";
    }
}
