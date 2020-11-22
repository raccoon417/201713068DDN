package example.com.wordbook_201713068.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import example.com.wordbook_201713068.model.Vocabulary;
import io.reactivex.Observable;
import io.reactivex.Single;

@Dao
public interface VocabularyDAO {
    @Insert
    Single<Long> insert(Vocabulary voca);

    @Update
    Single<Integer> update(Vocabulary voca);

    @Query("SELECT * FROM vocabulary")
    Observable<List<Vocabulary>> selectAll();

    @Query("SELECT * FROM vocabulary WHERE id = :id")
    Observable<Vocabulary> findById(int id);

    @Query("SELECT COUNT(*) FROM vocabulary")
    Observable<Integer> getCount();

    //@Query("DELETE FROM vocabulary WHERE id = :id")
    //Observable<Integer> cleanup(int id);
}
