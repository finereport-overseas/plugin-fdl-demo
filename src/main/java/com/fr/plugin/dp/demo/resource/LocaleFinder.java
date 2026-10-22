package com.fr.plugin.dp.demo.resource;

import com.fr.intelli.record.Focus;
import com.fr.record.analyzer.EnableMetrics;
import com.fr.stable.fun.impl.AbstractLocaleFinder;

/**
 * Created by loy on 2023/11/3.
 */
@EnableMetrics
public class LocaleFinder extends AbstractLocaleFinder {

    @Focus(id = "com.fr.plugin.dp.demo", text = "Fdl_Plugin_Demo_Name")
    @Override
    public String find() {
        return "com/fr/plugin/dp/demo/locale/locale";
    }
}
