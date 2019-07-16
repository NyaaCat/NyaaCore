package cat.nyaa.nyaacoretester.orm;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;

import java.util.Objects;

@Table("test5")
public class TableTest5 {
    @Column(primary = true)
    String player_name;
    @Column
    int balance;
    @Column
    String group_name;

    public TableTest5() {
    }

    public TableTest5(String playerName, int balance, String group) {
        this.player_name = playerName;
        this.balance = balance;
        this.group_name = group;
    }

    @Table
    public static class CollectedReport {
        @Column
        String group_name;
        @Column
        int max_balance;
        @Column
        int head_count;

        public CollectedReport() {
        }

        public CollectedReport(String group, int max_balance, int head_count) {
            this.group_name = group;
            this.max_balance = max_balance;
            this.head_count = head_count;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CollectedReport that = (CollectedReport) o;
            return max_balance == that.max_balance &&
                    head_count == that.head_count &&
                    Objects.equals(group_name, that.group_name);
        }
    }
}
