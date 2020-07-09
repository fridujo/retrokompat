package com.github.fridujo.retrokompat;

import java.lang.reflect.Executable;

public class Signature {

    public final Executable executable;

    public Signature(Executable executable) {
        this.executable = executable;
    }
}
