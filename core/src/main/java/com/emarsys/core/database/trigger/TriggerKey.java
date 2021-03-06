package com.emarsys.core.database.trigger;

public class TriggerKey {
    private final String tableName;
    private final TriggerType triggerType;
    private final TriggerEvent triggerEvent;

    public TriggerKey(String tableName, TriggerType triggerType, TriggerEvent triggerEvent) {
        this.tableName = tableName;
        this.triggerType = triggerType;
        this.triggerEvent = triggerEvent;
    }

    public String getTableName() {
        return tableName;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public TriggerEvent getTriggerEvent() {
        return triggerEvent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TriggerKey that = (TriggerKey) o;

        if (tableName != null ? !tableName.equals(that.tableName) : that.tableName != null)
            return false;
        if (triggerType != that.triggerType) return false;
        return triggerEvent == that.triggerEvent;
    }

    @Override
    public int hashCode() {
        int result = tableName != null ? tableName.hashCode() : 0;
        result = 31 * result + (triggerType != null ? triggerType.hashCode() : 0);
        result = 31 * result + (triggerEvent != null ? triggerEvent.hashCode() : 0);
        return result;
    }
}
