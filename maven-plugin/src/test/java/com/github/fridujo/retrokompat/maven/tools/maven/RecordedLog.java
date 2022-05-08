package com.github.fridujo.retrokompat.maven.tools.maven;

import org.apache.maven.plugin.logging.SystemStreamLog;

import java.util.ArrayList;
import java.util.List;

public class RecordedLog extends SystemStreamLog {

    private final List<String> infos = new ArrayList<>();
    private final List<String> warns = new ArrayList<>();

    @Override
    public void info(CharSequence content) {
        infos.add(content.toString());
        super.info(content);
    }

    @Override
    public void warn(CharSequence content) {
        warns.add(content.toString());
        super.warn(content);
    }

    public List<String> getInfos() {
        return infos;
    }

    public List<String> getWarns() {
        return warns;
    }
}
