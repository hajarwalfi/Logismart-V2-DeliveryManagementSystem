package com.logismart.logismartv2.entity;

public enum ParcelPriority {

    NORMAL("Normal", "Standard delivery (3-5 days)", 0),


    URGENT("Urgent", "Expedited delivery (1-2 days)", 1),

    EXPRESS("Express", "Express delivery (same/next day)", 2);

    private final String displayName;
    private final String description;
    private final int priorityLevel;  

    ParcelPriority(String displayName, String description, int priorityLevel) {
        this.displayName = displayName;
        this.description = description;
        this.priorityLevel = priorityLevel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getPriorityLevel() {
        return priorityLevel;
    }

    public boolean isExpress() {
        return this == EXPRESS;
    }

    public boolean isHighPriority() {
        return this == URGENT || this == EXPRESS;
    }
}
