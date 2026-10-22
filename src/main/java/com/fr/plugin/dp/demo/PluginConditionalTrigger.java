package com.fr.plugin.dp.demo;

import com.fr.plugin.trigger.AbstractPluginConditionalTrigger;
import com.fr.plugin.trigger.PluginTrigger;

public class PluginConditionalTrigger extends AbstractPluginConditionalTrigger {

    @Override
    public PluginTrigger onceProjectRunning() {
        return new PluginTrigger() {
            @Override
            public void once() {
                System.out.println("plugin start");
            }

            @Override
            public void close() {
                System.out.println("plugin stop");
            }
        };
    }
}