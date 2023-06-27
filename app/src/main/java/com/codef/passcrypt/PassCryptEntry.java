package com.codef.passcrypt;

public class PassCryptEntry {

    private final String name;
    private final String url;

    public PassCryptEntry(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return this.name;
    }

    public String getUrl() {
        return this.url;
    }

}
