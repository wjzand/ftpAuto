package com.tools.ftpauto.entity;

public class PgyResultEntity {
    private String buildQRCodeURL = "";
    private String buildShortcutUrl = "";
    private String buildCreated = "";
    private String buildUpdated = "";

    private String buildIcon = "";

    public String getBuildIcon() {
        return buildIcon;
    }

    public void setBuildIcon(String buildIcon) {
        this.buildIcon = buildIcon;
    }

    public String getBuildQRCodeURL() {
        return buildQRCodeURL;
    }

    public void setBuildQRCodeURL(String buildQRCodeURL) {
        this.buildQRCodeURL = buildQRCodeURL;
    }

    public String getBuildShortcutUrl() {
        return buildShortcutUrl;
    }

    public void setBuildShortcutUrl(String buildShortcutUrl) {
        this.buildShortcutUrl = buildShortcutUrl;
    }

    public String getBuildCreated() {
        return buildCreated;
    }

    public void setBuildCreated(String buildCreated) {
        this.buildCreated = buildCreated;
    }

    public String getBuildUpdated() {
        return buildUpdated;
    }

    public void setBuildUpdated(String buildUpdated) {
        this.buildUpdated = buildUpdated;
    }

    @Override
    public String toString() {
        return "PgyResultEntity{" +
                "buildQRCodeURL='" + buildQRCodeURL + '\'' +
                ", buildShortcutUrl='" + buildShortcutUrl + '\'' +
                ", buildCreated='" + buildCreated + '\'' +
                ", buildUpdated='" + buildUpdated + '\'' +
                '}';
    }
}
