package example.com.wordbook_201713068;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import example.com.wordbook_201713068.adapter.VocaAdapter;
import example.com.wordbook_201713068.common.IntentExtra;
import example.com.wordbook_201713068.database.AppDatabase;
import example.com.wordbook_201713068.model.Vocabulary;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class VocaListActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.list_voca)
    RecyclerView vocaList;

    private AppDatabase mDb;
    private Observable<List<Vocabulary>> mVocaListObservable;
    private Disposable mVocaListDisposable;

    private VocaAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voca_list);
        ButterKnife.bind(this);

        ActionBarManager.initBackArrowActionbar(this, toolbar, getString(R.string.title_voca_list));
        mDb = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                AppDatabase.DATABASE_NAME).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        observeVocaList();
    }

    private void setRecyclerView(@NonNull List<Vocabulary> data) {
        vocaList.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new VocaAdapter(data);
        vocaList.setAdapter(mAdapter);
    }

    protected void observeVocaList() {
        mVocaListDisposable = mDb.vocaDao().selectAll().observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(vocaList -> setRecyclerView(vocaList));
    }

    private void goToMainActivity() {
        finish();
    }

    @OnClick(R.id.button_add)
    public void onAddBtnClicked() {
        startActivity(new Intent(this, EditVocaActivity.class)
                .putExtra(IntentExtra.VOCA_ID, IntentExtra.VOCA_NULL));
    }

    @OnClick(R.id.button_search_add)
    public void onAddSrhBtnClicked() {
        startActivity(new Intent(this, EditVocaSearchActivity.class)
                .putExtra(IntentExtra.VOCA_ID, IntentExtra.VOCA_NULL));
    }

    @Override
    protected void onStop() {
        super.onStop();
        mVocaListDisposable.dispose();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.home || id == android.R.id.home) {
            goToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        goToMainActivity();
    }
}
