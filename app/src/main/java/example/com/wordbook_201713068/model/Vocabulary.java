package example.com.wordbook_201713068.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Vocabulary {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo
    private String english;
    @ColumnInfo
    private String means;
    @ColumnInfo
    private long timestamp;

    public Vocabulary(String english, String means, long timestamp) {
        this.english = english;
        this.means = means;
        this.timestamp = timestamp;
    }

    public Vocabulary() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public String getMeans() {
        return means;
    }

    public void setMeans(String means) {
        this.means = means;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
