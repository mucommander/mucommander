package com.mucommander.conf;

public enum SystemIconsPolicy {
    /** Never use system file icons */
    NEVER("never"),
    /** Use system file icons only for applications */
    APPLICATIONS_ONLY("applications"),

    /** Use system file icons only for folders */
    FOLDERS_ONLY("folders"),

    /** Always use system file icons */
    ALWAYS("always");

    private String name;

    SystemIconsPolicy(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
